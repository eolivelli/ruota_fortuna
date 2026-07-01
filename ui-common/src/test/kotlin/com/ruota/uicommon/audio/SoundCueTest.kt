package com.ruota.uicommon.audio

import com.ruota.core.engine.Outcome
import com.ruota.core.model.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SoundCueTest {

    private val p = Player(0, "Ada")

    @Test fun consonantHitDingsOncePerOccurrence() {
        assertEquals(SoundCue.Ding(3), soundCueFor(Outcome.ConsonantHit('S', 3, 900)))
    }

    @Test fun boughtVowelAlsoDings() {
        assertEquals(SoundCue.Ding(2), soundCueFor(Outcome.VowelBought('A', 2, 250)))
    }

    @Test fun missesBuzz() {
        assertEquals(SoundCue.Miss, soundCueFor(Outcome.ConsonantMiss('Z', p)))
        assertEquals(SoundCue.Miss, soundCueFor(Outcome.VowelMiss('U', 250, p)))
    }

    @Test fun correctSolveFanfares() {
        assertEquals(SoundCue.Winner, soundCueFor(Outcome.SolvedCorrect(p, 1200)))
    }

    @Test fun otherOutcomesAreSilentHere() {
        assertNull(soundCueFor(Outcome.SpunCash(500, 2)))     // spin sound handled separately
        assertNull(soundCueFor(Outcome.SolvedWrong(p)))
        assertNull(soundCueFor(null))
    }
}
