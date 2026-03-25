package de.t_animal.opensourcebodytracker.feature.settings.onboarding

import de.t_animal.opensourcebodytracker.core.model.ReminderSettings
import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow

internal class ReminderStepHandler(
    private val uiState: MutableStateFlow<OnboardingUiState>,
) {

    fun onEnabledChanged(enabled: Boolean) {
        uiState.value = uiState.value.copy(
            reminders = uiState.value.reminders.copy(enabled = enabled, validationError = null),
        )
    }

    fun onWeekdayToggled(dayOfWeek: DayOfWeek) {
        val currentWeekdays = uiState.value.reminders.weekdays
        val updatedWeekdays = if (dayOfWeek in currentWeekdays) {
            currentWeekdays - dayOfWeek
        } else {
            currentWeekdays + dayOfWeek
        }
        uiState.value = uiState.value.copy(
            reminders = uiState.value.reminders.copy(
                weekdays = updatedWeekdays,
                validationError = null,
            ),
        )
    }

    fun onTimeChanged(time: LocalTime) {
        uiState.value = uiState.value.copy(
            reminders = uiState.value.reminders.copy(time = time, validationError = null),
        )
    }

    fun onPermissionDeniedWhileSaving() {
        uiState.value = uiState.value.copy(
            reminders = uiState.value.reminders.copy(enabled = false, validationError = null),
        )
    }

    fun validate(): Boolean {
        val current = uiState.value.reminders
        val settings = ReminderSettings(
            reminderEnabled = current.enabled,
            reminderWeekdays = current.weekdays,
            reminderTime = current.time,
        )
        val error = settings.validate()
        if (error != null) {
            uiState.value = uiState.value.copy(
                reminders = current.copy(validationError = error),
            )
            return false
        }
        return true
    }
}
