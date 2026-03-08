package de.t_animal.opensourcebodytracker.domain.reminders

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZonedDateTime

object ReminderScheduleCalculator {
    fun nextReminderAt(
        now: ZonedDateTime,
        weekdays: Set<DayOfWeek>,
        reminderTime: LocalTime,
    ): ZonedDateTime? {
        if (weekdays.isEmpty()) {
            return null
        }

        for (daysOffset in 0L..7L) {
            val candidateDate = now.toLocalDate().plusDays(daysOffset)
            if (candidateDate.dayOfWeek !in weekdays) {
                continue
            }

            val candidateDateTime = candidateDate.atTime(reminderTime).atZone(now.zone)
            if (candidateDateTime.isAfter(now)) {
                return candidateDateTime
            }
        }

        return null
    }
}
