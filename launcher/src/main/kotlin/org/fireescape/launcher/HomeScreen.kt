package org.fireescape.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

/**
 * Home screen shell. Shelves are placeholders until the plugin host (M0.5) supplies
 * real rows. Shelves scroll, and Compose brings a newly focused tile into view, so
 * D-pad focus never leaves the screen.
 */
@Composable
fun HomeScreen(shelves: List<Shelf>) {
    when (val content = HomeContent.from(shelves)) {
        HomeContent.Empty -> EmptyHome()
        is HomeContent.Shelves -> ShelvesHome(content.shelves)
    }
}

@Composable
private fun EmptyHome() {
    Box(
        modifier = Modifier
            .testTag("empty_state")
            .fillMaxSize()
            .background(Color(0xFF101418)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("FireEscape", style = MaterialTheme.typography.displaySmall, color = Color.White)
            Text("No plugins yet", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Text(
                "Install a plugin to fill your home screen.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB8C2CC),
            )
        }
    }
}

@Composable
private fun ShelvesHome(shelves: List<Shelf>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101418))
            .verticalScroll(rememberScrollState())
            .padding(48.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        Text("FireEscape", style = MaterialTheme.typography.displaySmall, color = Color.White)
        shelves.forEachIndexed { rowIndex, shelf ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(shelf.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                if (shelf.tileCount == 0) {
                    Text(
                        "Nothing here yet",
                        modifier = Modifier.testTag("shelf_empty_$rowIndex"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFB8C2CC),
                    )
                } else {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        repeat(shelf.tileCount) { column ->
                            Tile(tag = "tile_${rowIndex}_$column")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Tile(tag: String) {
    var focused by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .testTag(tag)
            .width(200.dp)
            .height(112.dp)
            .background(Color(0xFF2A3440), RoundedCornerShape(8.dp))
            .border(
                width = if (focused) 4.dp else 0.dp,
                color = if (focused) Color.White else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
            )
            .onFocusChanged { focused = it.isFocused }
            .focusable(),
        contentAlignment = Alignment.Center,
    ) {
        Text(tag, color = Color.White)
    }
}
