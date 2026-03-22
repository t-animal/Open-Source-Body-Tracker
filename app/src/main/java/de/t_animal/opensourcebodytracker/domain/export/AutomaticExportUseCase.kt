package de.t_animal.opensourcebodytracker.domain.export

import de.t_animal.opensourcebodytracker.core.model.AutomaticExportErrorKey
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
        } catch (_: Exception) {
            val error = AutomaticExportErrorKey.Unknown
            persistError(error, settings)
            AutomaticExportResult.Failed(error)
        }
    }

    private suspend fun performExport(
        onProgress: ((ExportProgress) -> Unit)?,
        settings: ExportSettings
    ): AutomaticExportResult {
        val exportPassword = exportPasswordRepository.getPassword() ?: run {
            val error = AutomaticExportErrorKey.EnterPassword
            persistError(error, settings)
            return AutomaticExportResult.Failed(error)
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
                val error = result.error.toErrorKey()
                exportSettingsRepository.saveSettings(
                    settings.copy(
                        lastAutomaticExportError = error,
                    ),
                )
                AutomaticExportResult.Failed(error)
            }
        }
    }

    private suspend fun persistError(error: AutomaticExportErrorKey, settings: ExportSettings) {
        exportSettingsRepository.saveSettings(
            settings.copy(lastAutomaticExportError = error),
        )
    }

    private fun ExportActionError.toErrorKey(): AutomaticExportErrorKey = when (this) {
        is ExportActionError.Validation -> when (error) {
            ExportValidationError.EnableDeviceStorage -> AutomaticExportErrorKey.EnableDeviceStorage
            ExportValidationError.SelectFolder -> AutomaticExportErrorKey.SelectFolder
            ExportValidationError.EnterPassword -> AutomaticExportErrorKey.EnterPassword
        }
        ExportActionError.InvalidFolder -> AutomaticExportErrorKey.InvalidFolder
        ExportActionError.PermissionDenied -> AutomaticExportErrorKey.PermissionDenied
        ExportActionError.WriteFailed -> AutomaticExportErrorKey.WriteFailed
        ExportActionError.Unknown -> AutomaticExportErrorKey.Unknown
    }
}

sealed interface AutomaticExportResult {
    data object Skipped : AutomaticExportResult
    data object Success : AutomaticExportResult
    data class Failed(val error: AutomaticExportErrorKey) : AutomaticExportResult
}
