package com.ruota.mobile

import android.content.pm.ActivityInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies the two fixes: rotating the device keeps the in-game state (no restart), and
 * the quit button abandons the match back to setup.
 */
@RunWith(AndroidJUnit4::class)
class RotationAndQuitTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun startGame() {
        composeRule.onNodeWithText("GIOCA", ignoreCase = true).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Gira la ruota", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun inGame(): Boolean =
        composeRule.onAllNodesWithText("Gira la ruota", substring = true)
            .fetchSemanticsNodes().isNotEmpty()

    @Test
    fun rotationKeepsGameState() {
        startGame()

        // Rotate to landscape then back to portrait; the game must NOT reset to setup.
        composeRule.activityRule.scenario.onActivity {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.onActivity {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        composeRule.waitForIdle()

        // Still in-game (setup would have no spin control) => rotation kept the state.
        composeRule.waitUntil(timeoutMillis = 5_000) { inGame() }
        composeRule.onNodeWithText("Gira la ruota", substring = true).assertExists()
        composeRule.onAllNodesWithText("GIOCA", ignoreCase = true).fetchSemanticsNodes().let {
            assert(it.isEmpty()) { "Rotation reset the app back to the setup screen" }
        }
    }

    @Test
    fun quitReturnsToSetup() {
        startGame()

        composeRule.onNodeWithText("Esci").performClick()
        // Confirm the quit dialog.
        composeRule.onNodeWithText("Abbandona").performClick()

        // Back on the setup screen.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("GIOCA", ignoreCase = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("GIOCA", ignoreCase = true).assertIsDisplayed()
    }
}
