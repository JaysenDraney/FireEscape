package org.fireescape.launcher.plugin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.fireescape.launcher.settings.SettingsStore
import org.fireescape.model.IndexFormat
import java.io.File
import java.net.URI
import java.util.Base64

/** Lets the user add a second index by URL and remove it. The key is pinned on first fetch. */
class IndexActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = IndexStore(SettingsStore(File(filesDir, "settings.properties")))
        setContent {
            var indexes by remember { mutableStateOf(store.list()) }
            IndexScreen(
                indexes = indexes,
                onAdd = { url ->
                    lifecycleScope.launch {
                        val keyBase64 = withContext(Dispatchers.IO) { fetchKey(url) }
                        if (keyBase64 != null) {
                            store.add(IndexStore.UserIndex(url, keyBase64))
                            indexes = store.list()
                        }
                    }
                },
                onRemove = { url ->
                    store.remove(url)
                    indexes = store.list()
                },
            )
        }
    }

    private fun fetchKey(url: String): String? = try {
        val json = URI(url).toURL().openStream().use { it.readBytes().decodeToString() }
        Base64.getEncoder().encodeToString(IndexFormat.parse(json).signingKey.encoded)
    } catch (e: Exception) {
        null
    }
}
