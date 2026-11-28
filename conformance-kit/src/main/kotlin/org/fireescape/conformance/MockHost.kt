package org.fireescape.conformance

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import org.fireescape.plugin.spike.ISpikePlugin
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * A minimal host that runs a plugin without the launcher (M0.8). It binds the plugin
 * service directly and checks the published contract rules, reporting pass or fail per
 * rule. Calls run under a timeout so a hanging plugin fails the rule instead of the kit.
 */
class MockHost(private val context: Context) {

    /** Checks the spike/1 contract (plugin-api/README.md) against one service component. */
    fun checkSpikeContract(component: ComponentName, timeoutMs: Long = 2_000): ConformanceReport {
        val results = mutableListOf<RuleResult>()
        var binder: IBinder? = null
        val latch = CountDownLatch(1)
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                binder = service
                latch.countDown()
            }

            override fun onServiceDisconnected(name: ComponentName) {
                binder = null
            }
        }
        val bound = context.bindService(
            Intent().setComponent(component), connection, Context.BIND_AUTO_CREATE,
        )
        val connected = bound && latch.await(5, TimeUnit.SECONDS)
        results.add(
            RuleResult("bind", connected, if (connected) "service connected" else "service did not connect"),
        )
        if (!connected) {
            if (bound) context.unbindService(connection)
            return ConformanceReport(component.flattenToShortString(), results)
        }
        try {
            val plugin = ISpikePlugin.Stub.asInterface(binder)

            val version = timed(timeoutMs) { plugin.pluginApiVersion }
            results.add(
                if (version == 1) {
                    RuleResult("api-version", true, "declared spike/1, reported 1")
                } else {
                    RuleResult("api-version", false, "declared spike/1 but reported $version")
                },
            )

            val description = timed(timeoutMs) { plugin.describe() }
            results.add(
                if (!description.isNullOrEmpty()) {
                    RuleResult("describe", true, description)
                } else {
                    RuleResult("describe", false, "no answer within ${timeoutMs}ms or empty")
                },
            )

            val echo = timed(timeoutMs) { plugin.slowEcho("kit", 100) }
            results.add(
                if (echo == "kit") {
                    RuleResult("echo", true, "slowEcho returned the text")
                } else {
                    RuleResult("echo", false, "expected 'kit', got '$echo' within ${timeoutMs}ms")
                },
            )
        } finally {
            context.unbindService(connection)
        }
        return ConformanceReport(component.flattenToShortString(), results)
    }

    private fun <T> timed(timeoutMs: Long, block: () -> T): T? {
        val executor = Executors.newSingleThreadExecutor()
        return try {
            executor.submit(Callable { block() }).get(timeoutMs, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            null
        } finally {
            executor.shutdownNow()
        }
    }
}
