package org.fireescape.launcher

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.fireescape.launcher.settings.SettingsStore
import org.junit.Assert.assertEquals
import org.junit.Assume
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * M0.3 L4: settings survive the process being killed. writeMarker and readMarker are
 * run as two separate instrumentation runs with a force-stop between them to prove
 * disk persistence across a real process death; in a single pass readMarker is skipped
 * unless the marker was already written.
 */
@RunWith(AndroidJUnit4::class)
class SettingsPersistenceTest {
    private val target = InstrumentationRegistry.getInstrumentation().targetContext
    private val file = File(target.filesDir, "settings.properties")

    @Test
    fun writeMarker() {
        SettingsStore(file).put("probe", "kept")
    }

    @Test
    fun readMarker() {
        Assume.assumeTrue("marker not written yet", file.isFile)
        assertEquals("kept", SettingsStore(file)["probe"])
    }

    @Test
    fun freshLoadReadsWhatWasWritten() {
        SettingsStore(file).put("probe", "kept")
        assertEquals("kept", SettingsStore(file)["probe"])
    }
}
