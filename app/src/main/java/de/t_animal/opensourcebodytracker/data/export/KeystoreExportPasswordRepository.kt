package de.t_animal.opensourcebodytracker.data.export

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.first

private val Context.exportSecretsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "export_secrets",
)

class KeystoreExportPasswordRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val crypto: ExportPasswordCrypto,
) : ExportPasswordRepository {

    private object Keys {
        val ciphertext = stringPreferencesKey("exportPasswordTinkCiphertext")
    }

    override suspend fun getPassword(): String? {
        val ciphertextBase64 = context.exportSecretsDataStore.data.first()[Keys.ciphertext]
            ?: return null

        return runCatching { crypto.decrypt(ciphertextBase64) }
            .onFailure { clearPayload() }
            .getOrNull()
    }

    override suspend fun savePassword(password: String?) {
        if (password.isNullOrBlank()) {
            clearPayload()
            return
        }

        val ciphertextBase64 = crypto.encrypt(password)
        context.exportSecretsDataStore.edit { prefs ->
            prefs[Keys.ciphertext] = ciphertextBase64
        }
    }

    private suspend fun clearPayload() {
        context.exportSecretsDataStore.edit { it.clear() }
    }
}
