package org.fireescape.launcher.settings

import java.io.File
import java.util.Properties

/**
 * Key-value settings persisted synchronously to a private file, so they survive the
 * process being killed. A corrupt or unreadable file falls back to the defaults; the
 * next write replaces it.
 */
class SettingsStore(
    private val file: File,
    private val defaults: Map<String, String> = emptyMap(),
) {
    private val values = Properties()

    init {
        resetToDefaults()
        if (file.isFile) {
            try {
                file.inputStream().use { values.load(it) }
            } catch (e: Exception) {
                resetToDefaults()
            }
        }
    }

    operator fun get(key: String): String? = values.getProperty(key)

    fun put(key: String, value: String) {
        values.setProperty(key, value)
        file.parentFile?.mkdirs()
        file.outputStream().use { values.store(it, null) }
    }

    private fun resetToDefaults() {
        values.clear()
        defaults.forEach { (key, value) -> values.setProperty(key, value) }
    }
}
