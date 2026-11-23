package org.fireescape.launcher.spike

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * M0.2 spike (L4). Needs the spike plugin APK installed first:
 *   gradlew :spike-plugin:installDebug :launcher:connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class SpikeTransportTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var client: SpikePluginClient

    @Before
    fun bindToPlugin() {
        client = SpikePluginClient(context)
        val found = client.discover()
        assertEquals("exactly one spike plugin installed", 1, found.size)
        assertEquals("spike/1", found.single().api)
        assertTrue(client.bind(found.single().component))
        waitUntil(5_000) { client.plugin != null }
    }

    @After
    fun unbind() = client.unbind()

    @Test
    fun discoversBindsAndCallsPluginInAnotherApk() {
        val result = client.call(2_000) { it.describe() }
        assertTrue(result is SpikePluginClient.CallResult.Ok)
        val text = (result as SpikePluginClient.CallResult.Ok).value
        assertTrue("runs in another process: $text", !text.endsWith("pid=${android.os.Process.myPid()}"))
        assertEquals(1, (client.call(2_000) { it.pluginApiVersion } as SpikePluginClient.CallResult.Ok).value)
    }

    @Test
    fun launcherSurvivesPluginCrashAndReconnects() {
        val before = (client.call(2_000) { it.describe() } as SpikePluginClient.CallResult.Ok).value

        client.call(2_000) { it.crash() }

        waitUntil(5_000) { client.disconnects >= 1 }
        // BIND_AUTO_CREATE: the system restarts the plugin and reconnects us.
        waitUntil(15_000) { client.connects >= 2 && client.plugin != null }
        val after = (client.call(2_000) { it.describe() } as SpikePluginClient.CallResult.Ok).value
        assertTrue("plugin restarted in a new process: $before -> $after", before != after)
    }

    @Test
    fun hungPluginCallTimesOutWithoutBlockingCaller() {
        val start = System.nanoTime()
        val result = client.call(500) { it.slowEcho("late", 5_000) }
        val elapsedMs = (System.nanoTime() - start) / 1_000_000

        assertEquals(SpikePluginClient.CallResult.TimedOut, result)
        assertTrue("caller waited ${elapsedMs}ms", elapsedMs < 1_500)
        assertNotNull(client.plugin)
    }

    private fun waitUntil(timeoutMs: Long, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (!condition()) {
            if (System.currentTimeMillis() > deadline) throw AssertionError("condition not met in ${timeoutMs}ms")
            Thread.sleep(50)
        }
    }
}