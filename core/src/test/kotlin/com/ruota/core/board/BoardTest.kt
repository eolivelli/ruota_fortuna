package com.ruota.core.board

import com.ruota.core.model.Puzzle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BoardTest {

    private fun board(text: String) = Board(Puzzle("Cosa", text))

    @Test fun countsOccurrences() {
        val b = board("CASA")
        assertEquals(2, b.occurrences('A'))
        assertEquals(1, b.occurrences('C'))
        assertEquals(0, b.occurrences('Z'))
    }

    @Test fun revealReturnsCountAndMarksRevealed() {
        val b = board("CASA")
        assertFalse(b.isRevealed('A'))
        assertEquals(2, b.reveal('a'))
        assertTrue(b.isRevealed('A'))
        assertEquals(0, b.reveal('Z')) // absent letter still counts as revealed set entry
    }

    @Test fun accentedLetterRevealedByBaseVowel() {
        // "PERCHÉ" contains E and É, both of which normalize to E.
        val b = board("PERCHÉ")
        assertEquals(2, b.occurrences('E'))
        assertEquals(2, b.reveal('E'))
        val letters = b.tiles().filterIsInstance<Tile.Letter>()
        assertTrue(letters.filter { it.display == 'E' || it.display == 'É' }.all { it.revealed })
    }

    @Test fun tilesClassifySpacesAndSymbols() {
        val tiles = board("L'ASSO D'ORO").tiles()
        assertTrue(tiles.any { it is Tile.Space })
        assertTrue(tiles.any { it is Tile.Symbol && it.display == '\'' })
        assertTrue(tiles.filterIsInstance<Tile.Letter>().none { it.revealed })
    }

    @Test fun isSolvedWhenAllLettersRevealed() {
        val b = board("CASA")
        assertFalse(b.isSolved())
        b.reveal('C'); b.reveal('S'); b.reveal('A')
        assertTrue(b.isSolved())
    }

    @Test fun revealAllSolves() {
        val b = board("IN BOCCA AL LUPO")
        b.revealAll()
        assertTrue(b.isSolved())
    }
}
