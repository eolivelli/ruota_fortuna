package com.ruota.uicommon.keyboard

import com.ruota.core.text.ItalianText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeysTest {

    @Test fun vowelsAreExactlyTheFiveItalianVowels() {
        assertEquals(listOf('A', 'E', 'I', 'O', 'U'), Keys.VOWELS)
    }

    @Test fun consonantsHave21LettersAndNoVowel() {
        assertEquals(21, Keys.CONSONANTS.size)
        for (v in Keys.VOWELS) {
            assertFalse("consonants must not contain vowel $v", v in Keys.CONSONANTS)
        }
        for (c in Keys.CONSONANTS) {
            assertTrue("$c must be a real consonant", ItalianText.isConsonant(c))
        }
    }

    @Test fun consonantsAreAlphabeticalAndComplete() {
        assertEquals(Keys.CONSONANTS.sorted(), Keys.CONSONANTS)
        // Together vowels + consonants cover the whole A..Z alphabet.
        assertEquals(('A'..'Z').toList().sorted(), (Keys.CONSONANTS + Keys.VOWELS).sorted())
    }

    @Test fun consonantRowsChunkCorrectly() {
        val rows = Keys.consonantRows(6)
        assertEquals(4, rows.size) // 21 -> 6,6,6,3
        assertEquals(6, rows[0].size)
        assertEquals(6, rows[1].size)
        assertEquals(6, rows[2].size)
        assertEquals(3, rows[3].size)
        // Flattening the rows must reproduce the consonant list in order.
        assertEquals(Keys.CONSONANTS, rows.flatten())
    }

    @Test fun consonantRowsExactMultiple() {
        val rows = Keys.consonantRows(7)
        assertEquals(3, rows.size) // 21 -> 7,7,7
        assertTrue(rows.all { it.size == 7 })
    }
}
