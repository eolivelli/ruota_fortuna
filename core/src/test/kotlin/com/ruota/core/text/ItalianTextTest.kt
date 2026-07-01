package com.ruota.core.text

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ItalianTextTest {

    @Test fun foldsAccentedVowels() {
        assertEquals('E', ItalianText.normalizeChar('è'))
        assertEquals('E', ItalianText.normalizeChar('É'))
        assertEquals('A', ItalianText.normalizeChar('à'))
        assertEquals('O', ItalianText.normalizeChar('ò'))
        assertEquals('U', ItalianText.normalizeChar('ù'))
        assertEquals('I', ItalianText.normalizeChar('ì'))
    }

    @Test fun classifiesLettersVowelsConsonants() {
        assertTrue(ItalianText.isVowel('à'))
        assertTrue(ItalianText.isVowel('E'))
        assertFalse(ItalianText.isVowel('B'))
        assertTrue(ItalianText.isConsonant('b'))
        assertFalse(ItalianText.isConsonant('a'))
        assertFalse(ItalianText.isLetter(' '))
        assertFalse(ItalianText.isLetter('\''))
        assertFalse(ItalianText.isConsonant('3'))
    }

    @Test fun normalizeForCompareStripsNonLetters() {
        assertEquals("LAPEEDORO", ItalianText.normalizeForCompare("L'ape è d'oro"))
        assertEquals("INBOCCAALLUPO", ItalianText.normalizeForCompare("In bocca al lupo"))
    }

    @Test fun phrasesMatchIgnoresCaseAccentsSpacesPunctuation() {
        assertTrue(ItalianText.phrasesMatch("in bocca al lupo", "IN BOCCA AL LUPO"))
        assertTrue(ItalianText.phrasesMatch("Perché no?", "PERCHE NO"))
        assertTrue(ItalianText.phrasesMatch("l'italia", "L ITALIA"))
        assertFalse(ItalianText.phrasesMatch("gatto", "gatti"))
    }
}
