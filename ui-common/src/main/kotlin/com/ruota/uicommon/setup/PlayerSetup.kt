package com.ruota.uicommon.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruota.core.model.GameConfig
import com.ruota.core.model.SolveMode
import com.ruota.uicommon.theme.RuotaDimens

/**
 * Pass-and-play setup: pick 1..4 players and their names, the number of rounds, and the
 * solve mode, then start the game. Controls are focusable so the screen is fully usable
 * with a D-pad on TV as well as by touch on mobile.
 */
@Composable
fun PlayerSetup(
    initialConfig: GameConfig,
    onStart: (playerNames: List<String>, config: GameConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    var playerCount by remember { mutableStateOf(2) }
    val names = remember {
        mutableStateListOf<String>().apply { addAll(SetupLogic.defaultPlayerNames(4)) }
    }
    var numRounds by remember { mutableStateOf(initialConfig.numRounds) }
    var solveMode by remember { mutableStateOf(initialConfig.solveMode) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(RuotaDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "La Ruota Della Fortuna",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )

        // ---- Number of players ---------------------------------------------------
        Text("Giocatori", fontWeight = FontWeight.SemiBold)
        Stepper(
            label = "$playerCount",
            onDecrement = { playerCount = SetupLogic.clampPlayers(playerCount - 1) },
            onIncrement = { playerCount = SetupLogic.clampPlayers(playerCount + 1) },
        )

        // ---- Player names --------------------------------------------------------
        for (i in 0 until playerCount) {
            OutlinedTextField(
                value = names[i],
                onValueChange = { names[i] = it },
                label = { Text("Giocatore ${i + 1}") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusable(),
            )
        }

        HorizontalDivider()

        // ---- Rounds --------------------------------------------------------------
        Text("Round", fontWeight = FontWeight.SemiBold)
        Stepper(
            label = "$numRounds",
            onDecrement = { numRounds = SetupLogic.clampRounds(numRounds - 1) },
            onIncrement = { numRounds = SetupLogic.clampRounds(numRounds + 1) },
        )

        HorizontalDivider()

        // ---- Solve mode ----------------------------------------------------------
        Text("Modalità soluzione", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = solveMode == SolveMode.KEYBOARD,
                onClick = { solveMode = SolveMode.KEYBOARD },
                label = { Text("Tastiera") },
                modifier = Modifier.focusable(),
            )
            FilterChip(
                selected = solveMode == SolveMode.HOST_CONFIRM,
                onClick = { solveMode = SolveMode.HOST_CONFIRM },
                label = { Text("Conferma") },
                modifier = Modifier.focusable(),
            )
        }

        Spacer(Modifier.height(8.dp))

        // ---- Start ---------------------------------------------------------------
        Button(
            onClick = {
                val finalNames = SetupLogic.finalizeNames(names.toList(), playerCount)
                val config = GameConfig(
                    numRounds = SetupLogic.clampRounds(numRounds),
                    vowelCost = initialConfig.vowelCost,
                    solveMode = solveMode,
                )
                onStart(finalNames, config)
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusable(),
        ) {
            Text("GIOCA", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Stepper(
    label: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FilledTonalButton(onClick = onDecrement, modifier = Modifier.focusable()) {
            Text("-", fontSize = 20.sp)
        }
        Text(label, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        FilledTonalButton(onClick = onIncrement, modifier = Modifier.focusable()) {
            Text("+", fontSize = 20.sp)
        }
    }
}

@Preview
@Composable
private fun PlayerSetupPreview() {
    PlayerSetup(
        initialConfig = GameConfig(),
        onStart = { _, _ -> },
    )
}
