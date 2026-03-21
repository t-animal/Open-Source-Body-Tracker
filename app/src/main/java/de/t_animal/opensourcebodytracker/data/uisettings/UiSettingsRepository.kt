package de.t_animal.opensourcebodytracker.data.uisettings

import kotlinx.coroutines.flow.Flow

interface UiSettingsRepository {
    val settingsFlow: Flow<UiSettings>

    suspend fun saveSettings(settings: UiSettings)

    suspend fun updateSettings(transform: (UiSettings) -> UiSettings)
}
