package org.fireescape.launcher

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
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

/** Layer 2 of ADR 0002: D-pad focus behavior and empty states, no appearance checks. */
@RunWith(RobolectricTestRunner::class)
class HomeScreenFocusTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun setShelves(vararg shelves: Shelf) {
        composeRule.setContent { HomeScreen(shelves = shelves.toList()) }
    }

    @OptIn(ExperimentalTestApi::class)
    private fun press(key: Key, fromTag: String) {
        composeRule.onNodeWithTag(fromTag).performKeyInput { pressKey(key) }
    }

    @Test
    fun dpadRightMovesFocusToNextTile() {
        setShelves(Shelf("Row", 8))
        composeRule.onNodeWithTag("tile_0_0").requestFocus()
        composeRule.onNodeWithTag("tile_0_0").assertIsFocused()

        press(Key.DirectionRight, "tile_0_0")

        composeRule.onNodeWithTag("tile_0_1").assertIsFocused()
    }

    @Test
    fun dpadDownMovesFocusToTheNextShelf() {
        setShelves(Shelf("Row one", 4), Shelf("Row two", 4))
        composeRule.onNodeWithTag("tile_0_0").requestFocus()

        press(Key.DirectionDown, "tile_0_0")

        composeRule.onNodeWithTag("tile_1_0").assertIsFocused()
    }

    @Test
    fun dpadUpReturnsToThePreviousShelf() {
        setShelves(Shelf("Row one", 4), Shelf("Row two", 4))
        composeRule.onNodeWithTag("tile_1_0").requestFocus()

        press(Key.DirectionUp, "tile_1_0")

        composeRule.onNodeWithTag("tile_0_0").assertIsFocused()
    }

    @Test
    fun everyShelfIsReachableWithTheDpadAlone() {
        setShelves(Shelf("One", 2), Shelf("Two", 2), Shelf("Three", 2))
        composeRule.onNodeWithTag("tile_0_0").requestFocus()

        press(Key.DirectionDown, "tile_0_0")
        press(Key.DirectionDown, "tile_1_0")

        composeRule.onNodeWithTag("tile_2_0").assertIsFocused()
    }

    @Test
    fun focusedTileStaysVisibleWhenScrollingOffScreen() {
        setShelves(Shelf("Row", 8))
        composeRule.onNodeWithTag("tile_0_0").requestFocus()

        var focused = "tile_0_0"
        repeat(7) { step ->
            press(Key.DirectionRight, focused)
            focused = "tile_0_${step + 1}"
            composeRule.onNodeWithTag(focused).assertIsFocused()
        }

        composeRule.onNodeWithTag("tile_0_7").assertIsDisplayed()
    }

    @Test
    fun emptyHomeShowsTheEmptyStateWithoutCrashing() {
        setShelves()
        composeRule.onNodeWithTag("empty_state").assertIsDisplayed()
    }

    @Test
    fun shelvesWithoutTilesShowTheEmptyState() {
        setShelves(Shelf("Continue watching", 0), Shelf("Apps", 0))
        composeRule.onNodeWithTag("empty_state").assertIsDisplayed()
    }

    @Test
    fun anEmptyShelfShowsItsMessageAndOtherShelvesStayFocusable() {
        setShelves(Shelf("Empty", 0), Shelf("Apps", 4))
        composeRule.onNodeWithTag("shelf_empty_0").assertIsDisplayed()
        composeRule.onNodeWithTag("tile_1_0").requestFocus()
        composeRule.onNodeWithTag("tile_1_0").assertIsFocused()
    }
}
