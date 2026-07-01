package com.ruota.uicommon.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ruota.core.engine.GameEngine
import com.ruota.core.engine.GameState
import com.ruota.core.engine.Outcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A thin, synchronous bridge between an already-built [GameEngine] and Compose. Every action
 * calls the engine, then republishes a fresh [snapshot][GameEngine.snapshot] and the [Outcome].
 * No dispatchers are used, so the whole thing runs on the JVM under plain unit tests.
 */
class GameViewModel(private val engine: GameEngine) : ViewModel() {

    private val _state = MutableStateFlow(engine.snapshot())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _lastOutcome = MutableStateFlow<Outcome?>(null)
    val lastOutcome: StateFlow<Outcome?> = _lastOutcome.asStateFlow()

    fun spin() = apply { engine.spin() }

    fun guessConsonant(letter: Char) = apply { engine.guessConsonant(letter) }

    fun buyVowel(letter: Char) = apply { engine.buyVowel(letter) }

    fun attemptSolve(text: String) = apply { engine.attemptSolve(text) }

    fun hostConfirm(correct: Boolean) = apply { engine.hostConfirm(correct) }

    fun advanceRound() = apply { engine.advanceRound() }

    /** Runs an engine action then republishes state and the produced outcome. */
    private inline fun apply(action: () -> Outcome) {
        val outcome = action()
        _state.value = engine.snapshot()
        _lastOutcome.value = outcome
    }
}

/**
 * Builds a [GameViewModel] from a lazily-provided [GameEngine], so the engine (with its
 * chosen players/config/wheel) can be constructed at navigation time.
 */
class GameViewModelFactory(
    private val engineProvider: () -> GameEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(GameViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return GameViewModel(engineProvider()) as T
    }
}
