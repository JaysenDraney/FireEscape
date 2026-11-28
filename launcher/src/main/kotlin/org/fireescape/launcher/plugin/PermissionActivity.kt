package org.fireescape.launcher.plugin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.Text
import org.fireescape.launcher.settings.SettingsStore
import org.fireescape.model.Grant
import org.fireescape.model.PermissionBroker
import java.io.File

/** Shows one plugin's permission screen. Launch with the plugin's package name extra. */
class PermissionActivity : ComponentActivity() {
    private lateinit var host: PluginHost

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        host = PluginHost(this)
        val packageName = intent.getStringExtra(EXTRA_PACKAGE)
        val plugin = packageName?.let { pkg ->
            host.discover().firstOrNull { it.descriptor.packageName == pkg }
        }
        val grantStore = GrantStore(SettingsStore(File(filesDir, "settings.properties")))
        setContent {
            if (plugin == null) {
                Text("Plugin not found", color = Color.White)
            } else {
                var stored by remember {
                    mutableStateOf(grantStore.grantsFor(plugin.descriptor.packageName))
                }
                PermissionScreen(
                    plugin = plugin,
                    activeGrants = PermissionBroker.activeGrants(plugin, stored),
                    storedGrants = stored,
                    onToggleGrant = { grant, on ->
                        grantStore.set(plugin.descriptor.packageName, grant, on)
                        stored = grantStore.grantsFor(plugin.descriptor.packageName)
                    },
                    onApproveTier3 = {
                        grantStore.set(plugin.descriptor.packageName, Grant.TIER3_APPROVAL, true)
                        stored = grantStore.grantsFor(plugin.descriptor.packageName)
                    },
                )
            }
        }
    }

    override fun onDestroy() {
        host.shutdown()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_PACKAGE = "package"
    }
}
