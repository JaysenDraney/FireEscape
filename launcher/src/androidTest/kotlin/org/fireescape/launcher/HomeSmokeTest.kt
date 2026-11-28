package org.fireescape.launcher

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** M0.4 L4 smoke: the launcher starts and shows the home screen. */
@RunWith(AndroidJUnit4::class)
class HomeSmokeTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launcherShowsTheHomeScreen() {
        rule.onNodeWithTag("empty_state").assertIsDisplayed()
    }
}
