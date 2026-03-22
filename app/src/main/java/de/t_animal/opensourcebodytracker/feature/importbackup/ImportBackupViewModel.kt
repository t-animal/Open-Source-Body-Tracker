package de.t_animal.opensourcebodytracker.feature.importbackup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.t_animal.opensourcebodytracker.domain.importbackup.ImportBackupUseCase
import de.t_animal.opensourcebodytracker.domain.importbackup.ImportProgress
import de.t_animal.opensourcebodytracker.domain.importbackup.ImportResult
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ImportProgressStep {
    data object ReadingBackup : ImportProgressStep
    data object ValidatingArchive : ImportProgressStep
    data object ReadingProfile : ImportProgressStep
    data object ReadingMeasurements : ImportProgressStep
    data object SavingToDatabase : ImportProgressStep
    data class ExtractingPhotos(val current: Int, val total: Int) : ImportProgressStep
    data object VerifyingPhotos : ImportProgressStep
}

@HiltViewModel
class ImportBackupViewModel @Inject constructor(
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
            errorResult = null,
        )
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            errorResult = null,
        )
    }

    fun onImportClicked(context: Context) {
        val state = _uiState.value
        val uri = state.selectedFileUri ?: return
        if (state.isImporting) return

        _uiState.value = state.copy(
            isImporting = true,
            errorResult = null,
            progress = ImportUiProgress(step = ImportProgressStep.ReadingBackup),
        )

        viewModelScope.launch(Dispatchers.IO) {
            val result = importBackupUseCase(
                context = context,
                fileUri = uri,
                password = state.password,
                onProgress = { progress ->
                    _uiState.value = _uiState.value.copy(
                        progress = progress.toUiProgress(),
                    )
                },
            )

            when (result) {
                is ImportResult.Success -> {
                    _events.emit(ImportBackupEvent.ImportCompleted)
                }

                is ImportResult.SuccessWithWarning -> {
                    _events.emit(ImportBackupEvent.ImportCompleted)
                }

                is ImportResult.CatastrophicFailure -> {
                    _events.emit(
                        ImportBackupEvent.CatastrophicFailure(result),
                    )
                }

                is ImportResult.RecoverableError -> {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        progress = null,
                        errorResult = result,
                    )
                }
            }
        }
    }

    private fun ImportProgress.toUiProgress(): ImportUiProgress = when (this) {
        ImportProgress.ValidatingArchive -> ImportUiProgress(step = ImportProgressStep.ValidatingArchive)
        ImportProgress.ReadingProfile -> ImportUiProgress(step = ImportProgressStep.ReadingProfile)
        ImportProgress.ReadingMeasurements -> ImportUiProgress(step = ImportProgressStep.ReadingMeasurements)
        ImportProgress.SavingToDatabase -> ImportUiProgress(step = ImportProgressStep.SavingToDatabase)
        is ImportProgress.ExtractingPhotos -> ImportUiProgress(
            step = ImportProgressStep.ExtractingPhotos(current = current, total = total),
            current = current,
            total = total,
        )
        ImportProgress.VerifyingPhotos -> ImportUiProgress(step = ImportProgressStep.VerifyingPhotos)
    }
}

data class ImportUiProgress(
    val step: ImportProgressStep,
    val current: Int? = null,
    val total: Int? = null,
) {
    val isDeterminate: Boolean
        get() = current != null && total != null && total > 0

    val progressFraction: Float?
        get() = if (isDeterminate) current!!.toFloat() / total!!.toFloat() else null
}

data class ImportBackupUiState(
    val selectedFileUri: Uri? = null,
    val selectedFileName: String? = null,
    val password: String = "",
    val isImporting: Boolean = false,
    val progress: ImportUiProgress? = null,
    val errorResult: ImportResult.RecoverableError? = null,
) {
    val isImportEnabled: Boolean
        get() = selectedFileUri != null && password.isNotEmpty() && !isImporting
}

sealed interface ImportBackupEvent {
    data object ImportCompleted : ImportBackupEvent
    data class CatastrophicFailure(val result: ImportResult.CatastrophicFailure) : ImportBackupEvent
}

