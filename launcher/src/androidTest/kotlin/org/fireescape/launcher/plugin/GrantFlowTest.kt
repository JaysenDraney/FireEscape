package org.fireescape.launcher.plugin

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.fireescape.launcher.settings.SettingsStore
import org.fireescape.model.Grant
import org.fireescape.plugin.spike.ISpikePlugin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * M0.6 L4: grant the installed-app list and the plugin receives it; withdraw the grant
 * and the next call delivers nothing. Grants persist in the settings store.
 */
@RunWith(AndroidJUnit4::class)
class GrantFlowTest {

    @Test
    fun grantedAppsAreDeliveredAndWithdrawalStopsTheNextCall() {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val host = PluginHost(target)
        try {
            val spike = host.discover().first { it.descriptor.api == "spike/1" && it.compatible }
            assertTrue(host.bind(spike))
            val pkg = spike.descriptor.packageName
            val store = GrantStore(SettingsStore(File(target.filesDir, "grants-test.properties")))
            val apps = listOf("com.example.app")

            store.set(pkg, Grant.INSTALLED_APPS, true)
            val echo = host.callWithGrant(spike, Grant.INSTALLED_APPS, store.grantsFor(pkg)) {
                ISpikePlugin.Stub.asInterface(it).receiveInstalledApps(apps)
            }
            assertEquals("apps:com.example.app", echo)

            store.set(pkg, Grant.INSTALLED_APPS, false)
            val after = host.callWithGrant(spike, Grant.INSTALLED_APPS, store.grantsFor(pkg)) {
                ISpikePlugin.Stub.asInterface(it).receiveInstalledApps(apps)
            }
            assertNull("withdrawal stops delivery from the next call", after)
        } finally {
            host.shutdown()
        }
    }
}
