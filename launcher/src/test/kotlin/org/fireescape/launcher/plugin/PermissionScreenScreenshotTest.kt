package org.fireescape.launcher.plugin

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.fireescape.model.Capability
import org.fireescape.model.Grant
import org.fireescape.model.InstalledPlugin
import org.fireescape.model.PluginDescriptor
import org.fireescape.model.PluginMetadata
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** M0.6 L3: the permission screen for one plugin of each tier. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w960dp-h540dp-xhdpi")
class PermissionScreenScreenshotTest {
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

    private fun capture(plugin: InstalledPlugin, grants: Set<Grant>, file: String) {
        composeRule.setContent {
            PermissionScreen(plugin, grants, grants, { _, _ -> }, {})
        }
        composeRule.onRoot().captureRoboImage("src/screenshots/$file")
    }

    @Test
    fun tier0Pack() {
        capture(plugin("theme/1", emptySet()), emptySet(), "PermissionTier0.png")
    }

    @Test
    fun tier1VisualProvider() {
        capture(
            plugin("wallpaper/1", setOf(Capability.NETWORK)),
            setOf(Grant.NETWORK),
            "PermissionTier1.png",
        )
    }

    @Test
    fun tier2DataProvider() {
        capture(
            plugin("recommendation/1", setOf(Capability.NETWORK, Capability.READS_RATINGS)),
            setOf(Grant.NETWORK, Grant.RATINGS),
            "PermissionTier2.png",
        )
    }

    @Test
    fun tier3DeviceControl() {
        capture(
            plugin(
                "automation/1",
                setOf(Capability.NETWORK, Capability.SEES_INSTALLED_APPS, Capability.SYSTEM_GRANT),
            ),
            setOf(Grant.TIER3_APPROVAL, Grant.NETWORK, Grant.INSTALLED_APPS),
            "PermissionTier3.png",
        )
    }
}
