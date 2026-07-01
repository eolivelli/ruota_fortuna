package com.ruota.uicommon.keyboard

/**
 * The Italian on-screen alphabet, split into vowels and consonants. Pure data so it can be
 * unit-tested on the JVM without Compose.
 */
object Keys {

    /** The five vowels, in alphabetical order. */
    val VOWELS: List<Char> = listOf('A', 'E', 'I', 'O', 'U')

    /** Every other A–Z letter (the consonants), in alphabetical order. 21 letters. */
    val CONSONANTS: List<Char> = ('A'..'Z').filter { it !in VOWELS }

    /**
     * Chunks the consonants into rows of at most [perRow] keys for a grid layout.
     * The final row may be shorter. [perRow] must be positive.
     */
    fun consonantRows(perRow: Int): List<List<Char>> {
        require(perRow > 0) { "perRow must be positive, was $perRow" }
        return CONSONANTS.chunked(perRow)
    }
}
