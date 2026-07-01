package com.ruota.core.puzzle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PhraseParserTest {

    @Test fun parsesValidPack() {
        val json = """
            { "language": "it", "phrases": [
              { "category": "Proverbio", "text": "In bocca al lupo" },
              { "category": "Cosa", "text": "La macchina del caffè" }
            ] }
        """.trimIndent()
        val puzzles = PhraseParser.parsePuzzles(json)
        assertEquals(2, puzzles.size)
        assertEquals("Proverbio", puzzles[0].category)
        assertEquals("In bocca al lupo", puzzles[0].text)
    }

    @Test fun ignoresUnknownKeysAndTrims() {
        val json = """
            { "language": "it", "extra": 1, "phrases": [
              { "category": "  Cosa ", "text": "  Ciao  ", "hint": "x" }
            ] }
        """.trimIndent()
        val puzzles = PhraseParser.parsePuzzles(json)
        assertEquals("Cosa", puzzles[0].category)
        assertEquals("Ciao", puzzles[0].text)
    }

    @Test fun skipsTextWithoutLetters() {
        val json = """
            { "phrases": [
              { "category": "X", "text": "12345" },
              { "category": "Y", "text": "Ok" }
            ] }
        """.trimIndent()
        val puzzles = PhraseParser.parsePuzzles(json)
        assertEquals(1, puzzles.size)
        assertEquals("Ok", puzzles[0].text)
    }

    @Test fun emptyPackRejected() {
        assertFailsWith<IllegalArgumentException> {
            PhraseParser.parsePuzzles("""{ "phrases": [] }""")
        }
    }

    @Test fun malformedJsonThrows() {
        assertTrue(runCatching { PhraseParser.parse("{ not json") }.isFailure)
    }
}
