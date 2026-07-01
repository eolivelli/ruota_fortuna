package com.ruota.uicommon.puzzle

import android.content.Context
import com.ruota.core.model.Puzzle
import com.ruota.core.puzzle.PhraseParser

/** Loads the bundled Italian phrase pack from app assets and parses it into puzzles. */
object AssetPhrases {

    const val DEFAULT_ASSET: String = "phrases_it.json"

    /**
     * Reads [assetName] from the APK assets and returns the playable puzzles.
     * Parsing/validation is delegated to the JVM-tested [PhraseParser].
     */
    fun load(context: Context, assetName: String = DEFAULT_ASSET): List<Puzzle> {
        val json = context.assets.open(assetName).bufferedReader().use { it.readText() }
        return PhraseParser.parsePuzzles(json)
    }
}
