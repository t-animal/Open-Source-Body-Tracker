package de.t_animal.opensourcebodytracker.infra.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import de.t_animal.opensourcebodytracker.domain.reminders.HandleReminderAlarmUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {
    @Inject lateinit var handleReminderAlarmUseCase: HandleReminderAlarmUseCase

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ReminderNotificationContract.ReminderAlarmAction) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                handleReminderAlarmUseCase()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
