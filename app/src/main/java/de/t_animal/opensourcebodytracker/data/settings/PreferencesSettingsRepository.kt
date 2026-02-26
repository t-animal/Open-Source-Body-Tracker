package de.t_animal.opensourcebodytracker.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.SettingsState
import de.t_animal.opensourcebodytracker.core.model.defaultSettingsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesSettingsRepository(
    private val context: Context,
) : SettingsRepository {

    private object Keys {
        val navyBodyFatEnabled = booleanPreferencesKey("navyBodyFatEnabled")
        val skinfoldBodyFatEnabled = booleanPreferencesKey("skinfoldBodyFatEnabled")
        val enabledMeasurements = stringSetPreferencesKey("enabledMeasurements")
        val visibleInAnalysis = stringSetPreferencesKey("visibleInAnalysis")
        val visibleInTable = stringSetPreferencesKey("visibleInTable")
    }

    override val settingsFlow: Flow<SettingsState> = context.settingsDataStore.data.map { prefs ->
        val defaults = defaultSettingsState()
        SettingsState(
            navyBodyFatEnabled = prefs[Keys.navyBodyFatEnabled] ?: defaults.navyBodyFatEnabled,
            skinfoldBodyFatEnabled = prefs[Keys.skinfoldBodyFatEnabled] ?: defaults.skinfoldBodyFatEnabled,
            enabledMeasurements = parseEnumSet(
                raw = prefs[Keys.enabledMeasurements],
                values = BodyMetric.entries,
                fallback = defaults.enabledMeasurements,
            ),
            visibleInAnalysis = parseEnumSet(
                raw = prefs[Keys.visibleInAnalysis],
                values = BodyMetric.entries,
                fallback = defaults.visibleInAnalysis,
            ),
            visibleInTable = parseEnumSet(
                raw = prefs[Keys.visibleInTable],
                values = BodyMetric.entries,
                fallback = defaults.visibleInTable,
            ),
        )
    }

    override suspend fun saveSettings(settings: SettingsState) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.navyBodyFatEnabled] = settings.navyBodyFatEnabled
            prefs[Keys.skinfoldBodyFatEnabled] = settings.skinfoldBodyFatEnabled
            prefs[Keys.enabledMeasurements] = settings.enabledMeasurements.mapTo(mutableSetOf()) { it.name }
            prefs[Keys.visibleInAnalysis] = settings.visibleInAnalysis.mapTo(mutableSetOf()) { it.name }
            prefs[Keys.visibleInTable] = settings.visibleInTable.mapTo(mutableSetOf()) { it.name }
        }
    }
}

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
