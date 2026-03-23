package de.t_animal.opensourcebodytracker.data.reminders

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
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ReminderNotificationPoster @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun showReminderNotification(): ReminderNotificationResult {
        if (!areNotificationsEnabled()) {
            return ReminderNotificationResult.NotificationsDisabled
        }

        createChannelIfNeeded()

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            action = ReminderNotificationContract.OpenAddMeasurementScreenAction
            putExtra(ReminderNotificationContract.OpenAddMeasurementScreen, true)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            ReminderNotificationContract.OpenAddMeasurementRequestCode,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, ReminderNotificationContract.ReminderChannelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_reminder_title))
            .setContentText(context.getString(R.string.notification_reminder_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()


        try {
            NotificationManagerCompat.from(context).notify(
                ReminderNotificationContract.ReminderNotificationId,
                notification,
            )
        } catch (_:SecurityException) {
            return ReminderNotificationResult.Failed
        }
            return ReminderNotificationResult.Shown
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
        val existingChannel = manager.getNotificationChannel(ReminderNotificationContract.ReminderChannelId)
        if (existingChannel != null) {
            return
        }

        val channel = NotificationChannel(
            ReminderNotificationContract.ReminderChannelId,
            context.getString(R.string.notification_channel_reminders),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        manager.createNotificationChannel(channel)
    }
}

enum class ReminderNotificationResult {
    Shown,
    NotificationsDisabled,
    Failed,
}
