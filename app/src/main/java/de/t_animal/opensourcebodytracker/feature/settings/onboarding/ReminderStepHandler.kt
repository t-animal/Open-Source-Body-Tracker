package de.t_animal.opensourcebodytracker.feature.settings.onboarding

import de.t_animal.opensourcebodytracker.feature.settings.reminders.ReminderValidationError
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
        if (current.enabled && current.weekdays.isEmpty()) {
            uiState.value = uiState.value.copy(
                reminders = current.copy(
                    validationError = ReminderValidationError.NoWeekdaySelected,
                ),
            )
            return false
        }
        return true
    }
}
