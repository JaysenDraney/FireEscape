package org.fireescape.launcher.plugin

import org.fireescape.launcher.settings.SettingsStore
import org.json.JSONArray
import org.json.JSONObject

/**
 * User-added indexes, persisted with the key pinned on first use (TOFU). The default
 * index ships with the launcher and is not stored here.
 */
class IndexStore(private val settings: SettingsStore) {

    data class UserIndex(val url: String, val keyBase64: String)

    fun list(): List<UserIndex> {
        val raw = settings[KEY] ?: return emptyList()
        val array = JSONArray(raw)
        return (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            UserIndex(o.getString("url"), o.getString("key"))
        }
    }

    fun add(index: UserIndex) {
        save(list().filter { it.url != index.url } + index)
    }

    fun remove(url: String) {
        save(list().filter { it.url != url })
    }

    private fun save(indexes: List<UserIndex>) {
        val array = JSONArray()
        indexes.forEach { array.put(JSONObject().put("url", it.url).put("key", it.keyBase64)) }
        settings.put(KEY, array.toString())
    }

    private companion object {
        const val KEY = "indexes"
    }
}
