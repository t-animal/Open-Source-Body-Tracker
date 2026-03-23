package de.t_animal.opensourcebodytracker.data.export

import android.content.Context
import android.util.Base64
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ExportPasswordCrypto @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val aead: Aead by lazy {
        AeadConfig.register()
        AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, KEYSET_PREF_NAME)
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri("android-keystore://$MASTER_KEY_ALIAS")
            .build()
            .keysetHandle
            .getPrimitive(Aead::class.java)
    }

    fun encrypt(plainText: String): String {
        val ciphertext = aead.encrypt(plainText.toByteArray(Charsets.UTF_8), ASSOCIATED_DATA)
        return Base64.encodeToString(ciphertext, Base64.NO_WRAP)
    }

    fun decrypt(ciphertextBase64: String): String {
        val ciphertext = Base64.decode(ciphertextBase64, Base64.NO_WRAP)
        return String(aead.decrypt(ciphertext, ASSOCIATED_DATA), Charsets.UTF_8)
    }

    private companion object {
        const val KEYSET_NAME = "export_password_keyset"
        const val KEYSET_PREF_NAME = "export_password_keyset_prefs"
        const val MASTER_KEY_ALIAS = "export_password_master_key"
        val ASSOCIATED_DATA = "export_password".toByteArray(Charsets.UTF_8)
    }
}
