package org.fireescape.launcher.plugin

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.content.pm.SigningInfo
import org.fireescape.model.IndexEntry
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

sealed interface InstallResult {
    data object Installed : InstallResult
    data class Rejected(val reason: String) : InstallResult
    data class Failed(val reason: String) : InstallResult
}

/**
 * Installs a plugin APK from an index entry. Refuses an APK whose sha256 differs from
 * the entry, whose package differs from the entry id, or — for an update — whose
 * signing certificates differ from the installed version's. The launcher controls the
 * install; the index never installs anything by itself.
 */
class PluginInstaller(private val context: Context) {
    private val pm = context.packageManager

    sealed interface StageResult {
        data class Staged(val sessionId: Int) : StageResult
        data class NotStaged(val result: InstallResult) : StageResult
    }

    /**
     * Validates the APK against the entry and writes a staged install session, without
     * committing. Committing an install of another package needs INSTALL_PACKAGES, which
     * the launcher does not hold; production flows hand the staged session to the system
     * installer UI, and tests commit through the shell.
     */
    fun stage(entry: IndexEntry, apk: ByteArray): StageResult {
        if (sha256(apk) != entry.apkSha256) {
            return StageResult.NotStaged(InstallResult.Rejected("APK sha256 does not match the index entry"))
        }
        val archive = File(context.cacheDir, "incoming-${entry.id}.apk")
        archive.writeBytes(apk)
        val info = pm.getPackageArchiveInfo(archive.absolutePath, PackageManager.GET_SIGNING_CERTIFICATES)
            ?: return StageResult.NotStaged(InstallResult.Failed("APK cannot be parsed"))
        if (info.packageName != entry.id) {
            return StageResult.NotStaged(InstallResult.Rejected("APK package does not match the index entry id"))
        }
        val incoming = certHashes(info.signingInfo)
        try {
            val installed = pm.getPackageInfo(entry.id, PackageManager.GET_SIGNING_CERTIFICATES)
            if (certHashes(installed.signingInfo) != incoming) {
                return StageResult.NotStaged(
                    InstallResult.Rejected("update signed with a different key than the installed version"),
                )
            }
        } catch (e: PackageManager.NameNotFoundException) {
            // New install: no installed key to compare against.
        }
        val installer = pm.packageInstaller
        // A full-install session replaces an existing package when the keys match.
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        params.setAppPackageName(entry.id)
        val sessionId = installer.createSession(params)
        installer.openSession(sessionId).use { session ->
            session.openWrite("base.apk", 0, apk.size.toLong()).use { out ->
                out.write(apk)
                session.fsync(out)
            }
        }
        return StageResult.Staged(sessionId)
    }

    fun install(entry: IndexEntry, apk: ByteArray): InstallResult =
        when (val staged = stage(entry, apk)) {
            is StageResult.NotStaged -> staged.result
            is StageResult.Staged -> commitSession(staged.sessionId)
        }

    fun abandonSession(sessionId: Int) {
        pm.packageInstaller.abandonSession(sessionId)
    }

    fun commitSession(sessionId: Int): InstallResult {
        val installer = pm.packageInstaller
        installer.openSession(sessionId).use { session ->
            val latch = CountDownLatch(1)
            var status = PackageInstaller.STATUS_FAILURE
            var message = ""
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(c: Context, intent: Intent) {
                    status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
                    message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE) ?: ""
                    latch.countDown()
                }
            }
            val action = "org.fireescape.launcher.INSTALL_STATUS_$sessionId"
            context.registerReceiver(receiver, IntentFilter(action), Context.RECEIVER_EXPORTED)
            try {
                val pending = PendingIntent.getBroadcast(
                    context,
                    sessionId,
                    Intent(action).setPackage(context.packageName),
                    PendingIntent.FLAG_MUTABLE,
                )
                session.commit(pending.intentSender)
                if (!latch.await(60, TimeUnit.SECONDS)) {
                    return InstallResult.Failed("install timed out")
                }
            } finally {
                context.unregisterReceiver(receiver)
            }
            return when (status) {
                PackageInstaller.STATUS_SUCCESS -> InstallResult.Installed
                PackageInstaller.STATUS_FAILURE_CONFLICT -> InstallResult.Rejected("install conflict: $message")
                else -> InstallResult.Failed("install failed ($status): $message")
            }
        }
    }

    private fun certHashes(info: SigningInfo?): Set<String> =
        (info?.apkContentsSigners ?: emptyArray()).map { sha256(it.toByteArray()) }.toSet()

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}
