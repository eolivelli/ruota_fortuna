package com.ruota.uicommon.wheel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WheelGeometryTest {

    @Test
    fun sweepDegrees_dividesTheCircle() {
        assertEquals(15f, WheelGeometry.sweepDegrees(24), 1e-4f)
        assertEquals(90f, WheelGeometry.sweepDegrees(4), 1e-4f)
        assertEquals(360f, WheelGeometry.sweepDegrees(1), 1e-4f)
        assertEquals(360f / 7f, WheelGeometry.sweepDegrees(7), 1e-4f)
    }

    @Test
    fun wedgeAtPointer_atRestIsIndexZero() {
        for (n in intArrayOf(1, 4, 7, 13, 24)) {
            assertEquals(0, WheelGeometry.wedgeAtPointer(0f, n))
        }
    }

    @Test
    fun wedgeAtPointer_isPeriodicOver360() {
        // A full turn returns to the same wedge.
        assertEquals(
            WheelGeometry.wedgeAtPointer(37f, 24),
            WheelGeometry.wedgeAtPointer(37f + 360f, 24),
        )
        assertEquals(
            WheelGeometry.wedgeAtPointer(37f, 24),
            WheelGeometry.wedgeAtPointer(37f - 720f, 24),
        )
    }

    @Test
    fun wedgeAtPointer_handlesNegativeRotation() {
        // Rotating one sweep clockwise brings wedge that was one step CCW under pointer.
        val n = 24
        val sweep = WheelGeometry.sweepDegrees(n)
        // -sweep rotation => wedge 1 under pointer (per invariant with targetRotationDegrees).
        assertEquals(1, WheelGeometry.wedgeAtPointer(-sweep, n))
        assertEquals(23, WheelGeometry.wedgeAtPointer(sweep, n))
    }

    @Test
    fun geometryInvariant_holdsAcrossManyCombos() {
        val counts = intArrayOf(1, 2, 4, 7, 13, 24, 25)
        val spins = intArrayOf(0, 1, 5, 12)
        for (n in counts) {
            for (i in 0 until n) {
                for (k in spins) {
                    val rot = WheelGeometry.targetRotationDegrees(i, n, k)
                    assertEquals(
                        "n=$n i=$i k=$k rot=$rot",
                        i,
                        WheelGeometry.wedgeAtPointer(rot, n),
                    )
                }
            }
        }
    }

    @Test
    fun targetRotation_wrapAroundIndicesResolveCorrectly() {
        val n = 24
        // Index 0, last index, and a middle index all round-trip.
        for (i in intArrayOf(0, 1, 12, 22, 23)) {
            val rot = WheelGeometry.targetRotationDegrees(i, n, fullSpins = 5)
            assertEquals(i, WheelGeometry.wedgeAtPointer(rot, n))
        }
    }

    @Test
    fun targetRotation_includesRequestedFullSpins() {
        val rot0 = WheelGeometry.targetRotationDegrees(3, 24, fullSpins = 0)
        val rot5 = WheelGeometry.targetRotationDegrees(3, 24, fullSpins = 5)
        assertEquals(5f * 360f, rot5 - rot0, 1e-3f)
        assertTrue(rot5 > rot0)
    }
}
