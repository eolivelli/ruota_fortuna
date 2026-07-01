package com.ruota.core.board

import com.ruota.core.model.Puzzle
import com.ruota.core.text.ItalianText

/** One rendered cell of the puzzle board. */
sealed interface Tile {
    /**
     * A letter cell. [display] is the original (accented) uppercase character to show
     * when [revealed]; while hidden the UI draws a blank tile.
     */
    data class Letter(val display: Char, val revealed: Boolean) : Tile

    /** A word separator (empty background cell). */
    data object Space : Tile

    /** Always-visible punctuation (apostrophe, hyphen, etc.). */
    data class Symbol(val display: Char) : Tile
}

/**
 * Tracks which letters of a puzzle have been revealed and renders the masked board.
 * All matching is done on the normalized (accent-folded, uppercase) form so that, e.g.,
 * calling 'E' also reveals 'È'.
 */
class Board(val puzzle: Puzzle) {

    private val revealed: MutableSet<Char> = mutableSetOf()

    /** Normalized letters revealed so far (read-only view). */
    val revealedLetters: Set<Char> get() = revealed.toSet()

    /** Number of times [letter] occurs in the puzzle (0 if none). */
    fun occurrences(letter: Char): Int {
        val n = ItalianText.normalizeChar(letter)
        return puzzle.text.count { ItalianText.isLetter(it) && ItalianText.normalizeChar(it) == n }
    }

    /** True once [letter] has been revealed. */
    fun isRevealed(letter: Char): Boolean = ItalianText.normalizeChar(letter) in revealed

    /**
     * Reveals every occurrence of [letter] and returns how many there were.
     * Revealing a letter that does not occur is a no-op that returns 0.
     */
    fun reveal(letter: Char): Int {
        val n = ItalianText.normalizeChar(letter)
        revealed.add(n)
        return occurrences(letter)
    }

    /** Reveals the whole puzzle (used when solved or when the solution is shown). */
    fun revealAll() {
        for (ch in puzzle.text) if (ItalianText.isLetter(ch)) revealed.add(ItalianText.normalizeChar(ch))
    }

    /** True when every letter cell has been revealed. */
    fun isSolved(): Boolean =
        puzzle.text.all { !ItalianText.isLetter(it) || ItalianText.normalizeChar(it) in revealed }

    /** The current board as a flat list of tiles, in reading order. */
    fun tiles(): List<Tile> = puzzle.text.map { ch ->
        when {
            ch.isWhitespace() -> Tile.Space
            ItalianText.isLetter(ch) -> Tile.Letter(
                display = ch.uppercaseChar(),
                revealed = ItalianText.normalizeChar(ch) in revealed,
            )
            else -> Tile.Symbol(ch)
        }
    }
}
