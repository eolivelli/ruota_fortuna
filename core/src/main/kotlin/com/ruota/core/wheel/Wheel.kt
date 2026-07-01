package com.ruota.core.wheel

import com.ruota.core.model.WheelWedge
import kotlin.random.Random

/**
 * The wheel: an ordered ring of wedges. [spin] picks one uniformly at random using the
 * injected [random], so tests can force a specific landing with a seeded generator.
 */
class Wheel(
    val wedges: List<WheelWedge>,
    private val random: Random = Random.Default,
) {
    init {
        require(wedges.isNotEmpty()) { "Wheel must have at least one wedge" }
    }

    /** The index that will be landed on next is chosen here; returns that wedge. */
    fun spin(): SpinResult {
        val index = random.nextInt(wedges.size)
        return SpinResult(index, wedges[index])
    }

    companion object {
        /**
         * A standard 24-wedge board: a spread of cash values plus two Bankrupt wedges
         * and one Lose-a-Turn, mirroring a classic Wheel of Fortune layout.
         */
        fun standard(random: Random = Random.Default): Wheel = Wheel(
            listOf(
                WheelWedge.Cash(500),
                WheelWedge.Cash(900),
                WheelWedge.Cash(700),
                WheelWedge.Cash(300),
                WheelWedge.Cash(600),
                WheelWedge.Cash(350),
                WheelWedge.Bankrupt,
                WheelWedge.Cash(800),
                WheelWedge.Cash(500),
                WheelWedge.Cash(450),
                WheelWedge.Cash(700),
                WheelWedge.LoseATurn,
                WheelWedge.Cash(600),
                WheelWedge.Cash(550),
                WheelWedge.Cash(500),
                WheelWedge.Cash(900),
                WheelWedge.Cash(650),
                WheelWedge.Cash(400),
                WheelWedge.Bankrupt,
                WheelWedge.Cash(1000),
                WheelWedge.Cash(600),
                WheelWedge.Cash(500),
                WheelWedge.Cash(800),
                WheelWedge.Cash(650),
            ),
            random,
        )
    }
}

/** The outcome of a spin: the landed wedge and its index on the ring (for UI animation). */
data class SpinResult(val index: Int, val wedge: WheelWedge)
