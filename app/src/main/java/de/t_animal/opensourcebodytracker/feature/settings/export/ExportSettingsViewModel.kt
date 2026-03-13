package de.t_animal.opensourcebodytracker.feature.settings.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.data.export.ExportPasswordRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.export.AutomaticExportScheduler
import de.t_animal.opensourcebodytracker.domain.export.ExportActionError
import de.t_animal.opensourcebodytracker.domain.export.ExportActionResult
import de.t_animal.opensourcebodytracker.domain.export.ExportExecutionCommand
import de.t_animal.opensourcebodytracker.domain.export.ExportToFilesystemUseCase
import de.t_animal.opensourcebodytracker.domain.export.ExportProgress
import de.t_animal.opensourcebodytracker.domain.export.ExportValidationError
import de.t_animal.opensourcebodytracker.domain.export.validateExportCommandForSave
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExportUiProgress(
    val message: String,
    val current: Int? = null,
    val total: Int? = null,
) {
    val isDeterminate: Boolean
        get() = current != null && total != null && total > 0

    val progressFraction: Float?
        get() = if (isDeterminate) current!!.toFloat() / total!!.toFloat() else null
}

data class ExportSettingsUiState(
    val isLoading: Boolean = true,
    val isExporting: Boolean = false,
    val exportToDeviceStorageEnabled: Boolean = false,
    val exportFolderUri: String? = null,
    val exportPassword: String = "",
    val automaticExportEnabled: Boolean = false,
    val exportProgress: ExportUiProgress? = null,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val lastAutomaticExportError: String? = null,
)

sealed interface ExportSettingsEvent {
    data object Saved : ExportSettingsEvent
}

class ExportSettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val exportPasswordRepository: ExportPasswordRepository,
    private val exportToFileSystemUseCase: ExportToFilesystemUseCase,
    private val automaticExportScheduler: AutomaticExportScheduler,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExportSettingsUiState())
    val uiState: StateFlow<ExportSettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ExportSettingsEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settingsFlow.first()
            val password = exportPasswordRepository.getPassword().orEmpty()
            val hasPassword = password.isNotBlank()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isExporting = false,
                exportToDeviceStorageEnabled = settings.exportToDeviceStorageEnabled && hasPassword,
                exportFolderUri = settings.exportFolderUri,
                exportPassword = password,
                automaticExportEnabled = settings.automaticExportEnabled && settings.exportToDeviceStorageEnabled && hasPassword,
                exportProgress = null,
                statusMessage = null,
                errorMessage = null,
                lastAutomaticExportError = settings.lastAutomaticExportError,
            )
        }
    }

    fun onExportToDeviceStorageEnabledChanged(enabled: Boolean) {
        val canEnable = enabled && _uiState.value.exportPassword.isNotBlank()
        _uiState.value = _uiState.value.copy(
            exportToDeviceStorageEnabled = canEnable,
            exportFolderUri = if (canEnable) _uiState.value.exportFolderUri else null,
            automaticExportEnabled = if (canEnable) _uiState.value.automaticExportEnabled else false,
            exportProgress = null,
            statusMessage = null,
            errorMessage = null,
        )
    }

    fun onAutomaticExportEnabledChanged(enabled: Boolean) {
        val canEnableAutomatic = enabled &&
            _uiState.value.exportToDeviceStorageEnabled &&
            _uiState.value.exportPassword.isNotBlank()
        _uiState.value = _uiState.value.copy(
            automaticExportEnabled = canEnableAutomatic,
            statusMessage = null,
            errorMessage = null,
        )
    }

    fun onExportFolderSelected(uri: String) {
        _uiState.value = _uiState.value.copy(
            exportFolderUri = uri,
            exportProgress = null,
            statusMessage = null,
            errorMessage = null,
        )
    }

    fun onExportPasswordChanged(password: String) {
        val hasPassword = password.isNotBlank()
        _uiState.value = _uiState.value.copy(
            exportPassword = password,
            exportToDeviceStorageEnabled = _uiState.value.exportToDeviceStorageEnabled && hasPassword,
            automaticExportEnabled = _uiState.value.automaticExportEnabled && hasPassword,
            exportProgress = null,
            statusMessage = null,
            errorMessage = null,
        )
    }

    fun onExportNowClicked() {
        val current = _uiState.value
        if (current.isLoading || current.isExporting) {
            return
        }

        val command = current.toExportExecutionCommand()

        _uiState.value = current.copy(
            isExporting = true,
            exportProgress = null,
            statusMessage = null,
            errorMessage = null,
        )

        viewModelScope.launch {
            val result = exportToFileSystemUseCase(command) { progress ->
                _uiState.update { state ->
                    state.copy(
                        exportProgress = progress.toUiProgress(),
                        statusMessage = null,
                        errorMessage = null,
                    )
                }
            }
            _uiState.value = when (result) {
                is ExportActionResult.Success -> {
                    clearAutomaticExportPendingAndError()
                    _uiState.value.copy(
                        isExporting = false,
                        exportProgress = null,
                        statusMessage = "Export archive created: ${result.exportedFileName}",
                        errorMessage = null,
                        lastAutomaticExportError = null,
                    )
                }

                is ExportActionResult.Failure -> _uiState.value.copy(
                    isExporting = false,
                    exportProgress = null,
                    statusMessage = null,
                    errorMessage = result.error.toUserMessage(),
                )
            }
        }
    }

    fun onDismissAutomaticExportError() {
        val current = _uiState.value
        if (current.lastAutomaticExportError.isNullOrBlank()) {
            return
        }

        viewModelScope.launch {
            val settings = settingsRepository.settingsFlow.first()
            settingsRepository.saveSettings(
                settings.copy(
                    lastAutomaticExportError = null,
                ),
            )
            _uiState.update { state ->
                state.copy(
                    lastAutomaticExportError = null,
                )
            }
        }
    }

    fun onSaveClicked() {
        val current = _uiState.value
        if (current.isLoading || current.isExporting) {
            return
        }

        val validationError = validateExportCommandForSave(
            command = current.toExportExecutionCommand(),
        )
        if (validationError != null) {
            _uiState.value = current.copy(
                exportProgress = null,
                statusMessage = null,
                errorMessage = validationError.toUserMessage(),
            )
            return
        }

        viewModelScope.launch {
            val settings = settingsRepository.settingsFlow.first()
            val automaticExportEnabled = current.automaticExportEnabled

            val updatedSettings = settings.copy(
                exportToDeviceStorageEnabled = current.exportToDeviceStorageEnabled,
                exportFolderUri = current.exportFolderUri,
                automaticExportEnabled = automaticExportEnabled,
                automaticExportPending = if (automaticExportEnabled) {
                    settings.automaticExportPending
                } else {
                    false
                },
                lastAutomaticExportError = if (automaticExportEnabled) {
                    settings.lastAutomaticExportError
                } else {
                    null
                },
            )
            if (updatedSettings != settings) {
                settingsRepository.saveSettings(updatedSettings)
            }
            exportPasswordRepository.savePassword(current.exportPassword)

            if (automaticExportEnabled) {
                automaticExportScheduler.scheduleNightlyExportAtThreeAm()
            } else {
                automaticExportScheduler.cancelScheduledExport()
            }

            _events.emit(ExportSettingsEvent.Saved)
        }
    }

    private suspend fun clearAutomaticExportPendingAndError() {
        val settings = settingsRepository.settingsFlow.first()
        if (!settings.automaticExportPending && settings.lastAutomaticExportError == null) {
            return
        }

        settingsRepository.saveSettings(
            settings.copy(
                automaticExportPending = false,
                lastAutomaticExportError = null,
            ),
        )
    }

    private fun ExportSettingsUiState.toExportExecutionCommand(): ExportExecutionCommand {
        return ExportExecutionCommand(
            exportToDeviceStorageEnabled = exportToDeviceStorageEnabled,
            exportFolderUri = exportFolderUri,
            exportPassword = exportPassword,
        )
    }

    private fun ExportValidationError.toUserMessage(): String = when (this) {
        ExportValidationError.EnableDeviceStorage -> "Enable export to device storage"
        ExportValidationError.SelectFolder -> "Select an export folder"
        ExportValidationError.EnterPassword -> "Enter an export password"
    }

    private fun ExportActionError.toUserMessage(): String = when (this) {
        is ExportActionError.Validation -> error.toUserMessage()
        ExportActionError.InvalidFolder -> "Export folder is invalid. Select folder again"
        ExportActionError.PermissionDenied -> "Export folder permission was lost. Select folder again"
        ExportActionError.WriteFailed -> "Could not create export archive"
        ExportActionError.Unknown -> "Export failed"
    }

    private fun ExportProgress.toUiProgress(): ExportUiProgress = when (this) {
        ExportProgress.Validating,
        ExportProgress.LoadingProfile,
        ExportProgress.LoadingMeasurements -> ExportUiProgress(
            message = "Collecting data",
        )

        is ExportProgress.CollectingPhotos -> ExportUiProgress(
            message = "Collecting data",
            current = processedMeasurementCount,
            total = totalMeasurementCount.takeIf { it > 0 },
        )

        is ExportProgress.WritingArchiveData -> ExportUiProgress(
            message = "Writing archive data",
            current = currentDocumentIndex,
            total = totalDocumentCount,
        )

        is ExportProgress.WritingPhoto -> ExportUiProgress(
            message = "Writing photo $currentPhotoIndex of $totalPhotoCount",
            current = currentPhotoIndex,
            total = totalPhotoCount,
        )

        ExportProgress.CleaningUpOldExports -> ExportUiProgress(
            message = "Cleaning up old exports",
        )
    }
}

class ExportSettingsViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val exportPasswordRepository: ExportPasswordRepository,
    private val exportToFileSystemUseCase: ExportToFilesystemUseCase,
    private val automaticExportScheduler: AutomaticExportScheduler,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExportSettingsViewModel(
            settingsRepository = settingsRepository,
            exportPasswordRepository = exportPasswordRepository,
            exportToFileSystemUseCase = exportToFileSystemUseCase,
            automaticExportScheduler = automaticExportScheduler,
        ) as T
    }
}
