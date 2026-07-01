package com.ruota.core.puzzle

import com.ruota.core.model.Puzzle
import kotlin.random.Random

/** Supplies puzzles to the engine, one per round. */
fun interface PuzzleProvider {
    /** Returns the next puzzle to play. */
    fun next(): Puzzle
}

/**
 * Draws puzzles at random from a fixed pool without repeating until the pool is
 * exhausted, then reshuffles. Deterministic when given a seeded [random].
 */
class ShufflingPuzzleProvider(
    puzzles: List<Puzzle>,
    private val random: Random = Random.Default,
) : PuzzleProvider {

    init {
        require(puzzles.isNotEmpty()) { "Puzzle pool must not be empty" }
    }

    private val pool: List<Puzzle> = puzzles.toList()
    private val remaining: ArrayDeque<Puzzle> = ArrayDeque()

    override fun next(): Puzzle {
        if (remaining.isEmpty()) remaining.addAll(pool.shuffled(random))
        return remaining.removeFirst()
    }
}
