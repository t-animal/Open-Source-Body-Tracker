package de.t_animal.opensourcebodytracker.feature.settings.export

import android.Manifest
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.t_animal.opensourcebodytracker.data.export.ExportPasswordRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.export.AutomaticExportScheduler
import de.t_animal.opensourcebodytracker.domain.export.ExportToFilesystemUseCase
import de.t_animal.opensourcebodytracker.ui.components.PasswordTextField
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@Composable
fun ExportSettingsRoute(
    settingsRepository: SettingsRepository,
    exportPasswordRepository: ExportPasswordRepository,
    exportToFileSystemUseCase: ExportToFilesystemUseCase,
    automaticExportScheduler: AutomaticExportScheduler,
    onNavigateBack: () -> Unit,
) {
    val vm: ExportSettingsViewModel = viewModel(
        factory = ExportSettingsViewModelFactory(
            settingsRepository = settingsRepository,
            exportPasswordRepository = exportPasswordRepository,
            exportToFileSystemUseCase = exportToFileSystemUseCase,
            automaticExportScheduler = automaticExportScheduler,
        ),
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showNotificationPermissionDialog by remember { mutableStateOf(false) }
    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }

        val permissionFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        runCatching {
            context.contentResolver.takePersistableUriPermission(uri, permissionFlags)
        }

        vm.onExportFolderSelected(uri.toString())
    }
    val requestNotificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            vm.onAutomaticExportEnabledChanged(true)
        }
    }

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                ExportSettingsEvent.Saved -> onNavigateBack()
            }
        }
    }

    ExportSettingsScreen(
        state = state,
        onBackClicked = onNavigateBack,
        onExportToDeviceStorageEnabledChanged = vm::onExportToDeviceStorageEnabledChanged,
        onAutomaticExportEnabledChanged = { enabled ->
            if (!enabled) {
                vm.onAutomaticExportEnabledChanged(false)
            } else if (shouldRequestNotificationPermission(context)) {
                showNotificationPermissionDialog = true
            } else {
                vm.onAutomaticExportEnabledChanged(true)
            }
        },
        onSelectFolderClicked = { folderPicker.launch(state.exportFolderUri?.toUri()) },
        onExportPasswordChanged = vm::onExportPasswordChanged,
        onExportNowClicked = vm::onExportNowClicked,
        onSaveClicked = vm::onSaveClicked,
        onDismissAutomaticExportError = vm::onDismissAutomaticExportError,
    )

    if (showNotificationPermissionDialog) {
        AutomaticExportPermissionDialog(
            onDismiss = { showNotificationPermissionDialog = false },
            onContinue = {
                showNotificationPermissionDialog = false
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportSettingsScreen(
    state: ExportSettingsUiState,
    onBackClicked: () -> Unit,
    onExportToDeviceStorageEnabledChanged: (Boolean) -> Unit,
    onAutomaticExportEnabledChanged: (Boolean) -> Unit,
    onSelectFolderClicked: () -> Unit,
    onExportPasswordChanged: (String) -> Unit,
    onExportNowClicked: () -> Unit,
    onSaveClicked: () -> Unit,
    onDismissAutomaticExportError: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { contentPadding ->
        if (state.isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(16.dp),
        ) {
            val hasPassword = state.exportPassword.isNotBlank()

            if (!state.lastAutomaticExportError.isNullOrBlank()) {
                ExportErrorBanner(
                    errorMessage = state.lastAutomaticExportError,
                    onDismiss = onDismissAutomaticExportError,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = buildAnnotatedString {
                    append(
                        "Your measurements, profile and photos are exported into an encrypted " +
                            "ZIP archive in the folder you choose. Nightly automatic exports " +
                            "use the same destination and require notification permission so " +
                            "Android can keep the background export alive."
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Light),
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = buildAnnotatedString {
                            append("Set a password to encrypt your exported data.  ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("You need this password to import your data later, so make sure to keep it safe! ")
                            }
                            append(
                                "All usual password security rules apply here: No reuse, long and complex passwords are best.",
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Light),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PasswordTextField(
                        value = state.exportPassword,
                        onValueChange = onExportPasswordChanged,
                        label = { Text("Encryption Password") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Export to Device Storage",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Switch(
                            checked = state.exportToDeviceStorageEnabled,
                            onCheckedChange = onExportToDeviceStorageEnabledChanged,
                            enabled = hasPassword && !state.isExporting,
                        )
                    }

                    if (!hasPassword) {
                        Text(
                            text = "Enter an export password to enable export destinations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append("Export folder: ")
                            }
                            val folderUri = state.exportFolderUri
                            if (!folderUri.isNullOrBlank()) {
                                append(
                                    folderUri.replace("content://com.android.externalstorage.documents/tree/", "")
                                        .replace("%3A", "/")
                                )
                            } else {
                                append("Not set")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onSelectFolderClicked,
                        enabled = state.exportToDeviceStorageEnabled,
                    ) {
                        Text("Select Folder")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "Nightly Automatic Backup",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Runs around 3:00 AM when measurements changed.",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Switch(
                            checked = state.automaticExportEnabled,
                            onCheckedChange = onAutomaticExportEnabledChanged,
                            enabled = state.exportToDeviceStorageEnabled && hasPassword && !state.isExporting,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Export to Google Drive (coming later)",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Switch(
                            checked = false,
                            onCheckedChange = {},
                            enabled = false,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onExportNowClicked,
                enabled = state.exportToDeviceStorageEnabled && hasPassword && !state.isExporting,
            ) {
                Text(if (state.isExporting) "Exporting..." else "Export Now")
            }

            val progress = state.exportProgress
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
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            val status = state.statusMessage
            if (!status.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = status,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            val error = state.errorMessage
            if (!error.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onBackClicked,
                    enabled = !state.isExporting,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = onSaveClicked,
                    enabled = !state.isExporting,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun ExportErrorBanner(
    errorMessage: String,
    onDismiss: () -> Unit,
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "Automatic Backup Failed",
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun AutomaticExportPermissionDialog(
    onDismiss: () -> Unit,
    onContinue: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Allow Notifications for Nightly Backups") },
        text = {
            Text(
                "Android requires a notification for the nightly background export. " +
                    "We use it to show progress while photos are being written so the backup can finish reliably.",
            )
        },
        confirmButton = {
            Button(onClick = onContinue) {
                Text("Continue")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

private fun shouldRequestNotificationPermission(
    context: Context,
): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return false
    }

    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
    ) != PackageManager.PERMISSION_GRANTED
}

@Preview(showBackground = true)
@Composable
private fun ExportSettingsScreenPreview() {
    BodyTrackerTheme {
        ExportSettingsScreen(
            state = ExportSettingsUiState(
                isLoading = false,
                exportToDeviceStorageEnabled = true,
                exportFolderUri = "content://com.android.externalstorage.documents/tree/primary%3ADownload",
                exportPassword = "secret",
                automaticExportEnabled = true,
            ),
            onBackClicked = {},
            onExportToDeviceStorageEnabledChanged = {},
            onAutomaticExportEnabledChanged = {},
            onSelectFolderClicked = {},
            onExportPasswordChanged = {},
            onExportNowClicked = {},
            onSaveClicked = {},
            onDismissAutomaticExportError = {},
        )
    }
}
