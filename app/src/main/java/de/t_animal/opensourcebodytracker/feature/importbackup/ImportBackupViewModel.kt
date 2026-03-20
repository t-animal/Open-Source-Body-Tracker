package de.t_animal.opensourcebodytracker.feature.importbackup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class ImportBackupViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ImportBackupUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ImportBackupEvent>()
    val events = _events.asSharedFlow()

    fun onFileSelected(uri: Uri, fileName: String) {
        _uiState.value = _uiState.value.copy(
            selectedFileUri = uri,
            selectedFileName = fileName,
            errorMessage = null,
        )
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            errorMessage = null,
        )
    }

    fun onImportClicked() {
        // No-op in Stage 1
    }
}

data class ImportBackupUiState(
    val selectedFileUri: Uri? = null,
    val selectedFileName: String? = null,
    val password: String = "",
    val isImporting: Boolean = false,
    val errorMessage: String? = null,
) {
    val isImportEnabled: Boolean
        get() = selectedFileUri != null && password.isNotEmpty() && !isImporting
}

sealed interface ImportBackupEvent {
    data object ImportCompleted : ImportBackupEvent
}

class ImportBackupViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ImportBackupViewModel() as T
    }
}
