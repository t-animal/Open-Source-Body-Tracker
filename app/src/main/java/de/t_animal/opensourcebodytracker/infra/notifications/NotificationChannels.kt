package de.t_animal.opensourcebodytracker.infra.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import de.t_animal.opensourcebodytracker.R

object NotificationChannels {
    const val REMINDER_CHANNEL_ID = "measurement_reminders"
    const val EXPORT_CHANNEL_ID = "automatic_export_channel"

    fun ensureReminderChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(REMINDER_CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                REMINDER_CHANNEL_ID,
                context.getString(R.string.notification_channel_reminders),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
        )
    }

    fun ensureExportChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(EXPORT_CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                EXPORT_CHANNEL_ID,
                context.getString(R.string.notification_channel_export),
                NotificationManager.IMPORTANCE_LOW,
            )
        )
    }
}
