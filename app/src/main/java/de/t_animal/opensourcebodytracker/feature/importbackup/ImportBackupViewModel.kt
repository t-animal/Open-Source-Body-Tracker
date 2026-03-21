package de.t_animal.opensourcebodytracker.feature.importbackup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.domain.importbackup.ImportBackupUseCase
import de.t_animal.opensourcebodytracker.domain.importbackup.ImportResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImportBackupViewModel(
    private val importBackupUseCase: ImportBackupUseCase,
) : ViewModel() {
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

    fun onImportClicked(context: Context) {
        val state = _uiState.value
        val uri = state.selectedFileUri ?: return
        if (state.isImporting) return

        _uiState.value = state.copy(isImporting = true, errorMessage = null)

        viewModelScope.launch(Dispatchers.IO) {
            val result = importBackupUseCase(context, uri, state.password)

            when (result) {
                is ImportResult.Success -> {
                    _events.emit(ImportBackupEvent.ImportCompleted)
                }

                is ImportResult.SuccessWithWarning -> {
                    _events.emit(ImportBackupEvent.ImportCompleted)
                }

                is ImportResult.CatastrophicFailure -> {
                    _events.emit(ImportBackupEvent.CatastrophicFailure(result.message))
                }

                is ImportResult.WrongPassword -> {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        errorMessage = result.message,
                    )
                }

                is ImportResult.UnsupportedFormat -> {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        errorMessage = result.message,
                    )
                }

                is ImportResult.IncompleteBackup -> {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        errorMessage = result.message,
                    )
                }

                is ImportResult.GeneralFailure -> {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        errorMessage = result.message,
                    )
                }
            }
        }
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
    data class CatastrophicFailure(val message: String) : ImportBackupEvent
}

class ImportBackupViewModelFactory(
    private val importBackupUseCase: ImportBackupUseCase,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ImportBackupViewModel(importBackupUseCase) as T
    }
}
