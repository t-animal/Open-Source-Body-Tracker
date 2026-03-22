package de.t_animal.opensourcebodytracker.feature.settings.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import de.t_animal.opensourcebodytracker.core.model.AutomaticExportErrorKey
import de.t_animal.opensourcebodytracker.data.export.ExportPasswordRepository
import de.t_animal.opensourcebodytracker.data.settings.ExportSettingsRepository
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

sealed interface ExportProgressStep {
    data object CollectingData : ExportProgressStep
    data class WritingArchiveData(val current: Int, val total: Int) : ExportProgressStep
    data class WritingPhoto(val current: Int, val total: Int) : ExportProgressStep
    data object CleaningUp : ExportProgressStep
}

data class ExportUiProgress(
    val step: ExportProgressStep,
    val current: Int? = null,
    val total: Int? = null,
) {
    val isDeterminate: Boolean
        get() = current != null && total != null && total > 0

    val progressFraction: Float?
        get() = if (isDeterminate) current!!.toFloat() / total!!.toFloat() else null
}

sealed interface ExportSettingsError {
    data class Validation(val error: ExportValidationError) : ExportSettingsError
    data class Action(val error: ExportActionError) : ExportSettingsError
}

data class ExportSettingsUiState(
    val isLoading: Boolean = true,
    val isExporting: Boolean = false,
    val exportToDeviceStorageEnabled: Boolean = false,
    val exportFolderUri: String? = null,
    val exportPassword: String = "",
    val automaticExportEnabled: Boolean = false,
    val exportProgress: ExportUiProgress? = null,
    val exportedFileName: String? = null,
    val exportError: ExportSettingsError? = null,
    val lastAutomaticExportError: AutomaticExportErrorKey? = null,
)

sealed interface ExportSettingsEvent {
    data object Saved : ExportSettingsEvent
}

@HiltViewModel
class ExportSettingsViewModel @Inject constructor(
    private val exportSettingsRepository: ExportSettingsRepository,
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
            val settings = exportSettingsRepository.settingsFlow.first()
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
                exportedFileName = null,
                exportError = null,
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
            exportedFileName = null,
            exportError = null,
        )
    }

    fun onAutomaticExportEnabledChanged(enabled: Boolean) {
        val canEnableAutomatic = enabled &&
            _uiState.value.exportToDeviceStorageEnabled &&
            _uiState.value.exportPassword.isNotBlank()
        _uiState.value = _uiState.value.copy(
            automaticExportEnabled = canEnableAutomatic,
            exportedFileName = null,
            exportError = null,
        )
    }

    fun onExportFolderSelected(uri: String) {
        _uiState.value = _uiState.value.copy(
            exportFolderUri = uri,
            exportProgress = null,
            exportedFileName = null,
            exportError = null,
        )
    }

    fun onExportPasswordChanged(password: String) {
        val hasPassword = password.isNotBlank()
        _uiState.value = _uiState.value.copy(
            exportPassword = password,
            exportToDeviceStorageEnabled = _uiState.value.exportToDeviceStorageEnabled && hasPassword,
            automaticExportEnabled = _uiState.value.automaticExportEnabled && hasPassword,
            exportProgress = null,
            exportedFileName = null,
            exportError = null,
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
            exportedFileName = null,
            exportError = null,
        )

        viewModelScope.launch {
            val result = exportToFileSystemUseCase(command) { progress ->
                _uiState.update { state ->
                    state.copy(
                        exportProgress = progress.toUiProgress(),
                        exportedFileName = null,
                        exportError = null,
                    )
                }
            }
            _uiState.value = when (result) {
                is ExportActionResult.Success -> {
                    clearAutomaticExportPendingAndError()
                    _uiState.value.copy(
                        isExporting = false,
                        exportProgress = null,
                        exportedFileName = result.exportedFileName,
                        exportError = null,
                        lastAutomaticExportError = null,
                    )
                }

                is ExportActionResult.Failure -> _uiState.value.copy(
                    isExporting = false,
                    exportProgress = null,
                    exportedFileName = null,
                    exportError = ExportSettingsError.Action(result.error),
                )
            }
        }
    }

    fun onDismissAutomaticExportError() {
        val current = _uiState.value
        if (current.lastAutomaticExportError == null) {
            return
        }

        viewModelScope.launch {
            val settings = exportSettingsRepository.settingsFlow.first()
            exportSettingsRepository.saveSettings(
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
                exportedFileName = null,
                exportError = ExportSettingsError.Validation(validationError),
            )
            return
        }

        viewModelScope.launch {
            val settings = exportSettingsRepository.settingsFlow.first()
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
                exportSettingsRepository.saveSettings(updatedSettings)
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
        val settings = exportSettingsRepository.settingsFlow.first()
        if (!settings.automaticExportPending && settings.lastAutomaticExportError == null) {
            return
        }

        exportSettingsRepository.saveSettings(
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

    private fun ExportProgress.toUiProgress(): ExportUiProgress = when (this) {
        ExportProgress.Validating,
        ExportProgress.LoadingProfile,
        ExportProgress.LoadingMeasurements -> ExportUiProgress(
            step = ExportProgressStep.CollectingData,
        )

        is ExportProgress.CollectingPhotos -> ExportUiProgress(
            step = ExportProgressStep.CollectingData,
            current = processedMeasurementCount,
            total = totalMeasurementCount.takeIf { it > 0 },
        )

        is ExportProgress.WritingArchiveData -> ExportUiProgress(
            step = ExportProgressStep.WritingArchiveData(
                current = currentDocumentIndex,
                total = totalDocumentCount,
            ),
            current = currentDocumentIndex,
            total = totalDocumentCount,
        )

        is ExportProgress.WritingPhoto -> ExportUiProgress(
            step = ExportProgressStep.WritingPhoto(
                current = currentPhotoIndex,
                total = totalPhotoCount,
            ),
            current = currentPhotoIndex,
            total = totalPhotoCount,
        )

        ExportProgress.CleaningUpOldExports -> ExportUiProgress(
            step = ExportProgressStep.CleaningUp,
        )
    }
}
