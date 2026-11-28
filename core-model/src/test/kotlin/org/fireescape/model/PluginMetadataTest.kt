package org.fireescape.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** M0.5 L1: metadata parsing against valid, missing and malformed fixtures; version rules. */
class PluginMetadataTest {

    private fun descriptor(api: String, caps: Set<Capability> = emptySet()) = PluginDescriptor(
        packageName = "org.example.plugin",
        serviceClassName = "org.example.plugin.Service",
        api = api,
        displayName = "Example",
        sourceUrl = "https://example.org/repo",
        declaredCapabilities = caps,
    )

    @Test
    fun validWallpaperDeclaration() {
        val plugin = PluginMetadata.validate(descriptor("wallpaper/1"))!!
        assertEquals(PluginClass.WALLPAPER, plugin.pluginClass)
        assertEquals(1, plugin.apiMajor)
        assertEquals(1, plugin.tier)
        assertTrue(plugin.compatible)
    }

    @Test
    fun spikeSkeletonContractIsValid() {
        val plugin = PluginMetadata.validate(descriptor("spike/1"))!!
        assertEquals(PluginClass.SPIKE, plugin.pluginClass)
        assertTrue(plugin.compatible)
    }

    @Test
    fun tierComesFromClassNotFromThePlugin() {
        // A wallpaper plugin cannot claim a lower tier by declaring one; there is no
        // tier field at all. The class mapping fixes it.
        val wallpaper = PluginMetadata.validate(descriptor("wallpaper/1"))!!
        val automation = PluginMetadata.validate(descriptor("automation/1"))!!
        assertEquals(1, wallpaper.tier)
        assertEquals(3, automation.tier)
    }

    @Test
    fun unsupportedMajorIsListedIncompatible() {
        val plugin = PluginMetadata.validate(descriptor("wallpaper/2"))!!
        assertFalse("major 2 is not supported yet", plugin.compatible)
        assertEquals(2, plugin.apiMajor)
    }

    @Test
    fun unknownClassIsRejected() {
        assertNull(PluginMetadata.validate(descriptor("hologram/1")))
    }

    @Test
    fun malformedApiValuesAreRejected() {
        for (bad in listOf("", "wallpaper", "/1", "wallpaper/", "wallpaper/x", "wallpaper/0")) {
            assertNull("should reject '$bad'", PluginMetadata.validate(descriptor(bad)))
        }
    }

    @Test
    fun capabilityParsing() {
        assertEquals(Capability.NETWORK, PluginMetadata.parseCapability("network"))
        assertEquals(Capability.SEES_INSTALLED_APPS, PluginMetadata.parseCapability(" sees_installed_apps "))
        assertNull(PluginMetadata.parseCapability("root"))
    }

    @Test
    fun everySupportedClassHasAUniqueIntentAction() {
        val actions = PluginClass.entries.map {
            PluginMetadata.ACTION_PREFIX + it.name
        }
        assertEquals(actions.size, actions.toSet().size)
    }
}
