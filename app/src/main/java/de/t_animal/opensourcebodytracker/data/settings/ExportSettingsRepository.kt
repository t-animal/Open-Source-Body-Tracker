package de.t_animal.opensourcebodytracker.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import de.t_animal.opensourcebodytracker.core.model.ExportSettings
import de.t_animal.opensourcebodytracker.di.SettingsDataStore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ExportSettingsRepository {
    val settingsFlow: Flow<ExportSettings>

    suspend fun saveSettings(settings: ExportSettings)

    suspend fun updateSettings(transform: (ExportSettings) -> ExportSettings)
}

class PreferencesExportSettingsRepository @Inject constructor(
    @SettingsDataStore private val dataStore: DataStore<Preferences>,
) : ExportSettingsRepository {

    override val settingsFlow: Flow<ExportSettings> = dataStore.data.map { it.toExportSettings() }

    override suspend fun saveSettings(settings: ExportSettings) {
        dataStore.edit { it.applyExportSettings(settings) }
    }

    override suspend fun updateSettings(transform: (ExportSettings) -> ExportSettings) {
        dataStore.edit { prefs ->
            prefs.applyExportSettings(transform(prefs.toExportSettings()))
        }
    }

    private fun Preferences.toExportSettings(): ExportSettings {
        val defaults = ExportSettings()
        return ExportSettings(
            exportToDeviceStorageEnabled =
                this[SettingsKeys.exportToDeviceStorageEnabled] ?: defaults.exportToDeviceStorageEnabled,
            exportFolderUri = this[SettingsKeys.exportFolderUri] ?: defaults.exportFolderUri,
            automaticExportEnabled = this[SettingsKeys.automaticExportEnabled] ?: defaults.automaticExportEnabled,
            automaticExportPending = this[SettingsKeys.automaticExportPending] ?: defaults.automaticExportPending,
            lastAutomaticExportError =
                this[SettingsKeys.lastAutomaticExportError] ?: defaults.lastAutomaticExportError,
        )
    }

    private fun MutablePreferences.applyExportSettings(settings: ExportSettings) {
        this[SettingsKeys.exportToDeviceStorageEnabled] = settings.exportToDeviceStorageEnabled
        if (settings.exportFolderUri.isNullOrBlank()) {
            this.remove(SettingsKeys.exportFolderUri)
        } else {
            this[SettingsKeys.exportFolderUri] = settings.exportFolderUri
        }
        this[SettingsKeys.automaticExportEnabled] = settings.automaticExportEnabled
        this[SettingsKeys.automaticExportPending] = settings.automaticExportPending
        if (settings.lastAutomaticExportError.isNullOrBlank()) {
            this.remove(SettingsKeys.lastAutomaticExportError)
        } else {
            this[SettingsKeys.lastAutomaticExportError] = settings.lastAutomaticExportError
        }
    }
}
