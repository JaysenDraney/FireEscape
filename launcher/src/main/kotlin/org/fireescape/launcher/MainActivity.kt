package org.fireescape.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No plugin host yet (M0.5): the shell shows its empty state.
        setContent { HomeScreen(shelves = emptyList()) }
    }
}
