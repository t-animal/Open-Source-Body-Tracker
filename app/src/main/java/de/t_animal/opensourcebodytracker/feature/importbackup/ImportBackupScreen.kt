package de.t_animal.opensourcebodytracker.feature.importbackup

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.domain.importbackup.ImportResult
import de.t_animal.opensourcebodytracker.ui.components.PasswordTextField
import de.t_animal.opensourcebodytracker.ui.components.SecondaryScreenScaffold
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@Composable
fun ImportBackupRoute(
    onNavigateBack: () -> Unit,
    onImportCompleted: () -> Unit,
    onResetApp: () -> Unit,
) {
    val vm: ImportBackupViewModel = hiltViewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val msgDatabase = stringResource(R.string.import_catastrophic_database)
    val msgPhotos = stringResource(R.string.import_catastrophic_photos)
    val msgPhotoVerification = stringResource(R.string.import_catastrophic_photo_verification)
    val msgSettings = stringResource(R.string.import_catastrophic_settings)
    val defaultFileName = stringResource(R.string.common_selected_file)

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                ImportBackupEvent.ImportCompleted -> onImportCompleted()
                is ImportBackupEvent.CatastrophicFailure -> {
                    val message = when (event.result) {
                        ImportResult.CatastrophicFailure.DatabaseWriteFailed -> msgDatabase
                        ImportResult.CatastrophicFailure.PhotoExtractionFailed -> msgPhotos
                        ImportResult.CatastrophicFailure.PhotoVerificationFailed -> msgPhotoVerification
                        ImportResult.CatastrophicFailure.SettingsWriteFailed -> msgSettings
                    }
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    onResetApp()
                }
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = resolveFileName(context, uri) ?: defaultFileName
            vm.onFileSelected(uri, fileName)
        }
    }

    ImportBackupScreen(
        state = state,
        onSelectFileClicked = { filePickerLauncher.launch(arrayOf("application/zip")) },
        onPasswordChanged = vm::onPasswordChanged,
        onImportClicked = { vm.onImportClicked(context) },
        onCancelClicked = onNavigateBack,
    )
}

private fun resolveFileName(context: android.content.Context, uri: Uri): String? {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) {
            cursor.getString(nameIndex)
        } else {
            null
        }
    }
}

@Composable
fun ImportBackupScreen(
    state: ImportBackupUiState,
    onSelectFileClicked: () -> Unit,
    onPasswordChanged: (String) -> Unit,
    onImportClicked: () -> Unit,
    onCancelClicked: () -> Unit,
) {
    SecondaryScreenScaffold(
        title = stringResource(R.string.import_title),
        onNavigateBack = onCancelClicked,
        backEnabled = !state.isImporting,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.import_description),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            PasswordTextField(
                value = state.password,
                onValueChange = onPasswordChanged,
                enabled = !state.isImporting,
                label = { Text(stringResource(R.string.import_label_password)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onSelectFileClicked,
                enabled = !state.isImporting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(state.selectedFileName ?: stringResource(R.string.common_selected_file))
            }

            val errorMessage = when (val error = state.errorResult) {
                ImportResult.RecoverableError.WrongPassword -> stringResource(R.string.import_error_wrong_password)
                is ImportResult.RecoverableError.UnsupportedVersion -> stringResource(R.string.import_error_unsupported_version, error.foundVersion, error.supportedVersion)
                ImportResult.RecoverableError.InvalidArchive -> stringResource(R.string.import_error_invalid_archive)
                ImportResult.RecoverableError.IncompleteBackup -> stringResource(R.string.import_error_incomplete)
                ImportResult.RecoverableError.FileNotReadable -> stringResource(R.string.import_error_file_not_readable)
                null -> null
            }
            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onCancelClicked,
                    enabled = !state.isImporting,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.common_cancel))
                }

                Button(
                    onClick = onImportClicked,
                    enabled = state.isImportEnabled,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.import_button_import))
                }
            }

            val progress = state.progress
            if (progress != null) {
                val progressMessage = when (progress.step) {
                    ImportProgressStep.ReadingBackup -> stringResource(R.string.import_progress_reading)
                    ImportProgressStep.ValidatingArchive -> stringResource(R.string.import_progress_validating)
                    ImportProgressStep.ReadingProfile -> stringResource(R.string.import_progress_reading_profile)
                    ImportProgressStep.ReadingMeasurements -> stringResource(R.string.import_progress_reading_measurements)
                    ImportProgressStep.SavingToDatabase -> stringResource(R.string.import_progress_saving)
                    is ImportProgressStep.ExtractingPhotos -> stringResource(R.string.import_progress_extracting_photos)
                    ImportProgressStep.VerifyingPhotos -> stringResource(R.string.import_progress_verifying_photos)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = progressMessage,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (progress.isDeterminate) {
                    LinearProgressIndicator(
                        progress = { progress.progressFraction ?: 0f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ImportBackupScreenPreview() {
    BodyTrackerTheme {
        ImportBackupScreen(
            state = ImportBackupUiState(),
            onSelectFileClicked = {},
            onPasswordChanged = {},
            onImportClicked = {},
            onCancelClicked = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ImportBackupScreenWithFilePreview() {
    BodyTrackerTheme {
        ImportBackupScreen(
            state = ImportBackupUiState(
                selectedFileName = "backup_2026-03-20.zip",
                password = "secret",
            ),
            onSelectFileClicked = {},
            onPasswordChanged = {},
            onImportClicked = {},
            onCancelClicked = {},
        )
    }
}
