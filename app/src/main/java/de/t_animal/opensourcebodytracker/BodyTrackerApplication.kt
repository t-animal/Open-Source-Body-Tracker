package de.t_animal.opensourcebodytracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import de.t_animal.opensourcebodytracker.data.export.AutomaticExportWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BodyTrackerApplication : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }
    
    // TODO: Can this be deferred until the first notification is posted? No need to create channels if user never enables reminders or automatic export.
    // In any case this implementation and the one from ReminderNotificationPoster should be held in the same file to avoid inconsistencies and duplicate channels.
    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val exportChannel = NotificationChannel(
            AutomaticExportWorker.EXPORT_NOTIFICATION_CHANNEL_ID,
            "Automatic Export",
            NotificationManager.IMPORTANCE_LOW,
        )
        manager.createNotificationChannel(exportChannel)
    }
}
