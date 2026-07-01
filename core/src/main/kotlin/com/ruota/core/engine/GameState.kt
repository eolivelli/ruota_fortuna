package com.ruota.core.engine

import com.ruota.core.board.Tile
import com.ruota.core.model.GameConfig
import com.ruota.core.model.Player
import com.ruota.core.model.WheelWedge

/** What the current player is allowed to do right now. */
enum class TurnPhase {
    /** Turn start / after a correct call: may Spin, Buy a vowel, or Solve. */
    AWAITING_ACTION,

    /** Just spun a cash wedge: must call a consonant (or Solve). */
    AWAITING_CONSONANT,

    /** The round was solved; waiting for [GameEngine.advanceRound]. */
    ROUND_OVER,

    /** The final round is solved; the game is finished. */
    GAME_OVER,
}

/** High-level action types, used by the UI to enable/disable controls. */
enum class ActionType { SPIN, GUESS_CONSONANT, BUY_VOWEL, SOLVE, ADVANCE_ROUND }

/**
 * An immutable snapshot of the game for rendering. Produced by [GameEngine.snapshot];
 * a Compose ViewModel can expose this as observable state.
 */
data class GameState(
    val config: GameConfig,
    val players: List<Player>,
    val currentPlayerIndex: Int,
    /** 0-based index of the current round. */
    val roundIndex: Int,
    val phase: TurnPhase,
    val category: String,
    val tiles: List<Tile>,
    /** The wedge from the most recent spin this turn, or null. */
    val lastSpin: WheelWedge?,
    /** Normalized letters already called or bought this round. */
    val guessedLetters: Set<Char>,
    val availableActions: Set<ActionType>,
    /** The full solution text, revealed only when the round or game is over. */
    val solutionText: String?,
    /** Non-empty when [phase] is GAME_OVER: the player(s) with the highest grand total. */
    val winners: List<Player>,
) {
    val currentPlayer: Player get() = players[currentPlayerIndex]
    val roundNumber: Int get() = roundIndex + 1
    val isOver: Boolean get() = phase == TurnPhase.GAME_OVER
}
