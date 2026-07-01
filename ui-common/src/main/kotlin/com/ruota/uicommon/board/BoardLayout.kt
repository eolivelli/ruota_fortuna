package com.ruota.uicommon.board

import com.ruota.core.board.Tile

/**
 * Pure layout helpers for the puzzle board. Kept free of any Compose dependency so it
 * can be unit-tested on the JVM.
 */
object BoardLayout {

    /**
     * Splits a flat, reading-order list of [Tile]s into words, breaking on [Tile.Space].
     * Spaces act as separators and are not included in any word; empty groups (produced by
     * leading, trailing or consecutive spaces) are dropped. The relative order of the
     * remaining tiles is preserved. Keeping whole words together lets the UI wrap on word
     * boundaries instead of splitting a word across lines.
     */
    fun groupIntoWords(tiles: List<Tile>): List<List<Tile>> {
        val words = mutableListOf<List<Tile>>()
        var current = mutableListOf<Tile>()
        for (tile in tiles) {
            if (tile is Tile.Space) {
                if (current.isNotEmpty()) {
                    words.add(current)
                    current = mutableListOf()
                }
            } else {
                current.add(tile)
            }
        }
        if (current.isNotEmpty()) words.add(current)
        return words
    }
}
