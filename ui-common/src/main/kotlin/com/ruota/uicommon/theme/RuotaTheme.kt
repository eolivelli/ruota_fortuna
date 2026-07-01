package com.ruota.uicommon.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Theme-neutral palette and dimensions shared by both the mobile and TV apps.
 * The shared Composables (wheel, board, keyboard) are built on Compose foundation
 * primitives and take colors from here, so they render identically under Material3
 * (mobile) and tv-material (TV).
 */
object RuotaColors {
    val Background = Color(0xFF0E240F)
    val Surface = Color(0xFF1B5E20)
    val SurfaceVariant = Color(0xFF2E7D32)
    val OnSurface = Color(0xFFFFFFFF)
    val Accent = Color(0xFFFFEB3B) // gold
    val AccentDark = Color(0xFFF9A825)
    val TileEmpty = Color(0xFF14481A) // background-color tiles = spaces between words
    val TileBlank = Color(0xFFFFFFFF) // unrevealed letter tile
    val TileText = Color(0xFF0E240F)
    val Bankrupt = Color(0xFF000000)
    val BankruptText = Color(0xFFFFFFFF)
    val LoseATurn = Color(0xFFB71C1C)
    val Focus = Color(0xFFFFF59D)

    /** A repeating set of wedge fills used to color the wheel. */
    val WheelPalette: List<Color> = listOf(
        Color(0xFF1B5E20), Color(0xFFFFEB3B), Color(0xFFC62828), Color(0xFF1565C0),
        Color(0xFFEF6C00), Color(0xFF6A1B9A), Color(0xFF00838F), Color(0xFFAD1457),
    )
}

object RuotaDimens {
    val TileSize = 40.dp
    val TileSpacing = 4.dp
    val KeyMinSize = 44.dp
    val ScreenPadding = 16.dp
}
