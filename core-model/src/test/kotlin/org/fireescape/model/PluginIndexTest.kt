package org.fireescape.model

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.ECGenParameterSpec

/** M0.7 L1: schema validation and signature checks against valid, tampered and wrong-key fixtures. */
class PluginIndexTest {

    private fun keyPair(): KeyPair = KeyPairGenerator.getInstance("EC").apply {
        initialize(ECGenParameterSpec("secp256r1"))
    }.generateKeyPair()

    private fun sign(pair: KeyPair, bytes: ByteArray): ByteArray =
        Signature.getInstance(IndexFormat.ALG).apply {
            initSign(pair.private)
            update(bytes)
        }.sign()

    private fun entryJson(id: String = "org.fireescape.spike"): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", "Spike sample")
        put("api", "spike/1")
        put("licenseId", "Apache-2.0")
        put("sourceUrl", "https://github.com/JaysenDraney/FireEscape")
        put("apkUrl", "https://example.invalid/spike.apk")
        put("apkSha256", "a".repeat(64))
        put("versionCode", 1)
    }

    private fun payload(vararg entries: JSONObject): String = JSONObject().apply {
        put("schemaVersion", 1)
        put("plugins", JSONArray(entries.toList()))
    }.toString()

    private fun expectFailure(message: String, block: () -> Unit) {
        try {
            block()
            fail("expected IndexException: $message")
        } catch (expected: IndexException) {
            assertTrue(expected.message != null)
        }
    }

    @Test
    fun validIndexVerifies() {
        val pair = keyPair()
        val payload = payload(entryJson())
        val envelope = IndexFormat.envelope(payload, pair.public) { sign(pair, it) }
        val index = IndexFormat.parse(envelope)
        assertEquals(1, index.entries.size)
        assertEquals("org.fireescape.spike", index.entries[0].id)
        assertEquals(pair.public, index.signingKey)
    }

    @Test
    fun pinnedKeyAcceptsTheRightKey() {
        val pair = keyPair()
        val payload = payload(entryJson())
        val envelope = IndexFormat.envelope(payload, pair.public) { sign(pair, it) }
        assertEquals(1, IndexFormat.parse(envelope, pinnedKey = pair.public).entries.size)
    }

    @Test
    fun tamperedPayloadIsRejected() {
        val pair = keyPair()
        val payload = payload(entryJson())
        val envelope = JSONObject(IndexFormat.envelope(payload, pair.public) { sign(pair, it) })
        envelope.put("payload", payload.replace("Spike sample", "Evil sample"))
        expectFailure("tampered payload") { IndexFormat.parse(envelope.toString()) }
    }

    @Test
    fun tamperedSignatureIsRejected() {
        val pair = keyPair()
        val payload = payload(entryJson())
        val envelope = JSONObject(IndexFormat.envelope(payload, pair.public) { sign(pair, it) })
        val sig = envelope.getString("sig")
        envelope.put("sig", sig.dropLast(2) + "AA")
        expectFailure("tampered signature") { IndexFormat.parse(envelope.toString()) }
    }

    @Test
    fun wrongKeyIsRejectedAgainstThePin() {
        val real = keyPair()
        val other = keyPair()
        val payload = payload(entryJson())
        val envelope = IndexFormat.envelope(payload, other.public) { sign(other, it) }
        expectFailure("wrong key") { IndexFormat.parse(envelope, pinnedKey = real.public) }
    }

    @Test
    fun missingFieldsAreRejected() {
        for (field in listOf("id", "name", "api", "licenseId", "sourceUrl", "apkUrl", "apkSha256")) {
            val broken = entryJson().apply { remove(field) }
            expectFailure("missing $field") { IndexFormat.validatePayload(payload(broken)) }
        }
    }

    @Test
    fun nonOsiLicenseIsRejected() {
        val broken = entryJson().apply { put("licenseId", "Proprietary") }
        expectFailure("non-OSI license") { IndexFormat.validatePayload(payload(broken)) }
    }

    @Test
    fun nonHttpsSourceIsRejected() {
        val broken = entryJson().apply { put("sourceUrl", "http://example.org/repo") }
        expectFailure("non-https source") { IndexFormat.validatePayload(payload(broken)) }
    }

    @Test
    fun duplicateIdsAreRejected() {
        expectFailure("duplicate id") {
            IndexFormat.validatePayload(payload(entryJson(), entryJson()))
        }
    }

    @Test
    fun unknownApiIsRejected() {
        val broken = entryJson().apply { put("api", "hologram/1") }
        expectFailure("unknown api") { IndexFormat.validatePayload(payload(broken)) }
    }

    @Test
    fun badSchemaVersionIsRejected() {
        val broken = JSONObject(payload(entryJson())).apply { put("schemaVersion", 2) }
        expectFailure("schema version") { IndexFormat.validatePayload(broken.toString()) }
    }

    @Test
    fun malformedShaIsRejected() {
        val broken = entryJson().apply { put("apkSha256", "xyz") }
        expectFailure("malformed sha") { IndexFormat.validatePayload(payload(broken)) }
    }

    @Test
    fun garbageEnvelopeIsRejected() {
        expectFailure("not JSON") { IndexFormat.parse("this is not json") }
    }
}
