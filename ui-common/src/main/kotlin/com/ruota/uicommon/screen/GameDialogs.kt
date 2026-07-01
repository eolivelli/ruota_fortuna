package com.ruota.uicommon.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ruota.core.text.ItalianText
import com.ruota.uicommon.keyboard.Keys

/** Lets the current player pick a vowel to buy; already-bought vowels are disabled. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VowelPickerDialog(
    guessed: Set<Char>,
    onPick: (Char) -> Unit,
    onDismiss: () -> Unit,
) {
    val normalized = guessed.map { ItalianText.normalizeChar(it) }.toSet()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Compra una vocale") },
        text = {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Keys.VOWELS.forEach { vowel ->
                    Button(
                        onClick = { onPick(vowel) },
                        enabled = ItalianText.normalizeChar(vowel) !in normalized,
                    ) {
                        Text(vowel.toString())
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } },
    )
}

/** Keyboard-mode solve: the player types the phrase; it is submitted verbatim. */
@Composable
fun SolveInputDialog(
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Risolvi il gioco") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Scrivi la frase") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = { Button(onClick = { onSubmit(text) }) { Text("Conferma") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } },
    )
}

/** Host-confirm solve: reveals the solution and asks the group to judge it. */
@Composable
fun HostConfirmDialog(
    solution: String,
    onResult: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("La soluzione è:") },
        text = { Text(solution) },
        confirmButton = { Button(onClick = { onResult(true) }) { Text("Corretto") } },
        dismissButton = { OutlinedButton(onClick = { onResult(false) }) { Text("Sbagliato") } },
    )
}
