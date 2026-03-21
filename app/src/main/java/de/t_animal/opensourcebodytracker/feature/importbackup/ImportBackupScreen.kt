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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import de.t_animal.opensourcebodytracker.ui.components.PasswordTextField
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

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                ImportBackupEvent.ImportCompleted -> onImportCompleted()
                is ImportBackupEvent.CatastrophicFailure -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                    onResetApp()
                }
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = resolveFileName(context, uri) ?: "Selected file"
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportBackupScreen(
    state: ImportBackupUiState,
    onSelectFileClicked: () -> Unit,
    onPasswordChanged: (String) -> Unit,
    onImportClicked: () -> Unit,
    onCancelClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Backup") },
                navigationIcon = {
                    IconButton(onClick = onCancelClicked, enabled = !state.isImporting) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp),
        ) {
            Text(
                text = "Restore your data from a previously exported backup. " +
                    "Select the encrypted ZIP file and enter the password you used during export.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            PasswordTextField(
                value = state.password,
                onValueChange = onPasswordChanged,
                enabled = !state.isImporting,
                label = { Text("Backup Password") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onSelectFileClicked,
                enabled = !state.isImporting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(state.selectedFileName ?: "Select File")
            }

            if (!state.errorMessage.isNullOrBlank()) {
                Text(
                    text = state.errorMessage,
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
                    Text("Cancel")
                }

                Button(
                    onClick = onImportClicked,
                    enabled = state.isImportEnabled,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Import")
                }
            }

            val progress = state.progress
            if (progress != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = progress.message,
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
