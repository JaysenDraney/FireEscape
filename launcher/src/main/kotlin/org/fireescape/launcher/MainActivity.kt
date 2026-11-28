package org.fireescape.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Home is the root of the task: Back never leaves the launcher.
        onBackPressedDispatcher.addCallback(this) {}
        // No plugin host yet (M0.5): the shell shows its empty state.
        setContent { HomeScreen(shelves = emptyList()) }
    }
}
