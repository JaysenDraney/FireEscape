package org.fireescape.launcher

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Layer 2 of ADR 0002: D-pad focus behavior, no appearance checks. */
@RunWith(RobolectricTestRunner::class)
class HomeScreenFocusTest {
    @get:Rule
    val composeRule = createComposeRule()

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun dpadRightMovesFocusToNextTile() {
        composeRule.setContent { HomeScreen(rowTitles = listOf("Row")) }
        composeRule.onNodeWithTag("tile_0_0").requestFocus()
        composeRule.onNodeWithTag("tile_0_0").assertIsFocused()

        composeRule.onNodeWithTag("tile_0_0").performKeyInput { pressKey(Key.DirectionRight) }

        composeRule.onNodeWithTag("tile_0_1").assertIsFocused()
    }
}
