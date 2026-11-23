package org.fireescape.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
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

/** Placeholder home screen. Real rows come from the plugin host in a later phase. */
@Composable
fun HomeScreen(rowTitles: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101418))
            .padding(48.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        Text("FireEscape", style = MaterialTheme.typography.displaySmall, color = Color.White)
        rowTitles.forEachIndexed { rowIndex, title ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(4) { column -> Tile(tag = "tile_${rowIndex}_$column") }
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

