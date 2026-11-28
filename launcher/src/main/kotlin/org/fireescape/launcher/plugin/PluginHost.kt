package org.fireescape.launcher.plugin

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.RemoteException
import org.fireescape.model.Grant
import org.fireescape.model.InstalledPlugin
import org.fireescape.model.PermissionBroker
import org.fireescape.model.PluginDescriptor
import org.fireescape.model.PluginMetadata
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/** One published class contract: its discovery action and its call timeout. */
data class PluginContract(val apiName: String, val action: String, val timeoutMs: Long)

/**
 * Finds, binds and talks to plugins (ADR 0004). Discovery is by per-class intent action;
 * declarations are validated by [PluginMetadata]. A plugin whose major version the
 * launcher does not support is listed as incompatible and never bound. Calls run off the
 * main thread with the contract's timeout, so a crashing, hanging or late plugin never
 * freezes the launcher. Each call gets its own thread: a hung call cannot starve later
 * calls. The host unbinds plugins it is not using via [unbind].
 */
class PluginHost(context: Context) {
    private val appContext = context.applicationContext
    private val executor = Executors.newCachedThreadPool { runnable ->
        Thread(runnable, "plugin-host").apply { isDaemon = true }
    }
    private val connections = ConcurrentHashMap<ComponentName, ServiceConnection>()
    private val binders = ConcurrentHashMap<ComponentName, IBinder>()

    /** All validated declarations, compatible or not. Malformed declarations are skipped. */
    fun discover(): List<InstalledPlugin> {
        val pm = appContext.packageManager
        val found = mutableListOf<InstalledPlugin>()
        for (contract in CONTRACTS) {
            val services = pm.queryIntentServices(Intent(contract.action), PackageManager.GET_META_DATA)
            for (info in services) {
                val meta = info.serviceInfo.metaData ?: continue
                val descriptor = PluginDescriptor(
                    packageName = info.serviceInfo.packageName,
                    serviceClassName = info.serviceInfo.name,
                    api = meta.getString(PluginMetadata.KEY_API) ?: continue,
                    displayName = info.serviceInfo.loadLabel(pm).toString(),
                    licenseId = meta.getString(PluginMetadata.KEY_LICENSE),
                    sourceUrl = meta.getString(PluginMetadata.KEY_SOURCE),
                    declaredCapabilities = (meta.getString(PluginMetadata.KEY_CAPABILITIES) ?: "")
                        .split("[,\\s]+".toRegex())
                        .filter { it.isNotBlank() }
                        .mapNotNull { PluginMetadata.parseCapability(it) }
                        .toSet(),
                )
                PluginMetadata.validate(descriptor)?.let { found.add(it) }
            }
        }
        return found
    }

    /** Binds a compatible plugin and waits for the connection. Incompatible: never bound. */
    fun bind(plugin: InstalledPlugin, waitMs: Long = 5_000): Boolean {
        if (!plugin.compatible) return false
        val component = componentOf(plugin)
        if (binders.containsKey(component)) return true
        val latch = CountDownLatch(1)
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                binders[name] = service
                latch.countDown()
            }

            override fun onServiceDisconnected(name: ComponentName) {
                binders.remove(name)
            }
        }
        connections[component] = connection
        val intent = Intent().setComponent(component)
        if (!appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
            connections.remove(component)
            return false
        }
        return latch.await(waitMs, TimeUnit.MILLISECONDS)
    }

    fun isBound(plugin: InstalledPlugin): Boolean = binders.containsKey(componentOf(plugin))

    /**
     * Runs one call off the main thread under the contract's timeout. Throws
     * [TimeoutException] when the plugin is late, and the binder's own exception
     * (e.g. [RemoteException]) when it crashed. The host itself stays usable.
     */
    fun <T> call(plugin: InstalledPlugin, block: (IBinder) -> T): T {
        val component = componentOf(plugin)
        val contract = CONTRACTS.firstOrNull { it.apiName == plugin.descriptor.api.substringBefore("/") }
            ?: throw IllegalArgumentException("no contract for ${plugin.descriptor.api}")
        val future = executor.submit(Callable {
            val binder = binders[component] ?: throw RemoteException("plugin not bound: $component")
            block(binder)
        })
        return try {
            future.get(contract.timeoutMs, TimeUnit.MILLISECONDS)
        } catch (e: TimeoutException) {
            future.cancel(true)
            throw e
        } catch (e: ExecutionException) {
            throw e.cause ?: e
        }
    }

    /**
     * Hands data to a plugin only while the grant is active. The broker recomputes the
     * active set on every call, so withdrawing a grant stops delivery from the next
     * call on. Returns null when the grant is not active: nothing crosses the interface.
     */
    fun <T> callWithGrant(
        plugin: InstalledPlugin,
        grant: Grant,
        granted: Set<Grant>,
        block: (IBinder) -> T,
    ): T? {
        if (grant !in PermissionBroker.activeGrants(plugin, granted)) return null
        return call(plugin, block)
    }

    /** Releases a plugin the host is not using. */
    fun unbind(plugin: InstalledPlugin) {
        val component = componentOf(plugin)
        connections.remove(component)?.let { appContext.unbindService(it) }
        binders.remove(component)
    }

    fun shutdown() {
        executor.shutdownNow()
    }

    private fun componentOf(plugin: InstalledPlugin) =
        ComponentName(plugin.descriptor.packageName, plugin.descriptor.serviceClassName)

    companion object {
        /** Published contracts; see plugin-api/README.md. New classes register here. */
        val CONTRACTS = listOf(
            PluginContract("spike", "org.fireescape.plugin.SPIKE", 500),
        )
    }
}
