package de.t_animal.opensourcebodytracker.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import de.t_animal.opensourcebodytracker.core.model.ReminderSettings
import de.t_animal.opensourcebodytracker.domain.reminders.ReminderScheduleCalculator
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZonedDateTime
import javax.inject.Inject

class ReminderAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun syncWithSettings(
        settings: ReminderSettings,
        now: ZonedDateTime = ZonedDateTime.now(),
    ) {
        if (!settings.reminderEnabled || settings.reminderWeekdays.isEmpty()) {
            cancelScheduledReminder()
            return
        }

        val nextReminderAt = ReminderScheduleCalculator.nextReminderAt(
            now = now,
            weekdays = settings.reminderWeekdays,
            reminderTime = settings.reminderTime,
        )

        if (nextReminderAt == null) {
            cancelScheduledReminder()
            return
        }

        scheduleReminderAt(nextReminderAt.toInstant().toEpochMilli())
    }

    fun cancelScheduledReminder() {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        alarmManager.cancel(createReminderAlarmPendingIntent())
    }

    private fun scheduleReminderAt(triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val pendingIntent = createReminderAlarmPendingIntent()
        alarmManager.cancel(pendingIntent)

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent,
        )
    }

    private fun createReminderAlarmPendingIntent(): PendingIntent {
        val alarmIntent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ReminderNotificationContract.ReminderAlarmAction
        }

        return PendingIntent.getBroadcast(
            context,
            ReminderNotificationContract.ReminderAlarmRequestCode,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
