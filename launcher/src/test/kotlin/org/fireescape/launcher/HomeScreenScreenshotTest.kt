package org.fireescape.launcher

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Layer 3 of ADR 0002: Roborazzi screenshots on the JVM at a 1080p TV size (960x540 dp, 2x). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w960dp-h540dp-xhdpi")
class HomeScreenScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun homeScreen() {
        composeRule.setContent { HomeScreen(rowTitles = listOf("Continue watching", "Apps")) }
        composeRule.onRoot().captureRoboImage("src/screenshots/HomeScreen.png")
    }
}
