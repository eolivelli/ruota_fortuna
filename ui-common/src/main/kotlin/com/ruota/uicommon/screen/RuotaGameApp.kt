package com.ruota.uicommon.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.ruota.core.engine.GameEngine
import com.ruota.core.model.GameConfig
import com.ruota.core.model.Puzzle
import com.ruota.core.model.WheelWedge
import com.ruota.core.puzzle.ShufflingPuzzleProvider
import com.ruota.core.wheel.Wheel
import com.ruota.uicommon.game.GameViewModel
import com.ruota.uicommon.prefs.PreferencesRepository
import com.ruota.uicommon.puzzle.AssetPhrases
import com.ruota.uicommon.setup.PlayerSetup
import com.ruota.uicommon.theme.RuotaColors
import kotlinx.coroutines.launch

/** Holds the engine and its wheel wedges for an active game session. */
private class GameSession(val engine: GameEngine, val wedges: List<WheelWedge>)

/**
 * The whole game as a single Composable: player setup, then play. Loads the bundled
 * phrase pack and persisted preferences, builds the [GameEngine] on start, and swaps to
 * the [GameScreen]. Both the mobile and TV apps host this inside their own theme.
 */
@Composable
fun RuotaGameApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val puzzles: List<Puzzle> = remember {
        runCatching { AssetPhrases.load(context) }
            .getOrElse { listOf(Puzzle("Errore", "Impossibile caricare le frasi")) }
    }
    val prefsRepo = remember { PreferencesRepository(context) }
    val persistedConfig by prefsRepo.config.collectAsState(initial = GameConfig())
    val scope = rememberCoroutineScope()

    var session by remember { mutableStateOf<GameSession?>(null) }

    val current = session
    if (current == null) {
        PlayerSetup(
            initialConfig = persistedConfig,
            onStart = { names, config ->
                scope.launch {
                    prefsRepo.setNumRounds(config.numRounds)
                    prefsRepo.setVowelCost(config.vowelCost)
                    prefsRepo.setSolveMode(config.solveMode)
                }
                val wheel = Wheel.standard()
                val engine = GameEngine(config, names, ShufflingPuzzleProvider(puzzles), wheel)
                session = GameSession(engine, wheel.wedges)
            },
            modifier = modifier
                .fillMaxSize()
                .background(RuotaColors.Background),
        )
    } else {
        val viewModel = remember(current) { GameViewModel(current.engine) }
        GameScreen(
            viewModel = viewModel,
            wedges = current.wedges,
            peekSolution = current.engine::peekSolution,
            onNewGame = { session = null },
            modifier = modifier,
        )
    }
}
