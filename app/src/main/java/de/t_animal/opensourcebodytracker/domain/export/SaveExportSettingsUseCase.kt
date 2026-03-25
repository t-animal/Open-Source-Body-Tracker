package de.t_animal.opensourcebodytracker.domain.export

import de.t_animal.opensourcebodytracker.data.export.ExportPasswordRepository
import de.t_animal.opensourcebodytracker.data.settings.ExportSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

data class SaveExportSettingsCommand(
    val exportToDeviceStorageEnabled: Boolean,
    val exportFolderUri: String?,
    val exportPassword: String,
    val automaticExportEnabled: Boolean,
)

class SaveExportSettingsUseCase @Inject constructor(
    private val exportSettingsRepository: ExportSettingsRepository,
    private val exportPasswordRepository: ExportPasswordRepository,
    private val automaticExportScheduler: AutomaticExportScheduler,
) {
    suspend fun save(command: SaveExportSettingsCommand) {
        val current = exportSettingsRepository.settingsFlow.first()
        val automaticExportEnabled = command.automaticExportEnabled

        val updated = current.copy(
            exportToDeviceStorageEnabled = command.exportToDeviceStorageEnabled,
            exportFolderUri = command.exportFolderUri,
            automaticExportEnabled = automaticExportEnabled,
            automaticExportPending = if (automaticExportEnabled) {
                current.automaticExportPending
            } else {
                false
            },
            lastAutomaticExportError = if (automaticExportEnabled) {
                current.lastAutomaticExportError
            } else {
                null
            },
        )
        if (updated != current) {
            exportSettingsRepository.saveSettings(updated)
        }
        exportPasswordRepository.savePassword(command.exportPassword)

        if (automaticExportEnabled) {
            automaticExportScheduler.scheduleNightlyExportAtThreeAm()
        } else {
            automaticExportScheduler.cancelScheduledExport()
        }
    }

    suspend fun clearAutomaticExportState() {
        val current = exportSettingsRepository.settingsFlow.first()
        if (!current.automaticExportPending && current.lastAutomaticExportError == null) {
            return
        }

        exportSettingsRepository.saveSettings(
            current.copy(
                automaticExportPending = false,
                lastAutomaticExportError = null,
            ),
        )
    }

    suspend fun dismissAutomaticExportError() {
        val current = exportSettingsRepository.settingsFlow.first()
        if (current.lastAutomaticExportError == null) {
            return
        }

        exportSettingsRepository.saveSettings(
            current.copy(lastAutomaticExportError = null)
        )
    }
}
