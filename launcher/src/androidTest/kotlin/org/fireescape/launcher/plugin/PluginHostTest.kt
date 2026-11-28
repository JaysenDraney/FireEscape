package org.fireescape.launcher.plugin

import android.os.Process
import android.os.RemoteException
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.fireescape.model.InstalledPlugin
import org.fireescape.model.PluginClass
import org.fireescape.plugin.spike.ISpikePlugin
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeoutException

/**
 * M0.5 L4: the host finds, binds, calls and unbinds plugins in a separate APK, lists an
 * unsupported major as incompatible without binding it, and survives a plugin crash and
 * a hung call. Needs the spike plugin APK installed first.
 */
@RunWith(AndroidJUnit4::class)
class PluginHostTest {
    private val target = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var host: PluginHost

    @Before
    fun setUp() {
        host = PluginHost(target)
    }

    @After
    fun tearDown() {
        host.shutdown()
    }

    private fun spike1(): InstalledPlugin =
        host.discover().first { it.descriptor.api == "spike/1" && it.compatible }

    @Test
    fun discoversReadsDeclarationBindsCallsAndUnbinds() {
        val spike = host.discover().first { it.descriptor.api == "spike/1" }
        assertTrue(spike.compatible)
        assertEquals(PluginClass.SPIKE, spike.pluginClass)
        assertEquals("https://github.com/JaysenDraney/FireEscape", spike.descriptor.sourceUrl)

        assertTrue(host.bind(spike))
        val text = host.call(spike) { ISpikePlugin.Stub.asInterface(it).describe() }
        assertTrue("runs in another process: $text", !text.endsWith("pid=${Process.myPid()}"))

        host.unbind(spike)
        assertFalse(host.isBound(spike))
    }

    @Test
    fun unsupportedMajorIsListedIncompatibleAndNeverBound() {
        val v2 = host.discover().first { it.descriptor.api == "spike/2" }
        assertFalse(v2.compatible)
        assertFalse(host.bind(v2))
        assertFalse(host.isBound(v2))
    }

    @Test
    fun hungCallTimesOutAndLaterCallsStillWork() {
        val spike = spike1()
        assertTrue(host.bind(spike))

        val start = System.nanoTime()
        try {
            host.call(spike) { ISpikePlugin.Stub.asInterface(it).slowEcho("late", 5_000) }
            fail("expected a TimeoutException")
        } catch (expected: TimeoutException) {
        }
        val elapsedMs = (System.nanoTime() - start) / 1_000_000
        assertTrue("caller waited ${elapsedMs}ms", elapsedMs < 1_500)

        // The hung call must not starve later calls.
        val text = host.call(spike) { ISpikePlugin.Stub.asInterface(it).describe() }
        assertTrue(text.startsWith("spike plugin"))
    }

    @Test
    fun pluginCrashDoesNotFreezeTheHost() {
        val spike = spike1()
        assertTrue(host.bind(spike))
        val before = host.call(spike) { ISpikePlugin.Stub.asInterface(it).describe() }

        try {
            host.call(spike) { ISpikePlugin.Stub.asInterface(it).crash() }
        } catch (expected: RemoteException) {
            // The plugin died mid-call; either outcome is fine.
        }

        // BIND_AUTO_CREATE: the system restarts the plugin and reconnects the host.
        waitUntil(5_000) { !host.isBound(spike) }
        waitUntil(15_000) { host.isBound(spike) }
        val after = host.call(spike) { ISpikePlugin.Stub.asInterface(it).describe() }
        assertTrue("plugin restarted in a new process: $before -> $after", before != after)
    }

    private fun waitUntil(timeoutMs: Long, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (!condition()) {
            if (System.currentTimeMillis() > deadline) throw AssertionError("condition not met in ${timeoutMs}ms")
            Thread.sleep(50)
        }
    }
}
