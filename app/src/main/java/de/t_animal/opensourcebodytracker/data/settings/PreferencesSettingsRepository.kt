package de.t_animal.opensourcebodytracker.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.DerivedBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.SettingsState
import de.t_animal.opensourcebodytracker.core.model.defaultSettingsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesSettingsRepository(
    private val context: Context,
) : SettingsRepository {

    private object Keys {
        val bmiEnabled = booleanPreferencesKey("bmiEnabled")
        val navyBodyFatEnabled = booleanPreferencesKey("navyBodyFatEnabled")
        val skinfoldBodyFatEnabled = booleanPreferencesKey("skinfoldBodyFatEnabled")
        val waistHipRatioEnabled = booleanPreferencesKey("waistHipRatioEnabled")
        val waistHeightRatioEnabled = booleanPreferencesKey("waistHeightRatioEnabled")
        val enabledMeasurements = stringSetPreferencesKey("enabledMeasurements")
        val visibleInAnalysis = stringSetPreferencesKey("visibleInAnalysis")
        val visibleInTable = stringSetPreferencesKey("visibleInTable")
    }

    override val settingsFlow: Flow<SettingsState> = context.settingsDataStore.data.map { prefs ->
        val defaults = defaultSettingsState()
        SettingsState(
            bmiEnabled = prefs[Keys.bmiEnabled] ?: defaults.bmiEnabled,
            navyBodyFatEnabled = prefs[Keys.navyBodyFatEnabled] ?: defaults.navyBodyFatEnabled,
            skinfoldBodyFatEnabled = prefs[Keys.skinfoldBodyFatEnabled] ?: defaults.skinfoldBodyFatEnabled,
            waistHipRatioEnabled = prefs[Keys.waistHipRatioEnabled] ?: defaults.waistHipRatioEnabled,
            waistHeightRatioEnabled = prefs[Keys.waistHeightRatioEnabled] ?: defaults.waistHeightRatioEnabled,
            enabledMeasurements = parseEnumSet(
                raw = prefs[Keys.enabledMeasurements],
                values = MeasuredBodyMetric.entries,
                fallback = defaults.enabledMeasurements,
            ),
            visibleInAnalysis = parseBodyMetricSet(
                raw = prefs[Keys.visibleInAnalysis],
                fallback = defaults.visibleInAnalysis,
            ),
            visibleInTable = parseBodyMetricSet(
                raw = prefs[Keys.visibleInTable],
                fallback = defaults.visibleInTable,
            ),
        )
    }

    override suspend fun saveSettings(settings: SettingsState) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.bmiEnabled] = settings.bmiEnabled
            prefs[Keys.navyBodyFatEnabled] = settings.navyBodyFatEnabled
            prefs[Keys.skinfoldBodyFatEnabled] = settings.skinfoldBodyFatEnabled
            prefs[Keys.waistHipRatioEnabled] = settings.waistHipRatioEnabled
            prefs[Keys.waistHeightRatioEnabled] = settings.waistHeightRatioEnabled
            prefs[Keys.enabledMeasurements] = settings.enabledMeasurements.mapTo(mutableSetOf()) { it.name }
            prefs[Keys.visibleInAnalysis] = settings.visibleInAnalysis.mapTo(mutableSetOf()) { it.storageName() }
            prefs[Keys.visibleInTable] = settings.visibleInTable.mapTo(mutableSetOf()) { it.storageName() }
        }
    }
}

private fun parseBodyMetricSet(
    raw: Set<String>?,
    fallback: Set<BodyMetric>,
): Set<BodyMetric> {
    if (raw == null) {
        return fallback
    }

    val measuredByStorageName = MeasuredBodyMetric.entries.associateBy { it.storageName() }
    val derivedByStorageName = DerivedBodyMetric.entries.associateBy { it.storageName() }
    return raw.mapNotNull { token ->
        measuredByStorageName[token] ?: derivedByStorageName[token]
    }.toSet()
}

private fun BodyMetric.storageName(): String = this.javaClass.simpleName + ":$name"

private fun <E : Enum<E>> parseEnumSet(
    raw: Set<String>?,
    values: List<E>,
    fallback: Set<E>,
): Set<E> {
    if (raw == null) {
        return fallback
    }

    val byName = values.associateBy { it.name }
    val parsed = raw.mapNotNull { byName[it] }.toSet()
    return if (parsed.isEmpty()) fallback else parsed
}
