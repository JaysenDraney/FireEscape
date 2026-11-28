package org.fireescape.launcher.plugin

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

/**
 * Manage plugin indexes: the default one ships with the launcher; user indexes are
 * added by URL (key pinned on first fetch) and can be removed. D-pad and Select only.
 */
@Composable
fun IndexScreen(
    indexes: List<IndexStore.UserIndex>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    var url by rememberSaveable { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101418))
            .verticalScroll(rememberScrollState())
            .padding(48.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("Plugin indexes", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        Text(
            "The default index ships with FireEscape. Add another by URL; its signing key is pinned on first fetch.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFB8C2CC),
        )
        Text("Index URL", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFB8C2CC))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BasicTextField(
                value = url,
                onValueChange = { url = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("index_url")
                    .background(Color(0xFF2A3440), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                cursorBrush = SolidColor(Color.White),
                singleLine = true,
            )
            Button(
                onClick = {
                    if (url.isNotBlank()) {
                        onAdd(url.trim())
                        url = ""
                    }
                },
                modifier = Modifier.testTag("index_add"),
            ) { Text("Add") }
        }
        if (indexes.isEmpty()) {
            Text(
                "No user indexes yet.",
                modifier = Modifier.testTag("no_indexes"),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB8C2CC),
            )
        }
        indexes.forEach { index ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    index.url,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                )
                Button(
                    onClick = { onRemove(index.url) },
                    modifier = Modifier.testTag("index_remove"),
                ) { Text("Remove") }
            }
        }
    }
}
