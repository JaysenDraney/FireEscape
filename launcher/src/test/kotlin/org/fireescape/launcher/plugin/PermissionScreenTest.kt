package org.fireescape.launcher.plugin

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import org.fireescape.model.Capability
import org.fireescape.model.Grant
import org.fireescape.model.InstalledPlugin
import org.fireescape.model.PluginDescriptor
import org.fireescape.model.PluginMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** M0.6 L2: the permission screen is completable with the D-pad and Select only. */
@RunWith(RobolectricTestRunner::class)
class PermissionScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun plugin(api: String, caps: Set<Capability>): InstalledPlugin =
        PluginMetadata.validate(
            PluginDescriptor(
                packageName = "org.example",
                serviceClassName = "org.example.S",
                api = api,
                displayName = "Example",
                declaredCapabilities = caps,
            ),
        )!!

    private fun show(
        plugin: InstalledPlugin,
        active: Set<Grant> = emptySet(),
        stored: Set<Grant> = active,
        onToggle: (Grant, Boolean) -> Unit = { _, _ -> },
        onApprove: () -> Unit = {},
    ) {
        composeRule.setContent {
            PermissionScreen(plugin, active, stored, onToggle, onApprove)
        }
    }

    @Test
    fun tierIsShownFirst() {
        show(plugin("wallpaper/1", setOf(Capability.NETWORK)))
        composeRule.onNodeWithTag("tier_banner").assertIsDisplayed()
        composeRule.onNodeWithText("Tier 1 — visual provider").assertIsDisplayed()
    }

    @Test
    fun networkIsLabeledShownNotEnforced() {
        show(plugin("wallpaper/1", setOf(Capability.NETWORK)))
        composeRule.onNodeWithTag("network_note").assertIsDisplayed()
        composeRule.onNodeWithText("Shown, not enforced").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun dpadAndSelectCanToggleAGrant() {
        var toggled: Pair<Grant, Boolean>? = null
        show(
            plugin("spike/1", setOf(Capability.NETWORK, Capability.SEES_INSTALLED_APPS)),
            onToggle = { grant, on -> toggled = grant to on },
        )
        composeRule.onNodeWithTag("grant_NETWORK").requestFocus()
        composeRule.onNodeWithTag("grant_NETWORK").performKeyInput { pressKey(Key.DirectionDown) }
        composeRule.onNodeWithTag("grant_SEES_INSTALLED_APPS").assertIsFocused()
        composeRule.onNodeWithTag("grant_SEES_INSTALLED_APPS").performKeyInput { pressKey(Key.Enter) }
        assertEquals(Grant.INSTALLED_APPS to true, toggled)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun tier3ShowsWarningAndSeparateApproval() {
        var approved = false
        show(
            plugin("automation/1", setOf(Capability.SEES_INSTALLED_APPS)),
            onApprove = { approved = true },
        )
        composeRule.onNodeWithTag("tier3_warning").assertIsDisplayed()
        composeRule.onNodeWithTag("tier3_approve").requestFocus()
        composeRule.onNodeWithTag("tier3_approve").performKeyInput { pressKey(Key.Enter) }
        assertTrue(approved)
    }

    @Test
    fun tier3GrantsStayDisabledUntilApproved() {
        show(
            plugin("automation/1", setOf(Capability.SEES_INSTALLED_APPS)),
            active = emptySet(),
            stored = setOf(Grant.INSTALLED_APPS),
        )
        composeRule.onNodeWithTag("grant_SEES_INSTALLED_APPS").assertIsNotEnabled()
    }

    @Test
    fun capabilityOutsideTheClassAllowListIsMarked() {
        show(plugin("wallpaper/1", setOf(Capability.SEES_INSTALLED_APPS)))
        composeRule.onNodeWithTag("notallowed_SEES_INSTALLED_APPS").assertIsDisplayed()
        composeRule.onNodeWithTag("grant_SEES_INSTALLED_APPS").assertIsNotEnabled()
    }

    @Test
    fun tier0PackDeclaresNoCapabilities() {
        show(plugin("theme/1", emptySet()))
        composeRule.onNodeWithTag("no_capabilities").assertIsDisplayed()
    }
}
