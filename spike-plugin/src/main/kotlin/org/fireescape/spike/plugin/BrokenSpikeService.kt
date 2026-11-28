package org.fireescape.spike.plugin

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Process
import org.fireescape.plugin.spike.ISpikePlugin

/**
 * Deliberately broken sample for the conformance kit (M0.8): it declares spike/1 in its
 * manifest metadata but reports API version 2, so the kit must fail the api-version rule.
 * Discovered under its own action so the good sample's tests are unaffected.
 */
class BrokenSpikeService : Service() {

    private val binder = object : ISpikePlugin.Stub() {
        override fun getPluginApiVersion(): Int = 2

        override fun describe(): String = "broken spike plugin pid=${Process.myPid()}"

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
