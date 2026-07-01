package com.ruota.uicommon.wheel

import kotlin.math.roundToInt

/**
 * Pure, Compose-free geometry for the wheel. All math lives here so it can be unit-tested
 * without touching Android.
 *
 * ## Convention
 * - Wedges are laid out clockwise. At rotation `0f`, the centre of wedge `0` sits directly
 *   under the fixed pointer at the top (12 o'clock); wedge `1`'s centre is one [sweepDegrees]
 *   clockwise from it, and so on.
 * - `rotationDegrees` is the wheel's clockwise rotation: increasing it spins the wheel
 *   clockwise, which moves *lower* indices away from the pointer and brings the wedge whose
 *   centre angle equals `-rotationDegrees` (mod 360) under the pointer.
 * - Positive and negative rotations, and values well beyond +/-360, are all valid.
 */
object WheelGeometry {

    /** The angular width of a single wedge, in degrees. */
    fun sweepDegrees(wedgeCount: Int): Float {
        require(wedgeCount > 0) { "wedgeCount must be positive" }
        return 360f / wedgeCount
    }

    /**
     * The index of the wedge currently under the fixed top pointer, given the wheel's
     * [rotationDegrees]. Handles any rotation value (negative, or many full turns).
     */
    fun wedgeAtPointer(rotationDegrees: Float, wedgeCount: Int): Int {
        require(wedgeCount > 0) { "wedgeCount must be positive" }
        val sweep = sweepDegrees(wedgeCount)
        // Wedge i's centre is under the pointer when i * sweep == -rotation (mod 360).
        val raw = (-rotationDegrees / sweep).roundToInt()
        return ((raw % wedgeCount) + wedgeCount) % wedgeCount
    }

    /**
     * The absolute rotation to animate *to* so that [wedgeAtPointer] of the settled wheel
     * equals [targetIndex], after [fullSpins] complete clockwise revolutions.
     *
     * Invariant: `wedgeAtPointer(targetRotationDegrees(i, n, k), n) == i`.
     */
    fun targetRotationDegrees(targetIndex: Int, wedgeCount: Int, fullSpins: Int): Float {
        require(wedgeCount > 0) { "wedgeCount must be positive" }
        require(targetIndex in 0 until wedgeCount) { "targetIndex out of range" }
        require(fullSpins >= 0) { "fullSpins must be non-negative" }
        return 360f * fullSpins - targetIndex * sweepDegrees(wedgeCount)
    }
}
