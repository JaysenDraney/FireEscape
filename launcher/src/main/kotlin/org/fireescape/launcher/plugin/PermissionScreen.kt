package org.fireescape.launcher.plugin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import org.fireescape.model.Capability
import org.fireescape.model.Grant
import org.fireescape.model.InstalledPlugin
import org.fireescape.model.PermissionBroker

private val tierNames = mapOf(
    0 to "declarative pack",
    1 to "visual provider",
    2 to "data provider",
    3 to "device control",
)

private val capabilityText = mapOf(
    Capability.NETWORK to "Reach the network",
    Capability.SEES_INSTALLED_APPS to "See your installed apps",
    Capability.FOCUS_CONTEXT to "See which tile is focused",
    Capability.READS_RATINGS to "Read your ratings",
    Capability.READS_WATCH_HISTORY to "Read your watch history",
    Capability.SYSTEM_GRANT to "Use a system permission (ADB or system setting)",
)

internal fun capabilityGrant(capability: Capability): Grant = when (capability) {
    Capability.NETWORK -> Grant.NETWORK
    Capability.SEES_INSTALLED_APPS -> Grant.INSTALLED_APPS
    Capability.FOCUS_CONTEXT -> Grant.FOCUS_CONTEXT
    Capability.READS_RATINGS -> Grant.RATINGS
    Capability.READS_WATCH_HISTORY -> Grant.WATCH_HISTORY
    Capability.SYSTEM_GRANT -> Grant.SYSTEM_GRANT
}

/**
 * The permission screen: the tier first, then each declared capability with its grant.
 * Tier 3 shows a plain-language warning and a separate approval before any grant can
 * take effect. Network use is labeled as shown, not enforced. Everything is reachable
 * with the D-pad and Select.
 */
@Composable
fun PermissionScreen(
    plugin: InstalledPlugin,
    activeGrants: Set<Grant>,
    storedGrants: Set<Grant>,
    onToggleGrant: (Grant, Boolean) -> Unit,
    onApproveTier3: () -> Unit,
) {
    val allowed = PermissionBroker.allowedGrantsFor(plugin.pluginClass)
    val tier3Pending = PermissionBroker.requiresSeparateApproval(plugin) &&
        Grant.TIER3_APPROVAL !in activeGrants
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101418))
            .verticalScroll(rememberScrollState())
            .padding(48.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(
            modifier = Modifier.testTag("tier_banner"),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                "Tier ${plugin.tier} — ${tierNames[plugin.tier]}",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )
            Text(
                "${plugin.descriptor.displayName} · ${plugin.descriptor.api}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB8C2CC),
            )
        }
        if (PermissionBroker.requiresSeparateApproval(plugin)) {
            Text(
                "Tier 3 plugins can act on your device: launch apps, change settings, run " +
                    "automations. Approve only if you trust this plugin's source.",
                modifier = Modifier.testTag("tier3_warning"),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE0A83C),
            )
            if (tier3Pending) {
                Button(onClick = onApproveTier3, modifier = Modifier.testTag("tier3_approve")) {
                    Text("Approve separately")
                }
            } else {
                Text(
                    "Separately approved",
                    modifier = Modifier.testTag("tier3_approved"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4CAF7D),
                )
            }
        }
        if (plugin.descriptor.declaredCapabilities.isEmpty()) {
            Text(
                "This plugin declares no capabilities.",
                modifier = Modifier.testTag("no_capabilities"),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB8C2CC),
            )
        }
        plugin.descriptor.declaredCapabilities.forEach { capability ->
            val grant = capabilityGrant(capability)
            val isAllowed = grant in allowed
            val on = grant in activeGrants
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        capabilityText.getValue(capability),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                    )
                    if (capability == Capability.NETWORK) {
                        Text(
                            "Shown, not enforced",
                            modifier = Modifier.testTag("network_note"),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB8C2CC),
                        )
                    }
                    if (!isAllowed) {
                        Text(
                            "Not available for this class",
                            modifier = Modifier.testTag("notallowed_${capability.name}"),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB8C2CC),
                        )
                    }
                }
                Button(
                    onClick = { onToggleGrant(grant, grant !in storedGrants) },
                    modifier = Modifier.testTag("grant_${capability.name}"),
                    enabled = isAllowed && !tier3Pending,
                ) {
                    Text(if (on) "Revoke" else "Allow")
                }
            }
        }
    }
}
