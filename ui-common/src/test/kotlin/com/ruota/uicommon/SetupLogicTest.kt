package com.ruota.uicommon

import com.ruota.uicommon.setup.SetupLogic
import org.junit.Assert.assertEquals
import org.junit.Test

class SetupLogicTest {

    @Test
    fun defaultPlayerNamesProducesGiocatoreLabels() {
        assertEquals(listOf("Giocatore 1", "Giocatore 2"), SetupLogic.defaultPlayerNames(2))
        assertEquals(
            listOf("Giocatore 1", "Giocatore 2", "Giocatore 3", "Giocatore 4"),
            SetupLogic.defaultPlayerNames(4),
        )
    }

    @Test
    fun defaultPlayerNamesClampsToRange() {
        assertEquals(1, SetupLogic.defaultPlayerNames(0).size)
        assertEquals(1, SetupLogic.defaultPlayerNames(-5).size)
        assertEquals(4, SetupLogic.defaultPlayerNames(9).size)
    }

    @Test
    fun clampPlayersHonorsBounds() {
        assertEquals(1, SetupLogic.clampPlayers(0))
        assertEquals(4, SetupLogic.clampPlayers(100))
        assertEquals(3, SetupLogic.clampPlayers(3))
    }

    @Test
    fun clampRoundsHonorsBounds() {
        assertEquals(1, SetupLogic.clampRounds(0))
        assertEquals(12, SetupLogic.clampRounds(50))
        assertEquals(4, SetupLogic.clampRounds(4))
    }

    @Test
    fun resizeNamesPreservesEditsAndFillsDefaults() {
        val edited = listOf("Anna", "", "Marco")
        val resized = SetupLogic.resizeNames(edited, 4)
        assertEquals(listOf("Anna", "Giocatore 2", "Marco", "Giocatore 4"), resized)
    }

    @Test
    fun resizeNamesShrinksAndClamps() {
        val edited = listOf("Anna", "Bea", "Carlo", "Dino")
        assertEquals(listOf("Anna", "Bea"), SetupLogic.resizeNames(edited, 2))
        assertEquals(4, SetupLogic.resizeNames(edited, 99).size)
    }

    @Test
    fun finalizeNamesTrimsBlanks() {
        val edited = listOf("  Anna  ", "   ")
        assertEquals(listOf("Anna", "Giocatore 2"), SetupLogic.finalizeNames(edited, 2))
    }
}
