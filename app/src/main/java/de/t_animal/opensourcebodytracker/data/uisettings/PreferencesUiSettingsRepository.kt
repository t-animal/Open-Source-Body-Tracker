package de.t_animal.opensourcebodytracker.data.uisettings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.t_animal.opensourcebodytracker.core.model.AnalysisDuration
import de.t_animal.opensourcebodytracker.data.settings.parseBodyMetricList
import de.t_animal.opensourcebodytracker.data.settings.parseEnum
import de.t_animal.opensourcebodytracker.data.settings.storageName
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.uiSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "ui_settings")

class PreferencesUiSettingsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : UiSettingsRepository {

    private object Keys {
        val analysisChartOrder = stringPreferencesKey("analysisChartOrder")
        val analysisCollapsedCharts = stringSetPreferencesKey("analysisCollapsedCharts")
        val analysisDuration = stringPreferencesKey("analysisDuration")
    }

    override val settingsFlow: Flow<UiSettings> = context.uiSettingsDataStore.data.map { it.toUiSettings() }

    override suspend fun saveSettings(settings: UiSettings) {
        context.uiSettingsDataStore.edit { it.applySettings(settings) }
    }

    override suspend fun updateSettings(transform: (UiSettings) -> UiSettings) {
        context.uiSettingsDataStore.edit { prefs ->
            prefs.applySettings(transform(prefs.toUiSettings()))
        }
    }

    private fun Preferences.toUiSettings(): UiSettings {
        val defaults = defaultUiSettings()
        return UiSettings(
            analysisChartOrder = parseBodyMetricList(this[Keys.analysisChartOrder]),
            analysisCollapsedCharts = this[Keys.analysisCollapsedCharts] ?: defaults.analysisCollapsedCharts,
            analysisDuration = parseEnum(
                raw = this[Keys.analysisDuration],
                values = AnalysisDuration.entries,
                fallback = defaults.analysisDuration,
            ),
        )
    }

    private fun MutablePreferences.applySettings(settings: UiSettings) {
        this[Keys.analysisChartOrder] = settings.analysisChartOrder.joinToString(",") { it.storageName() }
        this[Keys.analysisCollapsedCharts] = settings.analysisCollapsedCharts
        this[Keys.analysisDuration] = settings.analysisDuration.name
    }
}
