package com.ruota.mobile

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
 * End-to-end smoke test on a device/emulator: the setup screen renders, and starting a
 * game shows the in-game controls (proving the engine + shared UI wire up and the phrase
 * asset loads).
 */
@RunWith(AndroidJUnit4::class)
class GameSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun setupThenStartGameShowsControls() {
        // Setup screen is up.
        composeRule.onNodeWithText("GIOCA", ignoreCase = true).assertIsDisplayed()

        // Start the game.
        composeRule.onNodeWithText("GIOCA", ignoreCase = true).performClick()

        // The in-game "Gira la ruota" (spin) control should appear once the engine starts.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Gira la ruota", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Gira la ruota", substring = true).assertIsDisplayed()
    }
}
