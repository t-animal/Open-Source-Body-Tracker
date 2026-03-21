package de.t_animal.opensourcebodytracker.domain.export

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import de.t_animal.opensourcebodytracker.core.export.AutomaticExportWorker
import de.t_animal.opensourcebodytracker.data.settings.ExportSettingsRepository
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class AutomaticExportScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val exportSettingsRepository: ExportSettingsRepository,
) {
    suspend fun scheduleNightlyExportAtThreeAm() {
        val settings = exportSettingsRepository.settingsFlow.first()

        if (!settings.automaticExportEnabled ||
            !settings.exportToDeviceStorageEnabled ||
            settings.exportFolderUri.isNullOrBlank()
        ) {
            cancelScheduledExport()
            return
        }

        val exportWorkRequest = PeriodicWorkRequestBuilder<AutomaticExportWorker>(
            Duration.ofDays(1),
        )
            .setInitialDelay(
                calculateInitialDelayToThreeAm(),
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            EXPORT_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            exportWorkRequest,
        )
    }

    fun cancelScheduledExport() {
        workManager.cancelUniqueWork(EXPORT_WORK_NAME)
    }

    fun scheduleExportInMinutes(minutes: Long) {
        val exportWorkRequest = OneTimeWorkRequestBuilder<AutomaticExportWorker>()
            .setInitialDelay(minutes, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniqueWork(
            "debug_export_in_$minutes",
            androidx.work.ExistingWorkPolicy.KEEP,
            exportWorkRequest,
        )
    }

    private fun calculateInitialDelayToThreeAm(): Duration {
        val now = ZonedDateTime.now()
        val threeAmToday = now.withHour(3).withMinute(0).withSecond(0).withNano(0)

        val targetTime = if (now.isBefore(threeAmToday) || now.isEqual(threeAmToday)) {
            threeAmToday
        } else {
            threeAmToday.plusDays(1)
        }

        val delaySeconds = java.time.temporal.ChronoUnit.SECONDS.between(now, targetTime)
        return Duration.ofSeconds(delaySeconds)
    }

    companion object {
        const val EXPORT_WORK_NAME = "automatic_nightly_export"
    }
}
