package com.ruota.uicommon.puzzle

import com.ruota.core.puzzle.PhraseParser
import com.ruota.core.text.ItalianText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Validates the actual bundled phrase pack (not an inline fixture): it parses, is large,
 * and every entry is playable and unique. Guards against a malformed edit to the JSON.
 */
class PhrasePackAssetTest {

    private fun loadJson(): String {
        // Unit tests run with the module dir as the working directory.
        val candidates = listOf(
            File("src/main/assets/phrases_it.json"),
            File("ui-common/src/main/assets/phrases_it.json"),
        )
        val file = candidates.firstOrNull { it.exists() }
            ?: error("phrases_it.json not found (cwd=${File(".").absolutePath})")
        return file.readText()
    }

    @Test fun bundledPackIsLargeValidAndUnique() {
        val puzzles = PhraseParser.parsePuzzles(loadJson())

        assertTrue("expected >= 100 phrases, got ${puzzles.size}", puzzles.size >= 100)
        assertTrue("all entries must have category + text", puzzles.all {
            it.category.isNotBlank() && it.text.isNotBlank()
        })
        assertTrue("every phrase must contain playable letters", puzzles.all { p ->
            p.text.any { ItalianText.isLetter(it) }
        })

        val normalized = puzzles.map { ItalianText.normalizeForCompare(it.text) }
        assertEquals("phrases must be unique", normalized.size, normalized.toSet().size)
    }
}
