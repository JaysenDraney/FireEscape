package org.fireescape.launcher.plugin

import android.content.ComponentName
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.fireescape.conformance.MockHost
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * M0.8 L4: the kit runs against a good and a deliberately broken sample plugin APK.
 * These tests run in the emulator CI workflow, so the reference plugin's CI runs the kit.
 */
@RunWith(AndroidJUnit4::class)
class KitTest {
    private val target = InstrumentationRegistry.getInstrumentation().targetContext
    private val spikePkg = "org.fireescape.spike.plugin"

    @Test
    fun goodSamplePassesEveryRule() {
        val report = MockHost(target).checkSpikeContract(
            ComponentName(spikePkg, "$spikePkg.SpikePluginService"),
        )
        assertTrue(report.render(), report.passed)
    }

    @Test
    fun brokenSampleFailsNamingTheRule() {
        val report = MockHost(target).checkSpikeContract(
            ComponentName(spikePkg, "$spikePkg.BrokenSpikeService"),
        )
        assertFalse(report.passed)
        val failed = report.results.first { !it.passed }
        assertEquals("api-version", failed.rule)
        assertTrue(report.render().contains("FAIL api-version"))
    }
}
