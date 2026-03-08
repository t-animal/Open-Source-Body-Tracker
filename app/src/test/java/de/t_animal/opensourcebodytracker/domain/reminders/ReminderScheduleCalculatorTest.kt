package de.t_animal.opensourcebodytracker.domain.reminders

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReminderScheduleCalculatorTest {

    @Test
    fun nextReminderAt_returnsToday_whenSelectedDayAndTimeStillAhead() {
        val zone = ZoneId.of("UTC")
        val now = ZonedDateTime.of(2026, 3, 9, 8, 30, 0, 0, zone)

        val result = ReminderScheduleCalculator.nextReminderAt(
            now = now,
            weekdays = setOf(now.dayOfWeek),
            reminderTime = LocalTime.of(9, 0),
        )

        val expected = now.toLocalDate().atTime(9, 0).atZone(zone)
        assertEquals(expected, result)
    }

    @Test
    fun nextReminderAt_returnsNextSelectedDay_whenTodaysTimeAlreadyPassed() {
        val zone = ZoneId.of("UTC")
        val mondayMorningPastReminder = ZonedDateTime.of(2026, 3, 9, 10, 0, 0, 0, zone)

        val result = ReminderScheduleCalculator.nextReminderAt(
            now = mondayMorningPastReminder,
            weekdays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
            reminderTime = LocalTime.of(9, 0),
        )

        val expected = mondayMorningPastReminder
            .with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY))
            .withHour(9)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        assertEquals(expected, result)
    }

    @Test
    fun nextReminderAt_wrapsToFollowingWeek_whenOnlyTodaysWeekdayAndTimePassed() {
        val zone = ZoneId.of("UTC")
        val mondayMorningPastReminder = ZonedDateTime.of(2026, 3, 9, 10, 0, 0, 0, zone)

        val result = ReminderScheduleCalculator.nextReminderAt(
            now = mondayMorningPastReminder,
            weekdays = setOf(DayOfWeek.MONDAY),
            reminderTime = LocalTime.of(9, 0),
        )

        val expected = mondayMorningPastReminder
            .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
            .withHour(9)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        assertEquals(expected, result)
    }

    @Test
    fun nextReminderAt_returnsNull_whenNoWeekdaysAreSelected() {
        val zone = ZoneId.of("UTC")
        val now = ZonedDateTime.of(2026, 3, 9, 8, 30, 0, 0, zone)

        val result = ReminderScheduleCalculator.nextReminderAt(
            now = now,
            weekdays = emptySet(),
            reminderTime = LocalTime.of(9, 0),
        )

        assertNull(result)
    }
}
