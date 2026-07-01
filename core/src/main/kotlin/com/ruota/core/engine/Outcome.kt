package com.ruota.core.engine

import com.ruota.core.model.Player
import com.ruota.core.model.WheelWedge

/** The result of applying an action to the [GameEngine], used to drive UI feedback. */
sealed interface Outcome {
    /** The wheel landed on a cash wedge; the player must now call a consonant. */
    data class SpunCash(val amount: Int, val index: Int) : Outcome

    /** The wheel landed on Bankrupt; the player's round total was zeroed and the turn passed. */
    data class SpunBankrupt(val index: Int, val nextPlayer: Player) : Outcome

    /** The wheel landed on Lose-a-Turn; the turn passed. */
    data class SpunLoseATurn(val index: Int, val nextPlayer: Player) : Outcome

    /** A called consonant occurred [count] times, earning [earned]; the player continues. */
    data class ConsonantHit(val letter: Char, val count: Int, val earned: Int) : Outcome

    /** A called consonant did not occur; the turn passed. */
    data class ConsonantMiss(val letter: Char, val nextPlayer: Player) : Outcome

    /** A bought vowel occurred [count] times; cost was deducted; the player continues. */
    data class VowelBought(val letter: Char, val count: Int, val cost: Int) : Outcome

    /** A bought vowel did not occur; cost was deducted and the turn passed. */
    data class VowelMiss(val letter: Char, val cost: Int, val nextPlayer: Player) : Outcome

    /** The phrase was solved; [awarded] was banked to [winner]'s grand total. */
    data class SolvedCorrect(val winner: Player, val awarded: Int) : Outcome

    /** The solve attempt was wrong; the turn passed. */
    data class SolvedWrong(val nextPlayer: Player) : Outcome

    /** A new round started with a fresh puzzle. */
    data class RoundStarted(val roundIndex: Int, val startingPlayer: Player) : Outcome

    /** The game ended; [winner] has the highest grand total ([winners] if tied). */
    data class GameOver(val winners: List<Player>) : Outcome

    /** The action was not legal in the current state; state is unchanged. */
    data class Invalid(val reason: String) : Outcome
}
