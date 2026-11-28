package org.fireescape.spike.plugin

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Process
import org.fireescape.plugin.spike.ISpikePlugin

open class SpikePluginService : Service() {

    private val binder = object : ISpikePlugin.Stub() {
        override fun getPluginApiVersion(): Int = 1

        override fun describe(): String = "spike plugin pid=${Process.myPid()}"

        override fun crash() {
            Process.killProcess(Process.myPid())
        }

        override fun slowEcho(text: String, delayMillis: Long): String {
            Thread.sleep(delayMillis)
            return text
        }

        override fun receiveInstalledApps(packages: MutableList<String>?): String =
            "apps:" + (packages?.joinToString(",") ?: "")
    }

    override fun onBind(intent: Intent): IBinder = binder
}