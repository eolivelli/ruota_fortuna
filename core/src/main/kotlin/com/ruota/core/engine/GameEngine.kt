package com.ruota.core.engine

import com.ruota.core.board.Board
import com.ruota.core.model.GameConfig
import com.ruota.core.model.Player
import com.ruota.core.model.SolveMode
import com.ruota.core.model.WheelWedge
import com.ruota.core.puzzle.PuzzleProvider
import com.ruota.core.text.ItalianText
import com.ruota.core.wheel.Wheel

/**
 * The full game state machine for "La Ruota Della Fortuna". Pure Kotlin and fully
 * deterministic when given seeded [Wheel]/[PuzzleProvider] instances, so every rule is
 * unit-testable off-device.
 *
 * Turn flow: a player [spin]s; on a cash wedge they [guessConsonant]; on a hit they
 * continue (spin again, [buyVowel], or [solve]); on a miss/Bankrupt/Lose-a-Turn the turn
 * passes. Solving correctly banks the round total and ends the round.
 */
class GameEngine(
    val config: GameConfig,
    playerNames: List<String>,
    private val puzzleProvider: PuzzleProvider,
    private val wheel: Wheel,
) {
    private val players: MutableList<Player>
    private var currentPlayerIndex: Int = 0
    private var roundIndex: Int = 0
    private var phase: TurnPhase = TurnPhase.AWAITING_ACTION
    private var board: Board
    private var lastSpin: WheelWedge? = null
    private val guessed: MutableSet<Char> = mutableSetOf()

    init {
        require(playerNames.size in 1..4) { "Il gioco richiede da 1 a 4 giocatori" }
        require(config.numRounds >= 1) { "Servono almeno 1 round" }
        players = playerNames.mapIndexed { i, name -> Player(id = i, name = name) }.toMutableList()
        board = Board(puzzleProvider.next())
    }

    // ---- Queries -----------------------------------------------------------------

    fun snapshot(): GameState = GameState(
        config = config,
        players = players.toList(),
        currentPlayerIndex = currentPlayerIndex,
        roundIndex = roundIndex,
        phase = phase,
        category = board.puzzle.category,
        tiles = board.tiles(),
        lastSpin = lastSpin,
        guessedLetters = guessed.toSet(),
        availableActions = availableActions(),
        solutionText = if (phase == TurnPhase.ROUND_OVER || phase == TurnPhase.GAME_OVER) {
            board.puzzle.text
        } else {
            null
        },
        winners = if (phase == TurnPhase.GAME_OVER) computeWinners() else emptyList(),
    )

    /** The solution text — for the host-confirm reveal. Use sparingly; it leaks the answer. */
    fun peekSolution(): String = board.puzzle.text

    private fun availableActions(): Set<ActionType> = when (phase) {
        TurnPhase.AWAITING_ACTION -> buildSet {
            add(ActionType.SPIN)
            add(ActionType.SOLVE)
            if (canBuyVowel()) add(ActionType.BUY_VOWEL)
        }
        TurnPhase.AWAITING_CONSONANT -> setOf(ActionType.GUESS_CONSONANT, ActionType.SOLVE)
        TurnPhase.ROUND_OVER -> setOf(ActionType.ADVANCE_ROUND)
        TurnPhase.GAME_OVER -> emptySet()
    }

    private fun canBuyVowel(): Boolean {
        val affordable = players[currentPlayerIndex].roundTotal >= config.vowelCost
        val vowelLeft = ItalianText.VOWELS.any { it !in guessed }
        return affordable && vowelLeft
    }

    // ---- Actions -----------------------------------------------------------------

    /** Spin the wheel. Legal only at the start of an action phase. */
    fun spin(): Outcome {
        if (phase != TurnPhase.AWAITING_ACTION) {
            return Outcome.Invalid("Non puoi girare la ruota adesso")
        }
        val result = wheel.spin()
        return when (val wedge = result.wedge) {
            is WheelWedge.Cash -> {
                lastSpin = wedge
                phase = TurnPhase.AWAITING_CONSONANT
                Outcome.SpunCash(wedge.amount, result.index)
            }
            WheelWedge.Bankrupt -> {
                updateCurrent { it.copy(roundTotal = 0) }
                passTurn()
                Outcome.SpunBankrupt(result.index, players[currentPlayerIndex])
            }
            WheelWedge.LoseATurn -> {
                passTurn()
                Outcome.SpunLoseATurn(result.index, players[currentPlayerIndex])
            }
        }
    }

    /** Call a consonant after spinning a cash wedge. */
    fun guessConsonant(letter: Char): Outcome {
        if (phase != TurnPhase.AWAITING_CONSONANT) {
            return Outcome.Invalid("Devi prima girare la ruota")
        }
        if (!ItalianText.isConsonant(letter)) {
            return Outcome.Invalid("'$letter' non è una consonante")
        }
        val n = ItalianText.normalizeChar(letter)
        if (n in guessed) return Outcome.Invalid("La lettera '$n' è già stata chiamata")

        val cash = lastSpin as WheelWedge.Cash
        guessed.add(n)
        val count = board.reveal(letter)
        return if (count > 0) {
            val earned = cash.amount * count
            updateCurrent { it.copy(roundTotal = it.roundTotal + earned) }
            lastSpin = null
            phase = TurnPhase.AWAITING_ACTION
            Outcome.ConsonantHit(n, count, earned)
        } else {
            passTurn()
            Outcome.ConsonantMiss(n, players[currentPlayerIndex])
        }
    }

    /** Buy a vowel for [GameConfig.vowelCost], deducted from the round total. */
    fun buyVowel(letter: Char): Outcome {
        if (phase != TurnPhase.AWAITING_ACTION) {
            return Outcome.Invalid("Non puoi comprare una vocale adesso")
        }
        if (!ItalianText.isVowel(letter)) {
            return Outcome.Invalid("'$letter' non è una vocale")
        }
        val n = ItalianText.normalizeChar(letter)
        if (n in guessed) return Outcome.Invalid("La vocale '$n' è già stata comprata")
        if (players[currentPlayerIndex].roundTotal < config.vowelCost) {
            return Outcome.Invalid("Fondi insufficienti per comprare una vocale")
        }

        updateCurrent { it.copy(roundTotal = it.roundTotal - config.vowelCost) }
        guessed.add(n)
        val count = board.reveal(letter)
        return if (count > 0) {
            Outcome.VowelBought(n, count, config.vowelCost)
        } else {
            passTurn()
            Outcome.VowelMiss(n, config.vowelCost, players[currentPlayerIndex])
        }
    }

    /**
     * Attempt to solve by typing the phrase (KEYBOARD mode). Comparison ignores case,
     * accents, spaces and punctuation.
     */
    fun attemptSolve(text: String): Outcome {
        if (config.solveMode != SolveMode.KEYBOARD) {
            return Outcome.Invalid("La modalità di soluzione è a conferma manuale")
        }
        if (!canSolve()) return Outcome.Invalid("Non puoi risolvere adesso")
        return if (ItalianText.phrasesMatch(text, board.puzzle.text)) winRound() else failSolve()
    }

    /** Confirm a spoken solution as correct/incorrect (HOST_CONFIRM mode). */
    fun hostConfirm(correct: Boolean): Outcome {
        if (config.solveMode != SolveMode.HOST_CONFIRM) {
            return Outcome.Invalid("La modalità di soluzione è a tastiera")
        }
        if (!canSolve()) return Outcome.Invalid("Non puoi risolvere adesso")
        return if (correct) winRound() else failSolve()
    }

    /** Move on to the next round (or reveal the game is over). */
    fun advanceRound(): Outcome {
        if (phase != TurnPhase.ROUND_OVER) return Outcome.Invalid("Il round non è finito")
        roundIndex += 1
        for (i in players.indices) players[i] = players[i].copy(roundTotal = 0)
        board = Board(puzzleProvider.next())
        guessed.clear()
        lastSpin = null
        currentPlayerIndex = roundIndex % players.size
        phase = TurnPhase.AWAITING_ACTION
        return Outcome.RoundStarted(roundIndex, players[currentPlayerIndex])
    }

    private fun canSolve(): Boolean =
        phase == TurnPhase.AWAITING_ACTION || phase == TurnPhase.AWAITING_CONSONANT

    private fun winRound(): Outcome {
        board.revealAll()
        val winner = players[currentPlayerIndex]
        val awarded = winner.roundTotal
        players[currentPlayerIndex] = winner.copy(grandTotal = winner.grandTotal + awarded)
        lastSpin = null
        phase = if (roundIndex >= config.numRounds - 1) TurnPhase.GAME_OVER else TurnPhase.ROUND_OVER
        return Outcome.SolvedCorrect(players[currentPlayerIndex], awarded)
    }

    private fun failSolve(): Outcome {
        passTurn()
        return Outcome.SolvedWrong(players[currentPlayerIndex])
    }

    private fun passTurn() {
        lastSpin = null
        phase = TurnPhase.AWAITING_ACTION
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size
    }

    private inline fun updateCurrent(transform: (Player) -> Player) {
        players[currentPlayerIndex] = transform(players[currentPlayerIndex])
    }

    private fun computeWinners(): List<Player> {
        val best = players.maxOf { it.grandTotal }
        return players.filter { it.grandTotal == best }
    }
}
