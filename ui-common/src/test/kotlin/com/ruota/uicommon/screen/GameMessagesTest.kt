package com.ruota.uicommon.screen

import com.ruota.core.engine.Outcome
import com.ruota.core.model.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameMessagesTest {

    private val bob = Player(1, "Bob")

    @Test fun nullOutcomeHasNoMessage() {
        assertNull(outcomeMessage(null))
    }

    @Test fun cashMessageFormatsMoney() {
        assertEquals("Hai girato \$600. Scegli una consonante!", outcomeMessage(Outcome.SpunCash(600, 3)))
    }

    @Test fun consonantHitShowsCountAndEarnings() {
        assertEquals("'P' × 2 = +\$400!", outcomeMessage(Outcome.ConsonantHit('P', 2, 400)))
    }

    @Test fun invalidPassesReasonThrough() {
        assertEquals("Fondi insufficienti", outcomeMessage(Outcome.Invalid("Fondi insufficienti")))
    }

    @Test fun spinIndexExtractedOnlyForSpins() {
        assertEquals(3, spinIndexOf(Outcome.SpunCash(600, 3)))
        assertEquals(7, spinIndexOf(Outcome.SpunBankrupt(7, bob)))
        assertEquals(1, spinIndexOf(Outcome.SpunLoseATurn(1, bob)))
        assertNull(spinIndexOf(Outcome.ConsonantHit('P', 2, 400)))
        assertNull(spinIndexOf(null))
    }
}
