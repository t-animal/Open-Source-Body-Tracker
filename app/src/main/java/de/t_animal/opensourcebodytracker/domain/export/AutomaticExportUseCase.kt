package de.t_animal.opensourcebodytracker.domain.export

import de.t_animal.opensourcebodytracker.core.model.ExportSettings
import de.t_animal.opensourcebodytracker.data.export.ExportPasswordRepository
import de.t_animal.opensourcebodytracker.data.settings.ExportSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class AutomaticExportUseCase @Inject constructor(
    private val exportSettingsRepository: ExportSettingsRepository,
    private val exportPasswordRepository: ExportPasswordRepository,
    private val exportToFilesystemUseCase: ExportToFilesystemUseCase,
) {
    suspend operator fun invoke(
        onProgress: ((ExportProgress) -> Unit)? = null,
    ): AutomaticExportResult {
        val settings = exportSettingsRepository.settingsFlow.first()

        if (!settings.automaticExportEnabled ||
            !settings.exportToDeviceStorageEnabled ||
            settings.exportFolderUri.isNullOrBlank() ||
            !settings.automaticExportPending
        ) {
            return AutomaticExportResult.Skipped
        }

        return try {
            performExport(onProgress, settings)
        } catch (e: Exception) {
            val message = e.message ?: "Automatic export failed"
            persistError(message, settings)
            AutomaticExportResult.Failed(message)
        }
    }

    private suspend fun performExport(
        onProgress: ((ExportProgress) -> Unit)?,
        settings: ExportSettings
    ): AutomaticExportResult {
        val exportPassword = exportPasswordRepository.getPassword() ?: run {
            val message = "Export password not configured"
            persistError(message, settings)
            return AutomaticExportResult.Failed(message)
        }

        val exportCommand = ExportExecutionCommand(
            exportToDeviceStorageEnabled = true,
            exportFolderUri = settings.exportFolderUri,
            exportPassword = exportPassword,
        )

        val result = exportToFilesystemUseCase(exportCommand, onProgress)

        return when (result) {
            is ExportActionResult.Success -> {
                exportSettingsRepository.saveSettings(
                    settings.copy(
                        automaticExportPending = false,
                        lastAutomaticExportError = null,
                    ),
                )
                AutomaticExportResult.Success
            }

            is ExportActionResult.Failure -> {
                val message = result.error.toAutomaticExportMessage()
                exportSettingsRepository.saveSettings(
                    settings.copy(
                        lastAutomaticExportError = message,
                    ),
                )
                AutomaticExportResult.Failed(message)
            }
        }
    }

    private suspend fun persistError(message: String, settings: ExportSettings) {
        exportSettingsRepository.saveSettings(
            settings.copy(lastAutomaticExportError = message),
        )
    }

    private fun ExportActionError.toAutomaticExportMessage(): String = when (this) {
        is ExportActionError.Validation -> when (error) {
            ExportValidationError.EnableDeviceStorage -> "Automatic export is disabled"
            ExportValidationError.SelectFolder -> "Export folder not configured"
            ExportValidationError.EnterPassword -> "Export password not configured"
        }
        ExportActionError.InvalidFolder -> "Export folder is invalid"
        ExportActionError.PermissionDenied -> "Export folder permission was lost"
        ExportActionError.WriteFailed -> "Could not create export archive"
        ExportActionError.Unknown -> "Automatic export failed"
    }
}

sealed interface AutomaticExportResult {
    data object Skipped : AutomaticExportResult
    data object Success : AutomaticExportResult
    data class Failed(val message: String) : AutomaticExportResult
}
