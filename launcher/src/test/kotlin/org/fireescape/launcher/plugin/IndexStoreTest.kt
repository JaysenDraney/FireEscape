package org.fireescape.launcher.plugin

import org.fireescape.launcher.settings.SettingsStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** M0.7 L1: user indexes can be added and removed, and persist. */
class IndexStoreTest {
    @get:Rule
    val folder = TemporaryFolder()

    private fun store() = IndexStore(SettingsStore(folder.newFile("settings.properties")))

    @Test
    fun addAndList() {
        val store = store()
        store.add(IndexStore.UserIndex("https://example.org/index.json", "a2V5"))
        assertEquals(listOf(IndexStore.UserIndex("https://example.org/index.json", "a2V5")), store.list())
    }

    @Test
    fun removeDeletesTheEntry() {
        val store = store()
        store.add(IndexStore.UserIndex("https://example.org/a", "a2V5"))
        store.add(IndexStore.UserIndex("https://example.org/b", "a2V5"))
        store.remove("https://example.org/a")
        assertEquals(listOf("https://example.org/b"), store.list().map { it.url })
    }

    @Test
    fun addingTheSameUrlReplacesTheKey() {
        val store = store()
        store.add(IndexStore.UserIndex("https://example.org/a", "b2xk"))
        store.add(IndexStore.UserIndex("https://example.org/a", "bmV3"))
        assertEquals(listOf("bmV3"), store.list().map { it.keyBase64 })
    }

    @Test
    fun indexesSurviveAFreshStoreInstance() {
        val file = folder.newFile("settings2.properties")
        IndexStore(SettingsStore(file)).add(IndexStore.UserIndex("https://example.org/a", "a2V5"))
        assertEquals(1, IndexStore(SettingsStore(file)).list().size)
    }

    @Test
    fun emptyByDefault() {
        assertTrue(store().list().isEmpty())
    }
}
