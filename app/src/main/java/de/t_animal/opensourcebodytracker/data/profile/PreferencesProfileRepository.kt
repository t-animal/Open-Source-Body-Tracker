package de.t_animal.opensourcebodytracker.data.profile

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.map

private val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(name = "profile")

class PreferencesProfileRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ProfileRepository {
    private object Keys {
        val sex = stringPreferencesKey("sex")
        val dateOfBirth = stringPreferencesKey("dateOfBirth")
        val heightCm = floatPreferencesKey("heightCm")
    }

    override val profileFlow: Flow<UserProfile?> = context.profileDataStore.data.map { prefs ->
        val sexName = prefs[Keys.sex]
        val dateOfBirthText = prefs[Keys.dateOfBirth]
        val heightCm = prefs[Keys.heightCm]

        if (sexName.isNullOrBlank() || dateOfBirthText.isNullOrBlank() || heightCm == null) {
            null
        } else {
            val sex = runCatching { Sex.valueOf(sexName) }.getOrNull() ?: return@map null
            val dateOfBirth = runCatching { LocalDate.parse(dateOfBirthText) }.getOrNull() ?: return@map null
            UserProfile(
                sex = sex,
                dateOfBirth = dateOfBirth,
                heightCm = heightCm,
            )
        }
    }

    override suspend fun saveProfile(profile: UserProfile) {
        context.profileDataStore.edit { prefs ->
            prefs[Keys.sex] = profile.sex.name
            prefs[Keys.dateOfBirth] = profile.dateOfBirth.toString()
            prefs[Keys.heightCm] = profile.heightCm
        }
    }
}
