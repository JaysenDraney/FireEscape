package org.fireescape.launcher.plugin

import org.fireescape.launcher.settings.SettingsStore
import org.fireescape.model.Grant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** M0.6 L1: grants persist, can be withdrawn, and are isolated per package. */
class GrantStoreTest {
    @get:Rule
    val folder = TemporaryFolder()

    private fun store() = GrantStore(SettingsStore(folder.newFile("settings.properties")))

    @Test
    fun grantRoundTrip() {
        val store = store()
        store.set("org.example.plugin", Grant.NETWORK, true)
        assertEquals(setOf(Grant.NETWORK), store.grantsFor("org.example.plugin"))
    }

    @Test
    fun withdrawalRemovesTheGrant() {
        val store = store()
        store.set("org.example.plugin", Grant.INSTALLED_APPS, true)
        store.set("org.example.plugin", Grant.INSTALLED_APPS, false)
        assertTrue(store.grantsFor("org.example.plugin").isEmpty())
    }

    @Test
    fun grantsAreIsolatedPerPackage() {
        val store = store()
        store.set("a", Grant.NETWORK, true)
        store.set("b", Grant.RATINGS, true)
        assertEquals(setOf(Grant.NETWORK), store.grantsFor("a"))
        assertEquals(setOf(Grant.RATINGS), store.grantsFor("b"))
    }

    @Test
    fun grantsSurviveAFreshStoreInstance() {
        val file = folder.newFile("settings2.properties")
        GrantStore(SettingsStore(file)).set("a", Grant.NETWORK, true)
        assertEquals(setOf(Grant.NETWORK), GrantStore(SettingsStore(file)).grantsFor("a"))
    }
}
