package de.t_animal.opensourcebodytracker.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import de.t_animal.opensourcebodytracker.core.model.ReminderSettings
import de.t_animal.opensourcebodytracker.di.SettingsDataStore
import java.time.DayOfWeek
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ReminderSettingsRepository {
    val settingsFlow: Flow<ReminderSettings>

    suspend fun saveSettings(settings: ReminderSettings)

    suspend fun updateSettings(transform: (ReminderSettings) -> ReminderSettings)
}

class PreferencesReminderSettingsRepository @Inject constructor(
    @SettingsDataStore private val dataStore: DataStore<Preferences>,
) : ReminderSettingsRepository {

    override val settingsFlow: Flow<ReminderSettings> = dataStore.data.map { it.toReminderSettings() }

    override suspend fun saveSettings(settings: ReminderSettings) {
        dataStore.edit { it.applyReminderSettings(settings) }
    }

    override suspend fun updateSettings(transform: (ReminderSettings) -> ReminderSettings) {
        dataStore.edit { prefs ->
            prefs.applyReminderSettings(transform(prefs.toReminderSettings()))
        }
    }

    private fun Preferences.toReminderSettings(): ReminderSettings {
        val defaults = ReminderSettings()
        return ReminderSettings(
            reminderEnabled = this[SettingsKeys.reminderEnabled] ?: defaults.reminderEnabled,
            reminderWeekdays = parseEnumSet(
                raw = this[SettingsKeys.reminderWeekdays],
                values = DayOfWeek.values().toList(),
                fallback = defaults.reminderWeekdays,
            ),
            reminderTime = parseLocalTime(
                raw = this[SettingsKeys.reminderTime],
                fallback = defaults.reminderTime,
            ),
        )
    }

    private fun MutablePreferences.applyReminderSettings(settings: ReminderSettings) {
        this[SettingsKeys.reminderEnabled] = settings.reminderEnabled
        this[SettingsKeys.reminderWeekdays] = settings.reminderWeekdays.mapTo(mutableSetOf()) { it.name }
        this[SettingsKeys.reminderTime] = settings.reminderTime.toString()
    }
}
