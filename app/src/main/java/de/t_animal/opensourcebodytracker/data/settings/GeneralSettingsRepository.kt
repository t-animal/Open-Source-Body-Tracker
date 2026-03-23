package de.t_animal.opensourcebodytracker.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import de.t_animal.opensourcebodytracker.core.model.GeneralSettings
import de.t_animal.opensourcebodytracker.core.model.UnitSystem
import de.t_animal.opensourcebodytracker.di.SettingsDataStore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface GeneralSettingsRepository {
    val settingsFlow: Flow<GeneralSettings>

    suspend fun saveSettings(settings: GeneralSettings)

    suspend fun updateSettings(transform: (GeneralSettings) -> GeneralSettings)
}

class PreferencesGeneralSettingsRepository @Inject constructor(
    @param:SettingsDataStore private val dataStore: DataStore<Preferences>,
) : GeneralSettingsRepository {

    override val settingsFlow: Flow<GeneralSettings> = dataStore.data.map { it.toGeneralSettings() }

    override suspend fun saveSettings(settings: GeneralSettings) {
        dataStore.edit { it.applyGeneralSettings(settings) }
    }

    override suspend fun updateSettings(transform: (GeneralSettings) -> GeneralSettings) {
        dataStore.edit { prefs ->
            prefs.applyGeneralSettings(transform(prefs.toGeneralSettings()))
        }
    }

    private fun Preferences.toGeneralSettings(): GeneralSettings {
        val defaults = GeneralSettings()
        return GeneralSettings(
            onboardingCompleted = this[SettingsKeys.onboardingCompleted] ?: defaults.onboardingCompleted,
            isDemoMode = this[SettingsKeys.isDemoMode] ?: defaults.isDemoMode,
            unitSystem = this[SettingsKeys.unitSystem]?.let {
                runCatching { UnitSystem.valueOf(it) }.getOrNull()
            } ?: defaults.unitSystem,
        )
    }

    private fun MutablePreferences.applyGeneralSettings(settings: GeneralSettings) {
        this[SettingsKeys.onboardingCompleted] = settings.onboardingCompleted
        this[SettingsKeys.isDemoMode] = settings.isDemoMode
        this[SettingsKeys.unitSystem] = settings.unitSystem.name
    }
}
