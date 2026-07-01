package com.ruota.uicommon.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruota.core.engine.ActionType
import com.ruota.core.engine.GameState
import com.ruota.core.engine.TurnPhase
import com.ruota.core.model.SolveMode
import com.ruota.core.model.WheelWedge
import com.ruota.uicommon.board.PuzzleBoard
import com.ruota.uicommon.format.formatMoney
import com.ruota.uicommon.game.GameViewModel
import com.ruota.uicommon.keyboard.Keys
import com.ruota.uicommon.keyboard.LetterKeyboard
import com.ruota.uicommon.theme.RuotaColors
import com.ruota.uicommon.wheel.WheelOfFortune

/**
 * The full in-game screen: player standings, board, wheel, and the context-sensitive
 * action controls (spin / call consonant / buy vowel / solve / advance). Built on
 * Material3 components, which are focusable and therefore drivable by a TV D-pad.
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    wedges: List<WheelWedge>,
    peekSolution: () -> String,
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val lastOutcome by viewModel.lastOutcome.collectAsState()

    var spinning by remember { mutableStateOf(false) }
    var spinTarget by remember { mutableStateOf<Int?>(null) }
    var showVowelPicker by remember { mutableStateOf(false) }
    var showSolveInput by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RuotaColors.Background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Header(state)
        PlayerBar(state)

        PuzzleBoard(
            category = state.category,
            tiles = state.tiles,
            modifier = Modifier.fillMaxWidth(),
        )

        outcomeMessage(lastOutcome)?.let { msg ->
            Text(
                text = msg,
                color = RuotaColors.Accent,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        WheelOfFortune(
            wedges = wedges,
            targetIndex = spinTarget,
            isSpinning = spinning,
            onSpinSettled = { spinning = false },
            modifier = Modifier.size(300.dp),
        )

        when (state.phase) {
            TurnPhase.GAME_OVER -> GameOverPanel(state, onNewGame)
            TurnPhase.ROUND_OVER -> RoundOverPanel(state) { viewModel.advanceRound() }
            else -> {
                if (spinning) {
                    Text("La ruota gira…", color = RuotaColors.OnSurface, fontSize = 18.sp)
                } else {
                    ActionControls(
                        state = state,
                        onSpin = {
                            viewModel.spin()
                            spinTarget = spinIndexOf(viewModel.lastOutcome.value)
                            if (spinTarget != null) spinning = true
                        },
                        onGuessConsonant = { viewModel.guessConsonant(it) },
                        onBuyVowel = { showVowelPicker = true },
                        onSolve = { showSolveInput = true },
                    )
                }
            }
        }
    }

    if (showVowelPicker) {
        VowelPickerDialog(
            guessed = state.guessedLetters,
            onPick = { viewModel.buyVowel(it); showVowelPicker = false },
            onDismiss = { showVowelPicker = false },
        )
    }

    if (showSolveInput) {
        if (state.config.solveMode == SolveMode.HOST_CONFIRM) {
            HostConfirmDialog(
                solution = peekSolution(),
                onResult = { viewModel.hostConfirm(it); showSolveInput = false },
                onDismiss = { showSolveInput = false },
            )
        } else {
            SolveInputDialog(
                onSubmit = { viewModel.attemptSolve(it); showSolveInput = false },
                onDismiss = { showSolveInput = false },
            )
        }
    }
}

@Composable
private fun Header(state: GameState) {
    Text(
        text = "Round ${state.roundNumber} / ${state.config.numRounds}",
        color = RuotaColors.Accent,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun PlayerBar(state: GameState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        state.players.forEachIndexed { index, player ->
            val isCurrent = index == state.currentPlayerIndex && !state.isOver
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) RuotaColors.AccentDark else RuotaColors.Surface,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = player.name,
                        color = Color.White,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 15.sp,
                    )
                    Text("Round: ${formatMoney(player.roundTotal)}", color = Color.White, fontSize = 13.sp)
                    Text("Totale: ${formatMoney(player.grandTotal)}", color = RuotaColors.Accent, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ActionControls(
    state: GameState,
    onSpin: () -> Unit,
    onGuessConsonant: (Char) -> Unit,
    onBuyVowel: () -> Unit,
    onSolve: () -> Unit,
) {
    val actions = state.availableActions
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (state.phase == TurnPhase.AWAITING_CONSONANT) {
            Text("${state.currentPlayer.name}: scegli una consonante", color = Color.White, fontSize = 16.sp)
            LetterKeyboard(
                guessed = state.guessedLetters,
                onLetter = onGuessConsonant,
                showVowels = false,
                vowelsEnabled = false,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedButton(onClick = onSolve) { Text("Risolvi") }
        } else {
            Text("Tocca a ${state.currentPlayer.name}", color = Color.White, fontSize = 16.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSpin, enabled = ActionType.SPIN in actions) {
                    Text("Gira la ruota")
                }
                Button(onClick = onBuyVowel, enabled = ActionType.BUY_VOWEL in actions) {
                    Text("Compra vocale (${formatMoney(state.config.vowelCost)})")
                }
            }
            OutlinedButton(onClick = onSolve, enabled = ActionType.SOLVE in actions) {
                Text("Risolvi il gioco")
            }
        }
    }
}

@Composable
private fun RoundOverPanel(state: GameState, onNext: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Soluzione: ${state.solutionText}", color = RuotaColors.Accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Button(onClick = onNext) { Text("Prossimo round") }
    }
}

@Composable
private fun GameOverPanel(state: GameState, onNewGame: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val names = state.winners.joinToString(" e ") { it.name }
        Text("🏆 Vince $names!", color = RuotaColors.Accent, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        state.players.sortedByDescending { it.grandTotal }.forEach {
            Text("${it.name}: ${formatMoney(it.grandTotal)}", color = Color.White, fontSize = 16.sp)
        }
        Button(onClick = onNewGame) { Text("Nuova partita") }
    }
}
