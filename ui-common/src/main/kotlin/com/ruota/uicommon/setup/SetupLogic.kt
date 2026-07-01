package com.ruota.uicommon.setup

/** Pure, testable helpers for the player-setup screen. */
object SetupLogic {

    const val MIN_PLAYERS = 1
    const val MAX_PLAYERS = 4
    const val MIN_ROUNDS = 1
    const val MAX_ROUNDS = 12

    /** Default player names, e.g. "Giocatore 1".."Giocatore 4". [count] is clamped to 1..4. */
    fun defaultPlayerNames(count: Int): List<String> {
        val n = clampPlayers(count)
        return (1..n).map { "Giocatore $it" }
    }

    /** Clamps a requested player count into the legal 1..4 range. */
    fun clampPlayers(count: Int): Int = count.coerceIn(MIN_PLAYERS, MAX_PLAYERS)

    /** Clamps a requested round count into the legal 1..12 range. */
    fun clampRounds(rounds: Int): Int = rounds.coerceIn(MIN_ROUNDS, MAX_ROUNDS)

    /**
     * Resizes a list of edited names to [count] entries, preserving existing (non-blank) names
     * and filling any new slots with defaults. Blank names fall back to their default.
     */
    fun resizeNames(current: List<String>, count: Int): List<String> {
        val n = clampPlayers(count)
        val defaults = defaultPlayerNames(n)
        return (0 until n).map { i ->
            val existing = current.getOrNull(i)?.trim()
            if (existing.isNullOrEmpty()) defaults[i] else existing
        }
    }

    /** The names actually submitted: trimmed, with blanks replaced by their default. */
    fun finalizeNames(current: List<String>, count: Int): List<String> =
        resizeNames(current, count)
}
