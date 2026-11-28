package org.fireescape.launcher.plugin

import android.content.Context
import org.fireescape.model.IndexFormat
import org.fireescape.model.PluginIndex
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

/**
 * The index the launcher ships with. Dev-sample signing key (decision D11 in the
 * autonomy decision log): the private part lives outside the repo and this is NOT a
 * production key. Production index signing is a later owner-run step.
 */
object DefaultIndex {
    const val PINNED_KEY_BASE64 =
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEbiM2f+MyxkRtaFkil8b4lChEacgX1i+uVEUfzGkAIlHLIS4Vg43ZoogzujDVBZb4fOkfxv49Qq6obGOdvteAyg=="

    fun load(context: Context): PluginIndex {
        val json = context.assets.open("default-index.json").readBytes().decodeToString()
        val key = KeyFactory.getInstance("EC").generatePublic(
            X509EncodedKeySpec(Base64.getDecoder().decode(PINNED_KEY_BASE64)),
        )
        return IndexFormat.parse(json, pinnedKey = key)
    }
}
