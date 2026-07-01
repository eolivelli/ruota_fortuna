package com.ruota.core.wheel

import com.ruota.core.model.WheelWedge
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class WheelTest {

    @Test fun spinLandsOnWedgeAtReturnedIndex() {
        val wheel = Wheel(
            listOf(WheelWedge.Cash(100), WheelWedge.Bankrupt, WheelWedge.Cash(300)),
            Random(42),
        )
        repeat(50) {
            val r = wheel.spin()
            assertEquals(wheel.wedges[r.index], r.wedge)
        }
    }

    @Test fun singleWedgeWheelIsDeterministic() {
        val wheel = Wheel(listOf(WheelWedge.Bankrupt))
        assertEquals(WheelWedge.Bankrupt, wheel.spin().wedge)
    }

    @Test fun standardWheelHasClassicLayout() {
        val wheel = Wheel.standard()
        assertEquals(24, wheel.wedges.size)
        assertEquals(2, wheel.wedges.count { it is WheelWedge.Bankrupt })
        assertEquals(1, wheel.wedges.count { it is WheelWedge.LoseATurn })
        assertTrue(wheel.wedges.any { it is WheelWedge.Cash && it.amount == 1000 })
    }

    @Test fun emptyWheelRejected() {
        assertFailsWith<IllegalArgumentException> { Wheel(emptyList()) }
    }
}
