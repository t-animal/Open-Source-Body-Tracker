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

        _uiState.value = state.copy(
            isImporting = true,
            errorMessage = null,
            progress = ImportUiProgress("Reading backup file…"),
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
                        ImportBackupEvent.CatastrophicFailure(
                            catastrophicFailureMessage(result),
                        ),
                    )
                }

                ImportResult.WrongPassword,
                is ImportResult.UnsupportedVersion,
                ImportResult.IncompleteBackup,
                ImportResult.InvalidArchive,
                ImportResult.FileNotReadable -> {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        progress = null,
                        errorMessage = recoverableErrorMessage(result),
                    )
                }
            }
        }
    }

    private fun recoverableErrorMessage(result: ImportResult): String = when (result) {
        ImportResult.WrongPassword -> "Wrong password. Please try again."
        is ImportResult.UnsupportedVersion ->
            "Unsupported backup version (${result.foundVersion}). " +
                "This app supports version ${result.supportedVersion}."
        ImportResult.InvalidArchive -> "The selected file is not a valid ZIP archive."
        ImportResult.IncompleteBackup ->
            "The backup is incomplete or corrupted. It may not be a valid backup."
        ImportResult.FileNotReadable -> "Could not read the selected file."
        else -> "An unexpected error occurred."
    }

    private fun catastrophicFailureMessage(
        result: ImportResult.CatastrophicFailure,
    ): String = when (result) {
        ImportResult.CatastrophicFailure.DatabaseWriteFailed ->
            "Failed to save data to the database. The app may be in an inconsistent state."
        ImportResult.CatastrophicFailure.PhotoExtractionFailed ->
            "Photos could not be restored after data was saved. " +
                "The app may be in an inconsistent state."
        ImportResult.CatastrophicFailure.PhotoVerificationFailed ->
            "Some photos could not be verified after extraction. " +
                "The app may be in an inconsistent state."
        ImportResult.CatastrophicFailure.SettingsWriteFailed ->
            "Data was imported but settings could not be saved. " +
                "The app may be in an inconsistent state."
    }

    private fun ImportProgress.toUiProgress(): ImportUiProgress = when (this) {
        ImportProgress.ValidatingArchive -> ImportUiProgress("Validating archive…")
        ImportProgress.ReadingProfile -> ImportUiProgress("Reading profile…")
        ImportProgress.ReadingMeasurements -> ImportUiProgress("Reading measurements…")
        ImportProgress.SavingToDatabase -> ImportUiProgress("Saving to database…")
        is ImportProgress.ExtractingPhotos -> ImportUiProgress(
            message = "Restoring photos…",
            current = current,
            total = total,
        )
        ImportProgress.VerifyingPhotos -> ImportUiProgress("Verifying photos…")
    }
}

data class ImportUiProgress(
    val message: String,
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
    val errorMessage: String? = null,
) {
    val isImportEnabled: Boolean
        get() = selectedFileUri != null && password.isNotEmpty() && !isImporting
}

sealed interface ImportBackupEvent {
    data object ImportCompleted : ImportBackupEvent
    data class CatastrophicFailure(val message: String) : ImportBackupEvent
}

