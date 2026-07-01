package com.ruota.core.puzzle

import com.ruota.core.model.Puzzle
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The JSON schema for a bundled phrase pack. See `assets/phrases_it.json` and the
 * documentation in README for how to add your own phrases.
 *
 * ```json
 * {
 *   "language": "it",
 *   "phrases": [
 *     { "category": "Proverbio", "text": "In bocca al lupo" }
 *   ]
 * }
 * ```
 */
@Serializable
data class PhrasePack(
    @SerialName("language") val language: String = "it",
    @SerialName("phrases") val phrases: List<PhraseEntry> = emptyList(),
)

@Serializable
data class PhraseEntry(
    @SerialName("category") val category: String,
    @SerialName("text") val text: String,
)

/** Parses phrase packs from JSON and converts them to [Puzzle]s. Pure Kotlin, JVM-testable. */
object PhraseParser {

    private val json = Json { ignoreUnknownKeys = true }

    /** Parses a phrase-pack JSON string into a [PhrasePack]. Throws on malformed JSON. */
    fun parse(jsonText: String): PhrasePack = json.decodeFromString(PhrasePack.serializer(), jsonText)

    /**
     * Parses a phrase pack and returns valid [Puzzle]s, skipping entries whose text has
     * no letters (which could never be played). Throws if no usable puzzle remains.
     */
    fun parsePuzzles(jsonText: String): List<Puzzle> {
        val puzzles = parse(jsonText).phrases
            .filter { it.text.any(Char::isLetter) }
            .map { Puzzle(category = it.category.trim(), text = it.text.trim()) }
        require(puzzles.isNotEmpty()) { "Phrase pack contains no usable puzzles" }
        return puzzles
    }
}
