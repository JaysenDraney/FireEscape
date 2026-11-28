package org.fireescape.launcher.plugin

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** M0.7 L2: the index screen is operable with the D-pad and Select only. */
@RunWith(RobolectricTestRunner::class)
class IndexScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyStateIsShown() {
        composeRule.setContent { IndexScreen(emptyList(), {}, {}) }
        composeRule.onNodeWithTag("no_indexes").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun addByUrlWithDpadAndSelect() {
        var added: String? = null
        composeRule.setContent { IndexScreen(emptyList(), { added = it }, {}) }
        composeRule.onNodeWithTag("index_url").requestFocus()
        composeRule.onNodeWithTag("index_url").performTextInput("https://example.org/index.json")
        composeRule.onNodeWithTag("index_add").requestFocus()
        composeRule.onNodeWithTag("index_add").assertIsFocused()
        composeRule.onNodeWithTag("index_add").performKeyInput { pressKey(Key.Enter) }
        assertEquals("https://example.org/index.json", added)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun removeWithSelect() {
        var removed: String? = null
        val indexes = listOf(IndexStore.UserIndex("https://example.org/a", "a2V5"))
        composeRule.setContent { IndexScreen(indexes, {}, { removed = it }) }
        composeRule.onNodeWithTag("index_remove").requestFocus()
        composeRule.onNodeWithTag("index_remove").performKeyInput { pressKey(Key.Enter) }
        assertEquals("https://example.org/a", removed)
    }
}
