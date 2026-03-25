package de.t_animal.opensourcebodytracker.feature.settings.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import de.t_animal.opensourcebodytracker.core.model.AutomaticExportErrorKey
import de.t_animal.opensourcebodytracker.data.export.ExportPasswordRepository
import de.t_animal.opensourcebodytracker.data.settings.ExportSettingsRepository
import de.t_animal.opensourcebodytracker.domain.export.ExportActionError
import de.t_animal.opensourcebodytracker.domain.export.ExportActionResult
import de.t_animal.opensourcebodytracker.domain.export.ExportExecutionCommand
import de.t_animal.opensourcebodytracker.domain.export.ExportProgress
import de.t_animal.opensourcebodytracker.domain.export.ExportToFilesystemUseCase
import de.t_animal.opensourcebodytracker.domain.export.ExportValidationError
import de.t_animal.opensourcebodytracker.domain.export.SaveExportSettingsCommand
import de.t_animal.opensourcebodytracker.domain.export.SaveExportSettingsUseCase
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
) {
    fun withDeviceStorageEnabled(enabled: Boolean): ExportSettingsUiState {
        val canEnable = enabled && exportPassword.isNotBlank()
        return copy(
            exportToDeviceStorageEnabled = canEnable,
            exportFolderUri = if (canEnable) exportFolderUri else null,
            automaticExportEnabled = if (canEnable) automaticExportEnabled else false,
            exportProgress = null,
            exportedFileName = null,
            exportError = null,
        )
    }

    fun withAutomaticExportEnabled(enabled: Boolean): ExportSettingsUiState {
        val canEnable = enabled && exportToDeviceStorageEnabled && exportPassword.isNotBlank()
        return copy(
            automaticExportEnabled = canEnable,
            exportedFileName = null,
            exportError = null,
        )
    }

    fun withPasswordChanged(password: String): ExportSettingsUiState {
        val hasPassword = password.isNotBlank()
        return copy(
            exportPassword = password,
            exportToDeviceStorageEnabled = exportToDeviceStorageEnabled && hasPassword,
            automaticExportEnabled = automaticExportEnabled && hasPassword,
            exportProgress = null,
            exportedFileName = null,
            exportError = null,
        )
    }

    fun withFolderSelected(uri: String): ExportSettingsUiState = copy(
        exportFolderUri = uri,
        exportProgress = null,
        exportedFileName = null,
        exportError = null,
    )
}

sealed interface ExportSettingsEvent {
    data object Saved : ExportSettingsEvent
}

@HiltViewModel
class ExportSettingsViewModel @Inject constructor(
    private val exportSettingsRepository: ExportSettingsRepository,
    private val exportPasswordRepository: ExportPasswordRepository,
    private val exportToFileSystemUseCase: ExportToFilesystemUseCase,
    private val saveExportSettingsUseCase: SaveExportSettingsUseCase,
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
        _uiState.value = _uiState.value.withDeviceStorageEnabled(enabled)
    }

    fun onAutomaticExportEnabledChanged(enabled: Boolean) {
        _uiState.value = _uiState.value.withAutomaticExportEnabled(enabled)
    }

    fun onExportFolderSelected(uri: String) {
        _uiState.value = _uiState.value.withFolderSelected(uri)
    }

    fun onExportPasswordChanged(password: String) {
        _uiState.value = _uiState.value.withPasswordChanged(password)
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
                    saveExportSettingsUseCase.clearAutomaticExportState()
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
            saveExportSettingsUseCase.dismissAutomaticExportError()
            _uiState.update { it.copy(lastAutomaticExportError = null) }
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
            saveExportSettingsUseCase.save(
                SaveExportSettingsCommand(
                    exportToDeviceStorageEnabled = current.exportToDeviceStorageEnabled,
                    exportFolderUri = current.exportFolderUri,
                    exportPassword = current.exportPassword,
                    automaticExportEnabled = current.automaticExportEnabled,
                ),
            )
            _events.emit(ExportSettingsEvent.Saved)
        }
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
