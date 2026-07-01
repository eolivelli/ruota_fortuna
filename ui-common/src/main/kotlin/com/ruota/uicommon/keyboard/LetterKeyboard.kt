package com.ruota.uicommon.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruota.core.text.ItalianText
import com.ruota.uicommon.theme.RuotaColors
import com.ruota.uicommon.theme.RuotaDimens

/**
 * On-screen letter keyboard usable with both touch and a D-pad remote.
 *
 * Consonants are laid out in a grid; when [showVowels] is true a separate vowel row is shown
 * and its keys are interactive only when [vowelsEnabled]. Letters already present in [guessed]
 * (matched on their normalized form) are dimmed and cannot be focused or pressed. On first
 * composition focus lands on the first still-available key so a remote user can start typing
 * immediately.
 */
@Composable
fun LetterKeyboard(
    guessed: Set<Char>,
    onLetter: (Char) -> Unit,
    showVowels: Boolean,
    vowelsEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val normalizedGuessed = remember(guessed) { guessed.map { ItalianText.normalizeChar(it) }.toSet() }
    fun isGuessed(c: Char) = ItalianText.normalizeChar(c) in normalizedGuessed

    val firstFocus = remember { FocusRequester() }
    // The first key that should receive initial focus: earliest un-guessed consonant, else the
    // first un-guessed vowel (only if the vowel row is shown and enabled).
    val firstFocusable: Char? = remember(normalizedGuessed, showVowels, vowelsEnabled) {
        Keys.CONSONANTS.firstOrNull { !isGuessed(it) }
            ?: if (showVowels && vowelsEnabled) Keys.VOWELS.firstOrNull { !isGuessed(it) } else null
    }

    LaunchedEffect(firstFocusable) {
        if (firstFocusable != null) {
            runCatching { firstFocus.requestFocus() }
        }
    }

    val perRow = 6
    Column(
        modifier = modifier.padding(RuotaDimens.TileSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RuotaDimens.TileSpacing),
    ) {
        for (row in Keys.consonantRows(perRow)) {
            Row(horizontalArrangement = Arrangement.spacedBy(RuotaDimens.TileSpacing)) {
                for (letter in row) {
                    LetterKey(
                        char = letter,
                        enabled = !isGuessed(letter),
                        onLetter = onLetter,
                        focusRequester = if (letter == firstFocusable) firstFocus else null,
                    )
                }
            }
        }

        if (showVowels) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(RuotaDimens.TileSpacing),
                modifier = Modifier.padding(top = RuotaDimens.TileSpacing),
            ) {
                for (vowel in Keys.VOWELS) {
                    LetterKey(
                        char = vowel,
                        enabled = vowelsEnabled && !isGuessed(vowel),
                        onLetter = onLetter,
                        focusRequester = if (vowel == firstFocusable) firstFocus else null,
                    )
                }
            }
        }
    }
}

@Composable
private fun LetterKey(
    char: Char,
    enabled: Boolean,
    onLetter: (Char) -> Unit,
    focusRequester: FocusRequester?,
) {
    var focused by remember { mutableStateOf(false) }

    val background = when {
        !enabled -> RuotaColors.TileEmpty
        focused -> RuotaColors.Accent
        else -> RuotaColors.Surface
    }
    val textColor = if (enabled) RuotaColors.OnSurface else RuotaColors.OnSurface.copy(alpha = 0.35f)
    val borderColor = if (focused && enabled) RuotaColors.Focus else Color.Transparent

    var mod: Modifier = Modifier
        .sizeIn(minWidth = RuotaDimens.KeyMinSize, minHeight = RuotaDimens.KeyMinSize)
        .clip(RoundedCornerShape(8.dp))
        .background(background)
        .border(2.dp, borderColor, RoundedCornerShape(8.dp))

    if (enabled) {
        if (focusRequester != null) mod = mod.focusRequester(focusRequester)
        mod = mod
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp &&
                    (event.key == Key.Enter ||
                        event.key == Key.NumPadEnter ||
                        event.key == Key.DirectionCenter)
                ) {
                    onLetter(char)
                    true
                } else {
                    false
                }
            }
            .clickable { onLetter(char) }
    }

    Box(
        modifier = mod.padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = char.toString(),
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LetterKeyboardPreview() {
    LetterKeyboard(
        guessed = setOf('R', 'S', 'T', 'A'),
        onLetter = {},
        showVowels = true,
        vowelsEnabled = true,
    )
}
