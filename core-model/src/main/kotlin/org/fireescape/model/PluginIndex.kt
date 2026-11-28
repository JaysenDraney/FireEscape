package org.fireescape.model

import org.json.JSONObject
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

/** One plugin entry in an index. */
data class IndexEntry(
    val id: String,
    val name: String,
    /** Class contract, e.g. "spike/1". */
    val api: String,
    val licenseId: String,
    val sourceUrl: String,
    val apkUrl: String,
    val apkSha256: String,
    val versionCode: Int,
)

/** A verified index: its entries and the key that signed it. */
data class PluginIndex(val entries: List<IndexEntry>, val signingKey: PublicKey)

class IndexException(message: String) : Exception(message)

/**
 * The signed index format (M0.7). An index is an envelope
 * `{alg, key, payload, sig}` where `payload` is the index JSON as a string and `sig`
 * covers the exact payload bytes — no canonicalization ambiguity. The payload rules
 * are published as JSON Schema in docs/schema/plugin-index.schema.json and implemented
 * by [validatePayload].
 */
object IndexFormat {
    const val ALG = "SHA256withECDSA"

    /** OSI-approved license identifiers an index may list. */
    val osiLicenses = setOf(
        "Apache-2.0", "MIT", "0BSD", "BSD-2-Clause", "BSD-3-Clause", "ISC", "Zlib",
        "MPL-2.0", "GPL-2.0-only", "GPL-3.0-only", "LGPL-2.1-only", "LGPL-3.0-only",
        "AGPL-3.0-only", "EPL-2.0", "Artistic-2.0", "PostgreSQL", "NCSA", "AFL-3.0",
        "BSL-1.0", "WTFPL", "Unlicense", "CC0-1.0",
    )

    /**
     * Parses and verifies a signed envelope. When [pinnedKey] is given (the default
     * index), the envelope's key must equal it. Throws [IndexException] on any failure;
     * a failed verification never yields entries.
     */
    fun parse(envelopeJson: String, pinnedKey: PublicKey? = null): PluginIndex {
        val envelope = try {
            JSONObject(envelopeJson)
        } catch (e: Exception) {
            throw IndexException("envelope is not JSON")
        }
        if (envelope.optString("alg") != ALG) throw IndexException("unsupported algorithm")
        val keyBytes = decode(envelope.optString("key"), "key")
        val payload = envelope.optString("payload")
        if (payload.isEmpty()) throw IndexException("missing payload")
        val sig = decode(envelope.optString("sig"), "sig")
        val key = try {
            KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(keyBytes))
        } catch (e: Exception) {
            throw IndexException("bad signing key")
        }
        if (pinnedKey != null && key != pinnedKey) {
            throw IndexException("index key does not match the pinned key")
        }
        val verifies = try {
            Signature.getInstance(ALG).apply {
                initVerify(key)
                update(payload.toByteArray(Charsets.UTF_8))
            }.verify(sig)
        } catch (e: Exception) {
            throw IndexException("signature check failed")
        }
        if (!verifies) throw IndexException("signature does not verify")
        return PluginIndex(validatePayload(payload), key)
    }

    /** Structural validation implementing the published JSON Schema. */
    fun validatePayload(payload: String): List<IndexEntry> {
        val root = try {
            JSONObject(payload)
        } catch (e: Exception) {
            throw IndexException("payload is not JSON")
        }
        if (root.optInt("schemaVersion", -1) != 1) throw IndexException("schemaVersion must be 1")
        val plugins = root.optJSONArray("plugins") ?: throw IndexException("missing plugins array")
        val entries = mutableListOf<IndexEntry>()
        val ids = mutableSetOf<String>()
        for (i in 0 until plugins.length()) {
            val o = plugins.optJSONObject(i) ?: throw IndexException("plugin #$i is not an object")
            fun req(field: String): String {
                val value = o.optString(field)
                if (value.isEmpty()) throw IndexException("plugin #$i missing $field")
                return value
            }
            val id = req("id")
            if (!ids.add(id)) throw IndexException("duplicate plugin id $id")
            val api = req("api")
            if (PluginMetadata.validate(PluginDescriptor(id, "$id.Service", api, id)) == null) {
                throw IndexException("plugin $id has unknown or malformed api $api")
            }
            val license = req("licenseId")
            if (license !in osiLicenses) throw IndexException("plugin $id license not OSI-approved: $license")
            val source = req("sourceUrl")
            if (!source.startsWith("https://")) throw IndexException("plugin $id sourceUrl must be https")
            val apkUrl = req("apkUrl")
            if (!apkUrl.startsWith("https://")) throw IndexException("plugin $id apkUrl must be https")
            val sha = req("apkSha256")
            if (!sha.matches(Regex("[0-9a-f]{64}"))) throw IndexException("plugin $id apkSha256 malformed")
            val version = o.optInt("versionCode", -1)
            if (version < 1) throw IndexException("plugin $id versionCode must be at least 1")
            entries.add(IndexEntry(id, req("name"), api, license, source, apkUrl, sha, version))
        }
        return entries
    }

    /** Builds a signed envelope. Publishers and tests use this; [sign] produces the raw signature. */
    fun envelope(payload: String, key: PublicKey, sign: (ByteArray) -> ByteArray): String {
        val envelope = JSONObject()
        envelope.put("alg", ALG)
        envelope.put("key", Base64.getEncoder().encodeToString(key.encoded))
        envelope.put("payload", payload)
        envelope.put("sig", Base64.getEncoder().encodeToString(sign(payload.toByteArray(Charsets.UTF_8))))
        return envelope.toString()
    }

    private fun decode(value: String, field: String): ByteArray {
        if (value.isEmpty()) throw IndexException("missing $field")
        return try {
            Base64.getDecoder().decode(value)
        } catch (e: Exception) {
            throw IndexException("bad base64 in $field")
        }
    }
}
