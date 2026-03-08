package de.t_animal.opensourcebodytracker.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.t_animal.opensourcebodytracker.BodyTrackerApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action !in supportedActions) {
            return
        }

        val application = context.applicationContext as? BodyTrackerApplication ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val container = application.container
                val settings = container.settingsRepository.settingsFlow.first()
                container.reminderAlarmScheduler.syncWithSettings(settings)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        val supportedActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
        )
    }
}
