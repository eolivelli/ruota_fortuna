package com.ruota.uicommon

import com.ruota.core.engine.GameEngine
import com.ruota.core.engine.Outcome
import com.ruota.core.engine.TurnPhase
import com.ruota.core.model.GameConfig
import com.ruota.core.model.Puzzle
import com.ruota.core.model.WheelWedge
import com.ruota.core.puzzle.PuzzleProvider
import com.ruota.core.wheel.Wheel
import com.ruota.uicommon.game.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameViewModelTest {

    private fun newViewModel(): GameViewModel {
        val wheel = Wheel(listOf(WheelWedge.Cash(100)))
        val provider = PuzzleProvider { Puzzle("Cosa", "CASA") }
        val engine = GameEngine(
            config = GameConfig(),
            playerNames = listOf("Giocatore 1"),
            puzzleProvider = provider,
            wheel = wheel,
        )
        return GameViewModel(engine)
    }

    @Test
    fun initialStateIsAwaitingAction() {
        val vm = newViewModel()
        assertEquals(TurnPhase.AWAITING_ACTION, vm.state.value.phase)
        assertEquals(null, vm.lastOutcome.value)
    }

    @Test
    fun spinMovesToAwaitingConsonantAndReportsCash() {
        val vm = newViewModel()
        vm.spin()
        assertEquals(TurnPhase.AWAITING_CONSONANT, vm.state.value.phase)
        val outcome = vm.lastOutcome.value
        assertTrue("expected SpunCash, was $outcome", outcome is Outcome.SpunCash)
        assertEquals(100, (outcome as Outcome.SpunCash).amount)
    }

    @Test
    fun guessConsonantIncreasesRoundTotalAndUpdatesState() {
        val vm = newViewModel()
        vm.spin()
        val before = vm.state.value.currentPlayer.roundTotal
        vm.guessConsonant('C')
        val after = vm.state.value.currentPlayer.roundTotal
        assertTrue("round total should increase (before=$before after=$after)", after > before)
        assertTrue(vm.lastOutcome.value is Outcome.ConsonantHit)
        // 'C' occurs once in CASA at Cash(100).
        assertEquals(100, after)
        assertEquals(TurnPhase.AWAITING_ACTION, vm.state.value.phase)
    }
}
