package com.ruota.uicommon.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ruota.core.model.GameConfig
import com.ruota.core.model.SolveMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** DataStore-backed instance used by the [Context.gamePrefsDataStore] extension. */
private const val PREFS_NAME = "ruota_game_prefs"

/** A process-wide [DataStore] for the game preferences, keyed off the application context. */
val Context.gamePrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = PREFS_NAME)

/** Preference key definitions and defaults, kept private to this module. */
internal object PrefKeys {
    val NUM_ROUNDS = intPreferencesKey("num_rounds")
    val VOWEL_COST_DOLLARS = intPreferencesKey("vowel_cost_dollars")
    val SOLVE_MODE = stringPreferencesKey("solve_mode")

    const val DEFAULT_NUM_ROUNDS = 4
    const val DEFAULT_VOWEL_COST = 250
    const val DEFAULT_SOLVE_MODE = "KEYBOARD"
}

/**
 * Persists [GameConfig] settings using DataStore Preferences. Construct with an existing
 * [DataStore] (e.g. `context.gamePrefsDataStore`), which keeps the class unit-testable.
 */
class PreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) {
    /** Convenience constructor that uses the app-wide [Context.gamePrefsDataStore]. */
    constructor(context: Context) : this(context.applicationContext.gamePrefsDataStore)

    /** The stored preferences mapped into an immutable [GameConfig]. */
    val config: Flow<GameConfig> = dataStore.data.map { prefs ->
        GameConfig(
            numRounds = prefs[PrefKeys.NUM_ROUNDS] ?: PrefKeys.DEFAULT_NUM_ROUNDS,
            vowelCost = prefs[PrefKeys.VOWEL_COST_DOLLARS] ?: PrefKeys.DEFAULT_VOWEL_COST,
            solveMode = parseSolveMode(prefs[PrefKeys.SOLVE_MODE]),
        )
    }

    suspend fun setNumRounds(numRounds: Int) {
        dataStore.edit { it[PrefKeys.NUM_ROUNDS] = numRounds }
    }

    suspend fun setVowelCost(dollars: Int) {
        dataStore.edit { it[PrefKeys.VOWEL_COST_DOLLARS] = dollars }
    }

    suspend fun setSolveMode(mode: SolveMode) {
        dataStore.edit { it[PrefKeys.SOLVE_MODE] = mode.name }
    }

    private fun parseSolveMode(raw: String?): SolveMode =
        when (raw) {
            SolveMode.HOST_CONFIRM.name -> SolveMode.HOST_CONFIRM
            else -> SolveMode.KEYBOARD
        }
}
