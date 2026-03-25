package de.t_animal.opensourcebodytracker.core.model

import java.time.DayOfWeek
import java.time.LocalTime

data class ReminderSettings(
    val reminderEnabled: Boolean = false,
    val reminderWeekdays: Set<DayOfWeek> = setOf(DayOfWeek.SUNDAY),
    val reminderTime: LocalTime = LocalTime.of(9, 0),
) {
    fun validate(): ReminderValidationError? =
        if (reminderEnabled && reminderWeekdays.isEmpty()) ReminderValidationError.NoWeekdaySelected else null
}
