package com.ruota.uicommon.board

import com.ruota.core.board.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardLayoutTest {

    private fun letters(text: String): List<Tile> =
        text.map { Tile.Letter(it, revealed = false) }

    @Test fun splitsOnSpacesPreservingOrder() {
        val tiles = letters("AB") + Tile.Space + letters("CD")
        val words = BoardLayout.groupIntoWords(tiles)
        assertEquals(2, words.size)
        assertEquals(listOf('A', 'B'), words[0].map { (it as Tile.Letter).display })
        assertEquals(listOf('C', 'D'), words[1].map { (it as Tile.Letter).display })
    }

    @Test fun keepsSymbolsInsideWords() {
        val tiles = letters("L") + Tile.Symbol('\'') + letters("APE") + Tile.Space + letters("DORO")
        val words = BoardLayout.groupIntoWords(tiles)
        assertEquals(2, words.size)
        assertEquals(5, words[0].size) // L ' A P E
        assertTrue(words[0][1] is Tile.Symbol)
    }

    @Test fun dropsLeadingTrailingAndMultipleSpaces() {
        val tiles = listOf(Tile.Space, Tile.Space) +
            letters("HI") +
            listOf(Tile.Space, Tile.Space, Tile.Space) +
            letters("YO") +
            listOf(Tile.Space)
        val words = BoardLayout.groupIntoWords(tiles)
        assertEquals(2, words.size)
        assertEquals("HI", words[0].joinToString("") { (it as Tile.Letter).display.toString() })
        assertEquals("YO", words[1].joinToString("") { (it as Tile.Letter).display.toString() })
    }

    @Test fun emptyInputYieldsNoWords() {
        assertEquals(emptyList<List<Tile>>(), BoardLayout.groupIntoWords(emptyList()))
    }

    @Test fun onlySpacesYieldsNoWords() {
        assertEquals(
            emptyList<List<Tile>>(),
            BoardLayout.groupIntoWords(listOf(Tile.Space, Tile.Space)),
        )
    }

    @Test fun singleWordNoSpaces() {
        val words = BoardLayout.groupIntoWords(letters("CIAO"))
        assertEquals(1, words.size)
        assertEquals(4, words[0].size)
    }
}
