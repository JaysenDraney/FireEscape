package org.fireescape.launcher.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/** M0.3 L1: settings read and write, including a corrupt file falling back to defaults. */
class SettingsStoreTest {
    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun putThenGet() {
        val store = SettingsStore(folder.newFile("settings.properties"))
        store.put("theme", "dark")
        assertEquals("dark", store["theme"])
    }

    @Test
    fun missingFileUsesDefaults() {
        val store = SettingsStore(
            File(folder.root, "absent.properties"),
            defaults = mapOf("theme" to "system"),
        )
        assertEquals("system", store["theme"])
        assertNull(store["unknown"])
    }

    @Test
    fun freshInstanceReadsPersistedValue() {
        val file = folder.newFile("settings.properties")
        SettingsStore(file).put("theme", "dark")
        assertEquals("dark", SettingsStore(file)["theme"])
    }

    @Test
    fun fileValuesOverrideDefaults() {
        val file = folder.newFile("settings.properties")
        SettingsStore(file).put("theme", "dark")
        val store = SettingsStore(file, defaults = mapOf("theme" to "system", "rows" to "3"))
        assertEquals("dark", store["theme"])
        assertEquals("3", store["rows"])
    }

    @Test
    fun corruptFileFallsBackToDefaults() {
        val file = folder.newFile("settings.properties")
        // A malformed unicode escape makes Properties.load throw.
        file.writeText("theme=\\u00zz\n")
        val store = SettingsStore(file, defaults = mapOf("theme" to "system"))
        assertEquals("system", store["theme"])
    }

    @Test
    fun writeReplacesCorruptFile() {
        val file = folder.newFile("settings.properties")
        file.writeText("theme=\\u00zz\n")
        SettingsStore(file, defaults = mapOf("theme" to "system")).put("theme", "dark")
        assertEquals("dark", SettingsStore(file)["theme"])
    }
}
