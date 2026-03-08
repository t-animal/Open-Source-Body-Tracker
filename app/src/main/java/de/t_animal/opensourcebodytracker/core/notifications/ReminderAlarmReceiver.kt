package de.t_animal.opensourcebodytracker.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.t_animal.opensourcebodytracker.BodyTrackerApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ReminderNotificationContract.ReminderAlarmAction) {
            return
        }

        val application = context.applicationContext as? BodyTrackerApplication ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val container = application.container
                val settings = container.settingsRepository.settingsFlow.first()

                if (!settings.reminderEnabled || settings.reminderWeekdays.isEmpty()) {
                    container.reminderAlarmScheduler.cancelScheduledReminder()
                    return@launch
                }

                container.reminderNotificationPoster.showReminderNotification()

                // schedule next notification
                container.reminderAlarmScheduler.syncWithSettings(settings)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
