package de.t_animal.opensourcebodytracker.feature.settings.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.data.export.ExportPasswordRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ExportSettingsUiState(
    val isLoading: Boolean = true,
    val exportToDeviceStorageEnabled: Boolean = false,
    val exportFolderUri: String? = null,
    val exportPassword: String = "",
    val errorMessage: String? = null,
)

sealed interface ExportSettingsEvent {
    data object Saved : ExportSettingsEvent
}

class ExportSettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val exportPasswordRepository: ExportPasswordRepository,
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
                exportToDeviceStorageEnabled = settings.exportToDeviceStorageEnabled,
                exportFolderUri = settings.exportFolderUri,
                exportPassword = password,
                errorMessage = null,
            )
        }
    }

    fun onExportToDeviceStorageEnabledChanged(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            exportToDeviceStorageEnabled = enabled,
            exportFolderUri = if (enabled) _uiState.value.exportFolderUri else null,
            errorMessage = null,
        )
    }

    fun onExportFolderSelected(uri: String) {
        _uiState.value = _uiState.value.copy(
            exportFolderUri = uri,
            errorMessage = null,
        )
    }

    fun onExportPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(
            exportPassword = password,
            errorMessage = null,
        )
    }

    fun onSaveClicked() {
        val current = _uiState.value
        if (current.isLoading) {
            return
        }

        if (current.exportToDeviceStorageEnabled && current.exportFolderUri.isNullOrBlank()) {
            _uiState.value = current.copy(errorMessage = "Select an export folder")
            return
        }

        if (current.exportToDeviceStorageEnabled && current.exportPassword.isBlank()) {
            _uiState.value = current.copy(errorMessage = "Enter an export password")
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
}

class ExportSettingsViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val exportPasswordRepository: ExportPasswordRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExportSettingsViewModel(
            settingsRepository = settingsRepository,
            exportPasswordRepository = exportPasswordRepository,
        ) as T
    }
}
