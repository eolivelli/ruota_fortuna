package com.ruota.uicommon.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruota.core.board.Tile
import com.ruota.uicommon.theme.RuotaColors
import com.ruota.uicommon.theme.RuotaDimens

/**
 * Renders the masked puzzle board: a category label on top and the puzzle text laid out as
 * word-wrapping rows of tiles. Whole words stay together thanks to [FlowRow] wrapping on the
 * word groups produced by [BoardLayout.groupIntoWords].
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PuzzleBoard(
    category: String,
    tiles: List<Tile>,
    modifier: Modifier = Modifier,
) {
    val words = BoardLayout.groupIntoWords(tiles)
    Box(
        modifier = modifier
            .background(RuotaColors.Background)
            .padding(RuotaDimens.ScreenPadding),
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = category.uppercase(),
                color = RuotaColors.Accent,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = RuotaDimens.ScreenPadding),
            )
            // Bigger gap between words; tiles inside a word use the smaller spacing.
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(RuotaDimens.TileSpacing * 3),
                verticalArrangement = Arrangement.spacedBy(RuotaDimens.TileSpacing * 2),
            ) {
                for (word in words) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(RuotaDimens.TileSpacing),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        for (tile in word) {
                            when (tile) {
                                is Tile.Letter -> LetterTile(tile)
                                is Tile.Symbol -> SymbolTile(tile.display)
                                Tile.Space -> Unit // spaces are separators, never inside a word
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LetterTile(tile: Tile.Letter) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(RuotaDimens.TileSize)
            .clip(RoundedCornerShape(6.dp))
            .background(RuotaColors.TileBlank),
    ) {
        if (tile.revealed) {
            Text(
                text = tile.display.toString(),
                color = RuotaColors.TileText,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            )
        }
    }
}

@Composable
private fun SymbolTile(display: Char) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(width = RuotaDimens.TileSize / 2, height = RuotaDimens.TileSize),
    ) {
        Text(
            text = display.toString(),
            color = RuotaColors.OnSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PuzzleBoardPreview() {
    // "LA RUOTA DELLA FORTUNA" with only some letters revealed.
    fun word(text: String, revealed: Set<Char>): List<Tile> =
        text.map { ch -> Tile.Letter(ch, ch in revealed) }

    val revealed = setOf('A', 'O')
    val tiles = buildList {
        addAll(word("LA", revealed)); add(Tile.Space)
        addAll(word("RUOTA", revealed)); add(Tile.Space)
        addAll(word("DELLA", revealed)); add(Tile.Space)
        addAll(word("FORTUNA", revealed))
    }
    PuzzleBoard(category = "Modo di dire", tiles = tiles)
}
