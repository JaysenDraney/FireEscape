package org.fireescape.launcher.plugin

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.fireescape.model.IndexEntry
import org.fireescape.model.IndexFormat
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.MessageDigest
import java.security.spec.ECGenParameterSpec

/**
 * M0.7 L4: install a plugin from a test index on the emulator; refuse an update signed
 * with another key; verify the shipped default index against its pinned key.
 */
@RunWith(AndroidJUnit4::class)
class IndexInstallTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val target = instrumentation.targetContext
    private val spikePkg = "org.fireescape.spike.plugin"

    private fun installedSpikeApk(): ByteArray =
        File(target.packageManager.getApplicationInfo(spikePkg, 0).sourceDir).readBytes()

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    private fun entry(apk: ByteArray): IndexEntry = IndexEntry(
        id = spikePkg,
        name = "Spike",
        api = "spike/1",
        licenseId = "Apache-2.0",
        sourceUrl = "https://github.com/JaysenDraney/FireEscape",
        apkUrl = "https://example.invalid/spike.apk",
        apkSha256 = sha256(apk),
        versionCode = 1,
    )

    private fun signedTestIndex(entry: IndexEntry): String {
        val pair = KeyPairGenerator.getInstance("EC").apply {
            initialize(ECGenParameterSpec("secp256r1"))
        }.generateKeyPair()
        val plugin = JSONObject()
            .put("id", entry.id).put("name", entry.name).put("api", entry.api)
            .put("licenseId", entry.licenseId).put("sourceUrl", entry.sourceUrl)
            .put("apkUrl", entry.apkUrl).put("apkSha256", entry.apkSha256)
            .put("versionCode", entry.versionCode)
        val payload = JSONObject().put("schemaVersion", 1).put("plugins", JSONArray().put(plugin)).toString()
        return IndexFormat.envelope(payload, pair.public) { bytes ->
            Signature.getInstance(IndexFormat.ALG).apply {
                initSign(pair.private)
                update(bytes)
            }.sign()
        }
    }

    @Test
    fun installFromATestIndexSucceeds() {
        val apk = installedSpikeApk()
        val index = IndexFormat.parse(signedTestIndex(entry(apk)))
        // Our gate validates the entry and stages the APK.
        val installer = PluginInstaller(target)
        val staged = installer.stage(index.entries[0], apk)
        assertTrue("expected staging, got $staged", staged is PluginInstaller.StageResult.Staged)
        installer.abandonSession((staged as PluginInstaller.StageResult.Staged).sessionId)
        // The privileged install itself is shell-owned, exactly like adb install:
        // sessions belong to their creator, and the launcher holds no INSTALL_PACKAGES.
        val source = target.packageManager.getApplicationInfo(spikePkg, 0).sourceDir
        shell("cp $source /data/local/tmp/spike-index-test.apk")
        val created = shell("pm install-create -r")
        val id = Regex("\\[(\\d+)]").find(created)?.groupValues?.get(1)
        assertTrue("install-create said: $created", id != null)
        shell("pm install-write $id base.apk /data/local/tmp/spike-index-test.apk")
        val committed = shell("pm install-commit $id")
        assertTrue("install-commit said: $committed", committed.contains("Success"))
        assertNotNull(target.packageManager.getPackageInfo(spikePkg, 0))
    }

    private fun shell(command: String): String {
        val pfd = instrumentation.uiAutomation.executeShellCommand(command)
        return android.os.ParcelFileDescriptor.AutoCloseInputStream(pfd).use {
            it.readBytes().decodeToString()
        }
    }

    @Test
    fun updateSignedWithAnotherKeyIsRefused() {
        val wrongKeyApk = instrumentation.context.assets.open("spike-wrongkey.apk").readBytes()
        val result = PluginInstaller(target).install(entry(wrongKeyApk), wrongKeyApk)
        assertTrue("expected rejection, got $result", result is InstallResult.Rejected)
        // The installed version is untouched.
        assertNotNull(target.packageManager.getPackageInfo(spikePkg, 0))
    }

    @Test
    fun shaMismatchIsRefused() {
        val apk = installedSpikeApk()
        val tampered = entry(apk).copy(apkSha256 = "b".repeat(64))
        val result = PluginInstaller(target).install(tampered, apk)
        assertTrue(result is InstallResult.Rejected)
    }

    @Test
    fun defaultIndexVerifiesAgainstThePinnedKey() {
        val index = DefaultIndex.load(target)
        assertEquals(1, index.entries.size)
        assertTrue(index.entries.all {
            it.licenseId in IndexFormat.osiLicenses && it.sourceUrl.startsWith("https://")
        })
    }
}
