package de.t_animal.opensourcebodytracker.infra.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import de.t_animal.opensourcebodytracker.data.settings.ReminderSettingsRepository
import de.t_animal.opensourcebodytracker.domain.reminders.ReminderAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderRescheduleReceiver : BroadcastReceiver() {
    @Inject lateinit var reminderSettingsRepository: ReminderSettingsRepository
    @Inject lateinit var reminderAlarmScheduler: ReminderAlarmScheduler

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action !in supportedActions) {
            return
        }

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = reminderSettingsRepository.settingsFlow.first()
                reminderAlarmScheduler.syncWithSettings(settings)
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
