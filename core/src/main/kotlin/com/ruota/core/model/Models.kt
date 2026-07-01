package com.ruota.core.model

/** A puzzle: an Italian phrase and the category shown to players. */
data class Puzzle(
    val category: String,
    val text: String,
)

/** A player in a pass-and-play game. Immutable; the engine replaces instances on change. */
data class Player(
    val id: Int,
    val name: String,
    /** Money banked across all completed rounds. */
    val grandTotal: Int = 0,
    /** Money accumulated in the current round (lost on Bankrupt, banked on a correct solve). */
    val roundTotal: Int = 0,
)

/** A wedge the wheel can land on. */
sealed interface WheelWedge {
    /** A positive cash value; multiplied by the number of occurrences of a called consonant. */
    data class Cash(val amount: Int) : WheelWedge

    /** Zeroes the current player's round total and passes the turn. */
    data object Bankrupt : WheelWedge

    /** Passes the turn with no other effect. */
    data object LoseATurn : WheelWedge
}

/** How a solve attempt is judged. */
enum class SolveMode {
    /** The player types the phrase; the engine compares it to the answer. */
    KEYBOARD,

    /** The solution is revealed and the group confirms correct/incorrect. */
    HOST_CONFIRM,
}

/** Tunable game settings, surfaced in preferences. */
data class GameConfig(
    val numRounds: Int = 4,
    val vowelCost: Int = 250,
    val solveMode: SolveMode = SolveMode.KEYBOARD,
)
