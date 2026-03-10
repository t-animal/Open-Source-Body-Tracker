package de.t_animal.opensourcebodytracker.feature.settings.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.data.export.ExportPasswordRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.export.ExportActionError
import de.t_animal.opensourcebodytracker.domain.export.CreateLocalExportTestFileUseCase
import de.t_animal.opensourcebodytracker.domain.export.ExportActionResult
import de.t_animal.opensourcebodytracker.domain.export.ExportExecutionCommand
import de.t_animal.opensourcebodytracker.domain.export.ExportValidationError
import de.t_animal.opensourcebodytracker.domain.export.validateExportCommandForSave
import de.t_animal.opensourcebodytracker.domain.export.validateExportExecutionCommand
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ExportSettingsUiState(
    val isLoading: Boolean = true,
    val isExporting: Boolean = false,
    val exportToDeviceStorageEnabled: Boolean = false,
    val exportFolderUri: String? = null,
    val exportPassword: String = "",
    val statusMessage: String? = null,
    val errorMessage: String? = null,
)

sealed interface ExportSettingsEvent {
    data object Saved : ExportSettingsEvent
}

class ExportSettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val exportPasswordRepository: ExportPasswordRepository,
    private val createLocalExportTestFileUseCase: CreateLocalExportTestFileUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExportSettingsUiState())
    val uiState: StateFlow<ExportSettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ExportSettingsEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settingsFlow.first()
            val password = exportPasswordRepository.getPassword().orEmpty()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isExporting = false,
                exportToDeviceStorageEnabled = settings.exportToDeviceStorageEnabled,
                exportFolderUri = settings.exportFolderUri,
                exportPassword = password,
                statusMessage = null,
                errorMessage = null,
            )
        }
    }

    fun onExportToDeviceStorageEnabledChanged(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            exportToDeviceStorageEnabled = enabled,
            exportFolderUri = if (enabled) _uiState.value.exportFolderUri else null,
            statusMessage = null,
            errorMessage = null,
        )
    }

    fun onExportFolderSelected(uri: String) {
        _uiState.value = _uiState.value.copy(
            exportFolderUri = uri,
            statusMessage = null,
            errorMessage = null,
        )
    }

    fun onExportPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(
            exportPassword = password,
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
            statusMessage = null,
            errorMessage = null,
        )

        viewModelScope.launch {
            val result = createLocalExportTestFileUseCase(command)
            _uiState.value = when (result) {
                is ExportActionResult.Success -> _uiState.value.copy(
                    isExporting = false,
                    statusMessage = EXPORT_TEST_SUCCESS_MESSAGE,
                    errorMessage = null,
                )

                is ExportActionResult.Failure -> _uiState.value.copy(
                    isExporting = false,
                    statusMessage = null,
                    errorMessage = result.error.toUserMessage(),
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
                statusMessage = null,
                errorMessage = validationError.toUserMessage(),
            )
            return
        }

        viewModelScope.launch {
            val settings = settingsRepository.settingsFlow.first()
            val updatedSettings = settings.copy(
                exportToDeviceStorageEnabled = current.exportToDeviceStorageEnabled,
                exportFolderUri = current.exportFolderUri,
            )
            if (updatedSettings != settings) {
                settingsRepository.saveSettings(updatedSettings)
            }
            exportPasswordRepository.savePassword(current.exportPassword)
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

    private fun ExportValidationError.toUserMessage(): String = when (this) {
        ExportValidationError.EnableDeviceStorage -> "Enable export to device storage"
        ExportValidationError.SelectFolder -> "Select an export folder"
        ExportValidationError.EnterPassword -> "Enter an export password"
    }

    private fun ExportActionError.toUserMessage(): String = when (this) {
        is ExportActionError.Validation -> error.toUserMessage()
        ExportActionError.InvalidFolder -> "Export folder is invalid. Select folder again"
        ExportActionError.PermissionDenied -> "Export folder permission was lost. Select folder again"
        ExportActionError.WriteFailed -> "Could not write export test file"
        ExportActionError.Unknown -> "Export failed"
    }

    private companion object {
        const val EXPORT_TEST_SUCCESS_MESSAGE = "Export test file created"
    }
}

class ExportSettingsViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val exportPasswordRepository: ExportPasswordRepository,
    private val createLocalExportTestFileUseCase: CreateLocalExportTestFileUseCase,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExportSettingsViewModel(
            settingsRepository = settingsRepository,
            exportPasswordRepository = exportPasswordRepository,
            createLocalExportTestFileUseCase = createLocalExportTestFileUseCase,
        ) as T
    }
}
