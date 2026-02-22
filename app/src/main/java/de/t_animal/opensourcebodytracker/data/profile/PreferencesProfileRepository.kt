package de.t_animal.opensourcebodytracker.data.profile

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(name = "profile")

class PreferencesProfileRepository(
    private val context: Context,
) : ProfileRepository {
    private object Keys {
        val sex = stringPreferencesKey("sex")
        val dateOfBirthEpochMillis = longPreferencesKey("dateOfBirthEpochMillis")
        val heightCm = floatPreferencesKey("heightCm")
    }

    override val profileFlow: Flow<UserProfile?> = context.profileDataStore.data.map { prefs ->
        val sexName = prefs[Keys.sex]
        val dobMillis = prefs[Keys.dateOfBirthEpochMillis]
        val heightCm = prefs[Keys.heightCm]

        if (sexName.isNullOrBlank() || dobMillis == null || heightCm == null) {
            null
        } else {
            val sex = runCatching { Sex.valueOf(sexName) }.getOrNull() ?: return@map null
            UserProfile(
                sex = sex,
                dateOfBirthEpochMillis = dobMillis,
                heightCm = heightCm,
            )
        }
    }

    override suspend fun saveProfile(profile: UserProfile) {
        context.profileDataStore.edit { prefs ->
            prefs[Keys.sex] = profile.sex.name
            prefs[Keys.dateOfBirthEpochMillis] = profile.dateOfBirthEpochMillis
            prefs[Keys.heightCm] = profile.heightCm
        }
    }
}
