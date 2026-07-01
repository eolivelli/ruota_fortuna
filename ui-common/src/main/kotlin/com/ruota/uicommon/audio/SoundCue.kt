package com.ruota.uicommon.audio

import com.ruota.core.engine.Outcome

/** A sound the game should play in response to an [Outcome]. Pure/JVM-testable. */
sealed interface SoundCue {
    /** Play the letter "ding" once per revealed occurrence ([times] > 0). */
    data class Ding(val times: Int) : SoundCue

    /** Play the "no letter found" buzzer. */
    data object Miss : SoundCue

    /** Play the winner fanfare. */
    data object Winner : SoundCue
}

/**
 * Maps an [Outcome] to the sound cue to play. The wheel-spin sound is handled separately
 * (it starts on the spin action and stops when the wheel settles), so spin outcomes map
 * to null here.
 */
fun soundCueFor(outcome: Outcome?): SoundCue? = when (outcome) {
    is Outcome.ConsonantHit -> SoundCue.Ding(outcome.count)
    is Outcome.VowelBought -> SoundCue.Ding(outcome.count)
    is Outcome.ConsonantMiss -> SoundCue.Miss
    is Outcome.VowelMiss -> SoundCue.Miss
    is Outcome.SolvedCorrect -> SoundCue.Winner
    else -> null
}
