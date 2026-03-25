package de.t_animal.opensourcebodytracker.data.export

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.domain.export.AutomaticExportUseCase
import de.t_animal.opensourcebodytracker.domain.export.ExportProgress
import de.t_animal.opensourcebodytracker.infra.NotificationChannels

@HiltWorker
class AutomaticExportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val automaticExportUseCase: AutomaticExportUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        runCatching { setForeground(createForegroundInfo()) }
        automaticExportUseCase { progress -> updateNotification(progress) }
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    @SuppressLint("MissingPermission")
    private fun updateNotification(progress: ExportProgress) {
        if (!canPostNotificationUpdates()) {
            return
        }

        runCatching {
            NotificationManagerCompat.from(applicationContext).notify(
                EXPORT_NOTIFICATION_ID,
                createNotification(progress),
            )
        }
    }

    private fun canPostNotificationUpdates(): Boolean {
        if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            return false
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }

        return ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createForegroundInfo(progress: ExportProgress? = null): ForegroundInfo {
        return ForegroundInfo(EXPORT_NOTIFICATION_ID, createNotification(progress))
    }

    private fun createNotification(progress: ExportProgress? = null) =
        progress.toNotificationState().let { notificationState ->
            NotificationCompat.Builder(
                applicationContext,
                NotificationChannels.EXPORT_CHANNEL_ID,
            )
                .setContentTitle(applicationContext.getString(R.string.notification_export_title))
                .setContentText(notificationState.message)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .setProgress(
                    notificationState.total ?: 0,
                    notificationState.current ?: 0,
                    notificationState.isIndeterminate,
                )
                .setOngoing(true)
                .build()
        }

    private fun ExportProgress?.toNotificationState(): NotificationState = when (this) {
        is ExportProgress.WritingPhoto -> NotificationState(
            message = applicationContext.resources.getQuantityString(
                R.plurals.notification_export_photos,
                totalPhotoCount,
                currentPhotoIndex,
                totalPhotoCount,
            ),
            current = currentPhotoIndex,
            total = totalPhotoCount,
            isIndeterminate = totalPhotoCount <= 0,
        )

        is ExportProgress.CollectingPhotos -> NotificationState(
            message = applicationContext.getString(R.string.notification_export_preparing),
            current = processedMeasurementCount,
            total = totalMeasurementCount,
            isIndeterminate = totalMeasurementCount <= 0,
        )

        ExportProgress.CleaningUpOldExports -> NotificationState(
            message = applicationContext.getString(R.string.notification_export_cleaning),
        )

        is ExportProgress.WritingArchiveData -> NotificationState(
            message = applicationContext.getString(R.string.notification_export_writing),
        )

        ExportProgress.Validating,
        ExportProgress.LoadingMeasurements,
        ExportProgress.LoadingProfile,
        null -> NotificationState(
            message = applicationContext.getString(R.string.notification_export_default),
        )
    }

    companion object {
        const val EXPORT_NOTIFICATION_ID = 4224
    }
}

private data class NotificationState(
    val message: String,
    val current: Int? = null,
    val total: Int? = null,
    val isIndeterminate: Boolean = true,
)
