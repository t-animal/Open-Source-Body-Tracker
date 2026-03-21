package de.t_animal.opensourcebodytracker.core.export

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
import de.t_animal.opensourcebodytracker.core.model.ExportSettings
import de.t_animal.opensourcebodytracker.data.export.ExportPasswordRepository
import de.t_animal.opensourcebodytracker.data.settings.ExportSettingsRepository
import de.t_animal.opensourcebodytracker.domain.export.ExportActionError
import de.t_animal.opensourcebodytracker.domain.export.ExportActionResult
import de.t_animal.opensourcebodytracker.domain.export.ExportExecutionCommand
import de.t_animal.opensourcebodytracker.domain.export.ExportProgress
import de.t_animal.opensourcebodytracker.domain.export.ExportToFilesystemUseCase
import kotlinx.coroutines.flow.first

@HiltWorker
class AutomaticExportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val exportExportSettingsRepository: ExportSettingsRepository,
    private val exportPasswordRepository: ExportPasswordRepository,
    private val exportToFilesystemUseCase: ExportToFilesystemUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val settings = exportExportSettingsRepository.settingsFlow.first()

            if (!settings.automaticExportEnabled ||
                !settings.exportToDeviceStorageEnabled ||
                settings.exportFolderUri.isNullOrBlank() ||
                !settings.automaticExportPending
            ) {
                Result.success()
            } else {
                performExport(settings)
            }
        } catch (e: Exception) {
            persistAutomaticExportError(
                message = e.message ?: "Automatic export failed",
            )
            Result.success()
        }
    }

    private suspend fun performExport(settings: ExportSettings): Result {
        return try {
            runCatching {
                setForeground(createForegroundInfo())
            }

            val exportPassword = exportPasswordRepository.getPassword() ?: run {
                exportExportSettingsRepository.saveSettings(
                    settings.copy(
                        lastAutomaticExportError = "Export password not configured",
                    ),
                )
                return Result.success()
            }

            val exportCommand = ExportExecutionCommand(
                exportToDeviceStorageEnabled = true,
                exportFolderUri = settings.exportFolderUri,
                exportPassword = exportPassword,
            )

            val result = exportToFilesystemUseCase(exportCommand) { progress ->
                if (progress is ExportProgress.WritingPhoto) {
                    updateNotification(progress)
                }
            }

            when (result) {
                is ExportActionResult.Success -> {
                    exportExportSettingsRepository.saveSettings(
                        settings.copy(
                            automaticExportPending = false,
                            lastAutomaticExportError = null,
                        ),
                    )
                    Result.success()
                }

                is ExportActionResult.Failure -> {
                    exportExportSettingsRepository.saveSettings(
                        settings.copy(
                            lastAutomaticExportError = result.error.toAutomaticExportMessage(),
                        ),
                    )
                    Result.success()
                }
            }
        } catch (e: Exception) {
            persistAutomaticExportError(
                message = e.message ?: "Automatic export failed",
            )
            Result.success()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    @SuppressLint("MissingPermission")
    private fun updateNotification(progress: ExportProgress? = null) {
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

    private suspend fun persistAutomaticExportError(message: String) {
        val settings = exportExportSettingsRepository.settingsFlow.first()
        exportExportSettingsRepository.saveSettings(
            settings.copy(
                lastAutomaticExportError = message,
            ),
        )
    }

    private fun createForegroundInfo(progress: ExportProgress? = null): ForegroundInfo {
        return ForegroundInfo(EXPORT_NOTIFICATION_ID, createNotification(progress))
    }

    private fun createNotification(progress: ExportProgress? = null) =
        progress.toNotificationState().let { notificationState ->
            NotificationCompat.Builder(
                applicationContext,
                EXPORT_NOTIFICATION_CHANNEL_ID,
            )
                .setContentTitle("Automatic Backup")
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

    private fun ExportActionError.toAutomaticExportMessage(): String = when (this) {
        is ExportActionError.Validation -> when (error) {
            de.t_animal.opensourcebodytracker.domain.export.ExportValidationError.EnableDeviceStorage -> {
                "Automatic export is disabled"
            }

            de.t_animal.opensourcebodytracker.domain.export.ExportValidationError.SelectFolder -> {
                "Export folder not configured"
            }

            de.t_animal.opensourcebodytracker.domain.export.ExportValidationError.EnterPassword -> {
                "Export password not configured"
            }
        }

        ExportActionError.InvalidFolder -> "Export folder is invalid"
        ExportActionError.PermissionDenied -> "Export folder permission was lost"
        ExportActionError.WriteFailed -> "Could not create export archive"
        ExportActionError.Unknown -> "Automatic export failed"
    }

    private fun ExportProgress?.toNotificationState(): NotificationState = when (this) {
        is ExportProgress.WritingPhoto -> NotificationState(
            message = "Exporting photos $currentPhotoIndex of $totalPhotoCount",
            current = currentPhotoIndex,
            total = totalPhotoCount,
            isIndeterminate = totalPhotoCount <= 0,
        )

        is ExportProgress.CollectingPhotos -> NotificationState(
            message = "Preparing photos",
            current = processedMeasurementCount,
            total = totalMeasurementCount,
            isIndeterminate = totalMeasurementCount <= 0,
        )

        ExportProgress.CleaningUpOldExports -> NotificationState(
            message = "Cleaning up old backups",
        )

        is ExportProgress.WritingArchiveData -> NotificationState(
            message = "Writing backup archive",
        )

        ExportProgress.Validating,
        ExportProgress.LoadingMeasurements,
        ExportProgress.LoadingProfile,
        null -> NotificationState(
            message = "Backing up your data...",
        )
    }

    companion object {
        const val EXPORT_NOTIFICATION_CHANNEL_ID = "automatic_export_channel"
        const val EXPORT_NOTIFICATION_ID = 4224
    }
}

private data class NotificationState(
    val message: String,
    val current: Int? = null,
    val total: Int? = null,
    val isIndeterminate: Boolean = true,
)
