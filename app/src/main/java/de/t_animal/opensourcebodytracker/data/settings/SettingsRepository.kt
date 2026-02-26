package de.t_animal.opensourcebodytracker.data.settings

import de.t_animal.opensourcebodytracker.core.model.SettingsState
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settingsFlow: Flow<SettingsState>

    suspend fun saveSettings(settings: SettingsState)
}
