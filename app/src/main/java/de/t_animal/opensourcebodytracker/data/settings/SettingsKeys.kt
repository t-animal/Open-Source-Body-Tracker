package de.t_animal.opensourcebodytracker.data.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

object SettingsKeys {
    // Measurement settings
    val bmiEnabled = booleanPreferencesKey("measurement.bmiEnabled")
    val navyBodyFatEnabled = booleanPreferencesKey("measurement.navyBodyFatEnabled")
    val skinfoldBodyFatEnabled = booleanPreferencesKey("measurement.skinfoldBodyFatEnabled")
    val waistHipRatioEnabled = booleanPreferencesKey("measurement.waistHipRatioEnabled")
    val waistHeightRatioEnabled = booleanPreferencesKey("measurement.waistHeightRatioEnabled")
    val enabledMeasurements = stringSetPreferencesKey("measurement.enabledMeasurements")
    val visibleInAnalysis = stringSetPreferencesKey("measurement.visibleInAnalysis")
    val visibleInTable = stringSetPreferencesKey("measurement.visibleInTable")

    // Reminder settings
    val reminderEnabled = booleanPreferencesKey("reminder.reminderEnabled")
    val reminderWeekdays = stringSetPreferencesKey("reminder.reminderWeekdays")
    val reminderTime = stringPreferencesKey("reminder.reminderTime")

    // Export settings
    val exportToDeviceStorageEnabled = booleanPreferencesKey("export.exportToDeviceStorageEnabled")
    val exportFolderUri = stringPreferencesKey("export.exportFolderUri")
    val automaticExportEnabled = booleanPreferencesKey("export.automaticExportEnabled")
    val automaticExportPending = booleanPreferencesKey("export.automaticExportPending")
    val lastAutomaticExportErrorKey = stringPreferencesKey("export.lastAutomaticExportError")

    // General settings
    val onboardingCompleted = booleanPreferencesKey("general.onboardingCompleted")
    val isDemoMode = booleanPreferencesKey("general.isDemoMode")
    val unitSystem = stringPreferencesKey("general.unitSystem")
    val photoQuality = stringPreferencesKey("general.photoQuality")
}
