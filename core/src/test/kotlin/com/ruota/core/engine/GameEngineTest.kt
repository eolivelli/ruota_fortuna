package com.ruota.core.engine

import com.ruota.core.model.GameConfig
import com.ruota.core.model.Puzzle
import com.ruota.core.model.SolveMode
import com.ruota.core.model.WheelWedge
import com.ruota.core.puzzle.PuzzleProvider
import com.ruota.core.wheel.Wheel
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GameEngineTest {

    private fun engine(
        puzzleText: String = "CASA",
        category: String = "Cosa",
        wedges: List<WheelWedge> = listOf(WheelWedge.Cash(100)),
        players: List<String> = listOf("Ada", "Bob"),
        config: GameConfig = GameConfig(numRounds = 2),
        puzzles: List<Puzzle>? = null,
    ): GameEngine {
        val provider: PuzzleProvider = if (puzzles != null) {
            var i = 0
            PuzzleProvider { puzzles[i++ % puzzles.size] }
        } else {
            PuzzleProvider { Puzzle(category, puzzleText) }
        }
        return GameEngine(config, players, provider, Wheel(wedges, Random(0)))
    }

    // ---- Setup / validation ------------------------------------------------------

    @Test fun rejectsBadPlayerCount() {
        assertFailsWithMessage { engine(players = emptyList()) }
        assertFailsWithMessage { engine(players = listOf("a", "b", "c", "d", "e")) }
    }

    @Test fun initialStateIsMaskedAndAwaitingAction() {
        val s = engine().snapshot()
        assertEquals(TurnPhase.AWAITING_ACTION, s.phase)
        assertEquals(0, s.currentPlayerIndex)
        assertEquals(0, s.roundIndex)
        assertEquals("Ada", s.currentPlayer.name)
        assertTrue(s.availableActions.containsAll(setOf(ActionType.SPIN, ActionType.SOLVE)))
        assertNull(s.solutionText)
    }

    // ---- Spin --------------------------------------------------------------------

    @Test fun spinCashRequiresConsonantNext() {
        val e = engine()
        val out = e.spin()
        assertIs<Outcome.SpunCash>(out)
        assertEquals(100, out.amount)
        assertEquals(TurnPhase.AWAITING_CONSONANT, e.snapshot().phase)
        assertEquals(setOf(ActionType.GUESS_CONSONANT, ActionType.SOLVE), e.snapshot().availableActions)
    }

    @Test fun cannotSpinTwiceInARow() {
        val e = engine()
        e.spin()
        assertIs<Outcome.Invalid>(e.spin())
    }

    @Test fun bankruptClearsAccumulatedRoundMoney() {
        val e = engine(puzzleText = "CASA", wedges = listOf(WheelWedge.Bankrupt))
        val out = e.spin()
        assertIs<Outcome.SpunBankrupt>(out)
        assertEquals(0, e.snapshot().players[0].roundTotal)
        assertEquals(1, e.snapshot().currentPlayerIndex) // turn passed to Bob
    }

    @Test fun loseATurnPassesTurn() {
        val e = engine(wedges = listOf(WheelWedge.LoseATurn))
        val out = e.spin()
        assertIs<Outcome.SpunLoseATurn>(out)
        assertEquals(1, e.snapshot().currentPlayerIndex)
    }

    // ---- Consonants --------------------------------------------------------------

    @Test fun consonantHitEarnsAmountTimesCount() {
        val e = engine(puzzleText = "PEPE", wedges = listOf(WheelWedge.Cash(200)))
        e.spin()
        val out = e.guessConsonant('P') // two P's
        assertIs<Outcome.ConsonantHit>(out)
        assertEquals(2, out.count)
        assertEquals(400, out.earned)
        assertEquals(400, e.snapshot().players[0].roundTotal)
        assertEquals(TurnPhase.AWAITING_ACTION, e.snapshot().phase)
    }

    @Test fun consonantMissPassesTurn() {
        val e = engine(puzzleText = "CASA", wedges = listOf(WheelWedge.Cash(100)))
        e.spin()
        val out = e.guessConsonant('Z')
        assertIs<Outcome.ConsonantMiss>(out)
        assertEquals(1, e.snapshot().currentPlayerIndex)
        assertEquals(TurnPhase.AWAITING_ACTION, e.snapshot().phase)
    }

    @Test fun consonantRejectsVowelAlreadyUsedAndWrongPhase() {
        val e = engine(puzzleText = "CASA", wedges = listOf(WheelWedge.Cash(100)))
        assertIs<Outcome.Invalid>(e.guessConsonant('C')) // before spin
        e.spin()
        assertIs<Outcome.Invalid>(e.guessConsonant('A')) // vowel
        e.guessConsonant('C') // valid
        e.spin()
        assertIs<Outcome.Invalid>(e.guessConsonant('C')) // already called
    }

    // ---- Vowels ------------------------------------------------------------------

    @Test fun buyVowelDeductsCostAndReveals() {
        val e = engine(puzzleText = "CASA", wedges = listOf(WheelWedge.Cash(300)), config = GameConfig(numRounds = 2, vowelCost = 250))
        e.spin(); e.guessConsonant('C') // roundTotal 300
        val out = e.buyVowel('A')
        assertIs<Outcome.VowelBought>(out)
        assertEquals(2, out.count)
        assertEquals(50, e.snapshot().players[0].roundTotal) // 300 - 250
        assertTrue(e.snapshot().tiles.filterIsInstance<com.ruota.core.board.Tile.Letter>()
            .filter { it.display == 'A' }.all { it.revealed })
    }

    @Test fun buyVowelBlockedWhenTooPoor() {
        val e = engine(puzzleText = "CASA", config = GameConfig(numRounds = 2, vowelCost = 250))
        val out = e.buyVowel('A') // roundTotal 0
        assertIs<Outcome.Invalid>(out)
    }

    @Test fun buyVowelRejectsConsonant() {
        val e = engine(puzzleText = "CASA", wedges = listOf(WheelWedge.Cash(300)))
        e.spin(); e.guessConsonant('C')
        assertIs<Outcome.Invalid>(e.buyVowel('C'))
    }

    // ---- Solving -----------------------------------------------------------------

    @Test fun solveCorrectBanksRoundTotalAndAdvances() {
        val e = engine(
            puzzleText = "CASA",
            wedges = listOf(WheelWedge.Cash(100)),
            config = GameConfig(numRounds = 2),
        )
        e.spin(); e.guessConsonant('C') // roundTotal 100
        val out = e.attemptSolve("casa")
        assertIs<Outcome.SolvedCorrect>(out)
        assertEquals(100, out.awarded)
        assertEquals(100, e.snapshot().players[0].grandTotal)
        assertEquals(TurnPhase.ROUND_OVER, e.snapshot().phase)
        assertEquals("CASA", e.snapshot().solutionText)

        val next = e.advanceRound()
        assertIs<Outcome.RoundStarted>(next)
        assertEquals(1, e.snapshot().roundIndex)
        assertEquals(0, e.snapshot().players[0].roundTotal) // reset
        assertEquals(1, e.snapshot().currentPlayerIndex) // starting player rotates to Bob
    }

    @Test fun solveWrongPassesTurn() {
        val e = engine(puzzleText = "CASA", wedges = listOf(WheelWedge.Cash(100)))
        e.spin(); e.guessConsonant('C')
        val out = e.attemptSolve("cane")
        assertIs<Outcome.SolvedWrong>(out)
        assertEquals(1, e.snapshot().currentPlayerIndex)
        assertEquals(100, e.snapshot().players[0].roundTotal) // keeps money, just loses turn
    }

    @Test fun solveMatchesIgnoringAccentsAndSpaces() {
        val e = engine(puzzleText = "PERCHÉ NO", wedges = listOf(WheelWedge.Cash(100)))
        val out = e.attemptSolve("perche no")
        assertIs<Outcome.SolvedCorrect>(out)
    }

    @Test fun gameEndsAfterLastRoundWithWinner() {
        val e = engine(
            puzzleText = "CASA",
            wedges = listOf(WheelWedge.Cash(100)),
            config = GameConfig(numRounds = 1),
        )
        e.spin(); e.guessConsonant('C')
        val out = e.attemptSolve("casa")
        assertIs<Outcome.SolvedCorrect>(out)
        val s = e.snapshot()
        assertEquals(TurnPhase.GAME_OVER, s.phase)
        assertTrue(s.isOver)
        assertEquals(listOf("Ada"), s.winners.map { it.name })
    }

    @Test fun tieProducesMultipleWinners() {
        val e = engine(
            puzzleText = "CASA",
            wedges = listOf(WheelWedge.Cash(100)),
            config = GameConfig(numRounds = 1),
        )
        // Solve immediately with zero round total -> both players end at 0.
        val out = e.attemptSolve("casa")
        assertIs<Outcome.SolvedCorrect>(out)
        assertEquals(0, out.awarded)
        assertEquals(setOf("Ada", "Bob"), e.snapshot().winners.map { it.name }.toSet())
    }

    // ---- Host-confirm mode -------------------------------------------------------

    @Test fun hostConfirmModeGovernsSolveApi() {
        val e = engine(
            puzzleText = "CASA",
            wedges = listOf(WheelWedge.Cash(100)),
            config = GameConfig(numRounds = 1, solveMode = SolveMode.HOST_CONFIRM),
        )
        e.spin(); e.guessConsonant('C')
        assertIs<Outcome.Invalid>(e.attemptSolve("casa")) // keyboard API disabled
        val out = e.hostConfirm(true)
        assertIs<Outcome.SolvedCorrect>(out)
        assertEquals(100, out.awarded)
    }

    @Test fun keyboardModeRejectsHostConfirm() {
        val e = engine(config = GameConfig(numRounds = 1, solveMode = SolveMode.KEYBOARD))
        assertIs<Outcome.Invalid>(e.hostConfirm(true))
    }

    // ---- helpers -----------------------------------------------------------------

    private fun assertNull(v: Any?) = assertTrue(v == null, "expected null but was $v")

    private fun assertFailsWithMessage(block: () -> Unit) {
        try {
            block()
            throw AssertionError("expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }
}
