package de.t_animal.opensourcebodytracker.core.model

sealed interface ReminderValidationError {
    data object NoWeekdaySelected : ReminderValidationError
}
