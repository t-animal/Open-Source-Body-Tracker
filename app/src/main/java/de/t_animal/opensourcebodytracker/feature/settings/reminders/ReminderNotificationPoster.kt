package de.t_animal.opensourcebodytracker.feature.settings.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import de.t_animal.opensourcebodytracker.MainActivity
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.notifications.ReminderNotificationContract

class ReminderNotificationPoster(
    private val context: Context,
) {
    @androidx.annotation.RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS)
    fun postReminderNotification(): ManualReminderResult {
        if (!areNotificationsEnabled()) {
            return ManualReminderResult.NotificationsDisabled
        }

        createChannelIfNeeded()

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            action = ReminderNotificationContract.OpenAddMeasurementScreenAction
            putExtra(ReminderNotificationContract.OpenAddMeasurementScreen, true)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Measurement Reminder")
            .setContentText("Don't forget to record your measurements.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        return runCatching{
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }.fold(
            onSuccess = { ManualReminderResult.Shown },
            onFailure = { ManualReminderResult.Failed },
        )
    }

    private fun areNotificationsEnabled(): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return false
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createChannelIfNeeded() {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val existingChannel = manager.getNotificationChannel(CHANNEL_ID)
        if (existingChannel != null) {
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Measurement Reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        manager.createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "measurement_reminders"
        const val NOTIFICATION_ID = 4223
        const val REQUEST_CODE = 4223
    }
}

enum class ManualReminderResult {
    Shown,
    NotificationsDisabled,
    Failed,
}
