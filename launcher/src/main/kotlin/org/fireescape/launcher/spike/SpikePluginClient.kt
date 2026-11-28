package org.fireescape.launcher.spike

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.RemoteException
import org.fireescape.plugin.spike.ISpikePlugin
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * M0.2 spike: the smallest plugin host that answers the transport questions.
 * Not production code. M0.5 replaces it with the real plugin host.
 */
class SpikePluginClient(private val context: Context) {

    data class Found(val component: ComponentName, val api: String?)

    sealed interface CallResult<out T> {
        data class Ok<T>(val value: T) : CallResult<T>
        data object TimedOut : CallResult<Nothing>
        data class Failed(val error: Throwable) : CallResult<Nothing>
    }

    @Volatile var plugin: ISpikePlugin? = null
        private set

    @Volatile var disconnects = 0
        private set

    @Volatile var connects = 0
        private set

    private val calls = Executors.newCachedThreadPool()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            plugin = ISpikePlugin.Stub.asInterface(service)
            connects++
        }

        override fun onServiceDisconnected(name: ComponentName) {
            plugin = null
            disconnects++
        }
    }

    /** Finds plugins by the per-class intent action and reads their declared API version. */
    fun discover(): List<Found> {
        val intent = Intent(ACTION)
        @Suppress("DEPRECATION")
        val services = context.packageManager.queryIntentServices(intent, PackageManager.GET_META_DATA)
        return services.map {
            val info = it.serviceInfo
            Found(ComponentName(info.packageName, info.name), info.metaData?.getString("fireescape.api"))
        }
    }

    fun bind(component: ComponentName): Boolean =
        context.bindService(Intent(ACTION).setComponent(component), connection, Context.BIND_AUTO_CREATE)

    fun unbind() = context.unbindService(connection)

    /**
     * Runs a call off the caller's thread and gives up after [timeoutMillis].
     * A Binder call cannot be cancelled, so a timed-out call keeps one worker thread
     * busy until the plugin answers or dies.
     */
    fun <T> call(timeoutMillis: Long, block: (ISpikePlugin) -> T): CallResult<T> {
        val p = plugin ?: return CallResult.Failed(IllegalStateException("not connected"))
        val future = calls.submit<T> { block(p) }
        return try {
            CallResult.Ok(future.get(timeoutMillis, TimeUnit.MILLISECONDS))
        } catch (e: TimeoutException) {
            CallResult.TimedOut
        } catch (e: java.util.concurrent.ExecutionException) {
            CallResult.Failed(e.cause ?: e)
        }
    }

    companion object {
        const val ACTION = "org.fireescape.plugin.SPIKE"

        fun isRemoteFailure(e: Throwable) = e is RemoteException
    }
}