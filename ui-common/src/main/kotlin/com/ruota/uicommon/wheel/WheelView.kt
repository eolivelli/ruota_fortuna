package com.ruota.uicommon.wheel

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ruota.core.model.WheelWedge
import com.ruota.core.wheel.Wheel
import com.ruota.uicommon.format.formatMoney
import com.ruota.uicommon.theme.RuotaColors
import kotlin.math.min

/**
 * The spinning Wheel of Fortune.
 *
 * When [isSpinning] flips to `true` with a non-null [targetIndex], the wheel animates from its
 * current rotation to [WheelGeometry.targetRotationDegrees] (with [SPIN_FULL_SPINS] full turns)
 * using a decelerating ease over [SPIN_DURATION_MS], then invokes [onSpinSettled] with the
 * settled index.
 *
 * @param wedges the ring to draw, laid out clockwise from the top pointer.
 * @param targetIndex the index to land on when a spin is requested, or `null` when idle.
 * @param isSpinning whether a spin is currently requested.
 * @param onSpinSettled called once the animation finishes, with the landed index.
 */
@Composable
fun WheelOfFortune(
    wedges: List<WheelWedge>,
    targetIndex: Int?,
    isSpinning: Boolean,
    onSpinSettled: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation = remember { Animatable(0f) }
    val currentOnSettled by rememberUpdatedState(onSpinSettled)

    // Key on the (isSpinning, targetIndex) request so each fresh spin animates exactly once.
    LaunchedEffect(isSpinning, targetIndex) {
        if (isSpinning && targetIndex != null && wedges.isNotEmpty()) {
            val current = rotation.value
            var target = WheelGeometry.targetRotationDegrees(
                targetIndex = targetIndex,
                wedgeCount = wedges.size,
                fullSpins = SPIN_FULL_SPINS,
            )
            // Always spin forward from wherever the wheel currently rests.
            while (target <= current) target += 360f
            rotation.animateTo(
                targetValue = target,
                animationSpec = tween(
                    durationMillis = SPIN_DURATION_MS,
                    easing = DecelerateEasing,
                ),
            )
            currentOnSettled(WheelGeometry.wedgeAtPointer(rotation.value, wedges.size))
        }
    }

    Box(modifier = modifier.fillMaxWidth().aspectRatio(1f)) {
        Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            drawWheel(wedges, rotation.value)
            drawPointer()
        }
    }
}

/** Draws every wedge (fill, divider and label) rotated by [rotationDegrees] about the centre. */
private fun DrawScope.drawWheel(wedges: List<WheelWedge>, rotationDegrees: Float) {
    if (wedges.isEmpty()) return
    val n = wedges.size
    val sweep = WheelGeometry.sweepDegrees(n)
    val diameter = min(size.width, size.height)
    val radius = diameter / 2f
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val topLeft = Offset(centerX - radius, centerY - radius)
    val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

    rotate(degrees = rotationDegrees, pivot = Offset(centerX, centerY)) {
        wedges.forEachIndexed { i, wedge ->
            // Wedge i is centred at the top (Compose -90 deg) at rest; it spans +/- sweep/2.
            val startAngle = -90f + i * sweep - sweep / 2f
            drawArc(
                color = fillColorFor(wedge, i),
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = topLeft,
                size = arcSize,
                style = Fill,
            )
            drawArc(
                color = Color.White.copy(alpha = 0.35f),
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = 2f),
            )
        }
        // Labels drawn in a second pass so dividers never cross the text.
        drawLabels(wedges, sweep, centerX, centerY, radius)
    }

    // Hub cap.
    drawCircle(
        color = RuotaColors.Accent,
        radius = radius * 0.10f,
        center = Offset(centerX, centerY),
    )
    drawCircle(
        color = RuotaColors.AccentDark,
        radius = radius * 0.10f,
        center = Offset(centerX, centerY),
        style = Stroke(width = 3f),
    )
}

private fun DrawScope.drawLabels(
    wedges: List<WheelWedge>,
    sweep: Float,
    centerX: Float,
    centerY: Float,
    radius: Float,
) {
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        textSize = radius * 0.09f
        isFakeBoldText = true
    }
    val canvas = drawContext.canvas.nativeCanvas
    wedges.forEachIndexed { i, wedge ->
        val centerAngleFromTop = i * sweep
        val label = labelFor(wedge)
        paint.color = labelColorFor(wedge).toArgb()
        canvas.save()
        canvas.translate(centerX, centerY)
        // Make the local +X axis point along this wedge's bisector so text reads radially.
        canvas.rotate(centerAngleFromTop - 90f)
        val x = radius * 0.62f
        val y = -(paint.ascent() + paint.descent()) / 2f
        canvas.drawText(label, x, y, paint)
        canvas.restore()
    }
}

/** The fixed pointer triangle at 12 o'clock; drawn outside the rotation so it stays put. */
private fun DrawScope.drawPointer() {
    val diameter = min(size.width, size.height)
    val radius = diameter / 2f
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val top = centerY - radius
    val w = radius * 0.09f
    val h = radius * 0.14f
    val path = Path().apply {
        moveTo(centerX, top + h)      // tip points inward toward the wheel
        lineTo(centerX - w, top - h * 0.4f)
        lineTo(centerX + w, top - h * 0.4f)
        close()
    }
    drawPath(path, color = RuotaColors.Accent)
    drawPath(path, color = RuotaColors.AccentDark, style = Stroke(width = 3f))
}

private fun fillColorFor(wedge: WheelWedge, index: Int): Color = when (wedge) {
    is WheelWedge.Cash -> RuotaColors.WheelPalette[index % RuotaColors.WheelPalette.size]
    WheelWedge.Bankrupt -> RuotaColors.Bankrupt
    WheelWedge.LoseATurn -> RuotaColors.LoseATurn
}

private fun labelColorFor(wedge: WheelWedge): Color = when (wedge) {
    is WheelWedge.Cash -> Color.White
    WheelWedge.Bankrupt -> RuotaColors.BankruptText
    WheelWedge.LoseATurn -> Color.White
}

private fun labelFor(wedge: WheelWedge): String = when (wedge) {
    is WheelWedge.Cash -> formatMoney(wedge.amount)
    WheelWedge.Bankrupt -> "BANCAROTTA"
    WheelWedge.LoseATurn -> "PASSA"
}

/** Number of full revolutions before a spin settles. */
const val SPIN_FULL_SPINS: Int = 5

/** Spin animation duration, in milliseconds (~3.5s). */
const val SPIN_DURATION_MS: Int = 3500

/** A strong ease-out so the wheel decelerates as it settles. */
private val DecelerateEasing = CubicBezierEasing(0.1f, 0.85f, 0.15f, 1f)

@Preview(showBackground = true, widthDp = 320, heightDp = 320, backgroundColor = 0xFF0E240F)
@Composable
private fun WheelOfFortunePreview() {
    WheelOfFortune(
        wedges = Wheel.standard().wedges,
        targetIndex = null,
        isSpinning = false,
        onSpinSettled = {},
    )
}
