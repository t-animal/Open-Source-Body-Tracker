package de.t_animal.opensourcebodytracker.data.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import de.t_animal.opensourcebodytracker.data.settings.ReminderSettingsRepository

@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {
    @Inject lateinit var reminderSettingsRepository: ReminderSettingsRepository
    @Inject lateinit var reminderAlarmScheduler: ReminderAlarmScheduler
    @Inject lateinit var reminderNotificationPoster: ReminderNotificationPoster

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ReminderNotificationContract.ReminderAlarmAction) {
            return
        }

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = reminderSettingsRepository.settingsFlow.first()

                if (!settings.reminderEnabled || settings.reminderWeekdays.isEmpty()) {
                    reminderAlarmScheduler.cancelScheduledReminder()
                    return@launch
                }

                reminderNotificationPoster.showReminderNotification()

                // schedule next notification
                reminderAlarmScheduler.syncWithSettings(settings)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
