package de.t_animal.opensourcebodytracker.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MeasurementSettings
import de.t_animal.opensourcebodytracker.di.SettingsDataStore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface MeasurementSettingsRepository {
    val settingsFlow: Flow<MeasurementSettings>

    suspend fun saveSettings(settings: MeasurementSettings)

    suspend fun updateSettings(transform: (MeasurementSettings) -> MeasurementSettings)
}

class PreferencesMeasurementSettingsRepository @Inject constructor(
    @SettingsDataStore private val dataStore: DataStore<Preferences>,
) : MeasurementSettingsRepository {

    override val settingsFlow: Flow<MeasurementSettings> = dataStore.data.map { it.toMeasurementSettings() }

    override suspend fun saveSettings(settings: MeasurementSettings) {
        dataStore.edit { it.applyMeasurementSettings(settings) }
    }

    override suspend fun updateSettings(transform: (MeasurementSettings) -> MeasurementSettings) {
        dataStore.edit { prefs ->
            prefs.applyMeasurementSettings(transform(prefs.toMeasurementSettings()))
        }
    }

    private fun Preferences.toMeasurementSettings(): MeasurementSettings {
        val defaults = MeasurementSettings()
        return MeasurementSettings(
            bmiEnabled = this[SettingsKeys.bmiEnabled] ?: defaults.bmiEnabled,
            navyBodyFatEnabled = this[SettingsKeys.navyBodyFatEnabled] ?: defaults.navyBodyFatEnabled,
            skinfoldBodyFatEnabled = this[SettingsKeys.skinfoldBodyFatEnabled] ?: defaults.skinfoldBodyFatEnabled,
            waistHipRatioEnabled = this[SettingsKeys.waistHipRatioEnabled] ?: defaults.waistHipRatioEnabled,
            waistHeightRatioEnabled = this[SettingsKeys.waistHeightRatioEnabled] ?: defaults.waistHeightRatioEnabled,
            enabledMeasurements = parseEnumSet(
                raw = this[SettingsKeys.enabledMeasurements],
                values = MeasuredBodyMetric.entries,
                fallback = defaults.enabledMeasurements,
            ),
            visibleInAnalysis = parseBodyMetricSet(
                raw = this[SettingsKeys.visibleInAnalysis],
                fallback = defaults.visibleInAnalysis,
            ),
            visibleInTable = parseBodyMetricSet(
                raw = this[SettingsKeys.visibleInTable],
                fallback = defaults.visibleInTable,
            ),
        )
    }

    private fun MutablePreferences.applyMeasurementSettings(settings: MeasurementSettings) {
        this[SettingsKeys.bmiEnabled] = settings.bmiEnabled
        this[SettingsKeys.navyBodyFatEnabled] = settings.navyBodyFatEnabled
        this[SettingsKeys.skinfoldBodyFatEnabled] = settings.skinfoldBodyFatEnabled
        this[SettingsKeys.waistHipRatioEnabled] = settings.waistHipRatioEnabled
        this[SettingsKeys.waistHeightRatioEnabled] = settings.waistHeightRatioEnabled
        this[SettingsKeys.enabledMeasurements] = settings.enabledMeasurements.mapTo(mutableSetOf()) { it.name }
        this[SettingsKeys.visibleInAnalysis] = settings.visibleInAnalysis.mapTo(mutableSetOf()) { it.storageName() }
        this[SettingsKeys.visibleInTable] = settings.visibleInTable.mapTo(mutableSetOf()) { it.storageName() }
    }
}
