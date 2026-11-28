package org.fireescape.launcher.plugin

import org.fireescape.launcher.settings.SettingsStore
import org.fireescape.model.Grant

/** Grants persisted per plugin package in the settings store, so they survive restarts. */
class GrantStore(private val settings: SettingsStore) {

    fun grantsFor(packageName: String): Set<Grant> =
        Grant.entries.filter { settings[key(packageName, it)] == "1" }.toSet()

    fun set(packageName: String, grant: Grant, granted: Boolean) {
        settings.put(key(packageName, grant), if (granted) "1" else "0")
    }

    private fun key(packageName: String, grant: Grant) = "grant.$packageName.${grant.name}"
}
