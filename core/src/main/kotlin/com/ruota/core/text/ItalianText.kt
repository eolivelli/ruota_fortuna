package com.ruota.core.text

/**
 * Italian text utilities shared by the board (masking / occurrence counting) and the
 * solve checker. All comparison is done on a normalized form: uppercased, accents
 * folded to their base vowel, non-letters handled explicitly.
 */
object ItalianText {

    /** The five Italian vowels in normalized (accent-folded, uppercase) form. */
    val VOWELS: Set<Char> = setOf('A', 'E', 'I', 'O', 'U')

    /** Folds a single character to uppercase and strips accents from vowels. */
    fun normalizeChar(c: Char): Char = when (c.uppercaseChar()) {
        'À', 'Á', 'Â', 'Ä', 'Ã' -> 'A'
        'È', 'É', 'Ê', 'Ë' -> 'E'
        'Ì', 'Í', 'Î', 'Ï' -> 'I'
        'Ò', 'Ó', 'Ô', 'Ö', 'Õ' -> 'O'
        'Ù', 'Ú', 'Û', 'Ü' -> 'U'
        else -> c.uppercaseChar()
    }

    /** True if the character represents a puzzle letter (A–Z after normalization). */
    fun isLetter(c: Char): Boolean = normalizeChar(c) in 'A'..'Z'

    /** True if the character is one of the five vowels. */
    fun isVowel(c: Char): Boolean = normalizeChar(c) in VOWELS

    /** True if the character is a letter that is not a vowel. */
    fun isConsonant(c: Char): Boolean = isLetter(c) && !isVowel(c)

    /**
     * Reduces a string to only its normalized letters (dropping spaces, punctuation,
     * accents and case) for solve comparison. "L'ape è d'oro" -> "LAPEEDORO".
     */
    fun normalizeForCompare(s: String): String =
        buildString {
            for (ch in s) if (isLetter(ch)) append(normalizeChar(ch))
        }

    /** True if two phrases are equal ignoring case, accents, spaces and punctuation. */
    fun phrasesMatch(a: String, b: String): Boolean =
        normalizeForCompare(a) == normalizeForCompare(b)
}
