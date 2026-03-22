package de.t_animal.opensourcebodytracker.feature.settings.export

import android.Manifest
import androidx.annotation.StringRes
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.AutomaticExportErrorKey
import de.t_animal.opensourcebodytracker.domain.export.ExportActionError
import de.t_animal.opensourcebodytracker.domain.export.ExportValidationError
import de.t_animal.opensourcebodytracker.ui.components.PasswordTextField
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@Composable
fun ExportSettingsRoute(
    onNavigateBack: () -> Unit,
) {
    val vm: ExportSettingsViewModel = hiltViewModel()
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
                title = { Text(stringResource(R.string.export_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
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

            val autoExportError = state.lastAutomaticExportError?.let { error ->
                stringResource(error.stringResourceId)
            }
            if (autoExportError != null) {
                ExportErrorBanner(
                    errorMessage = autoExportError,
                    onDismiss = onDismissAutomaticExportError,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = stringResource(R.string.export_description),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Light),
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = buildAnnotatedString {
                            append(stringResource(R.string.export_password_description_prefix))
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(stringResource(R.string.export_password_description_bold))
                            }
                            append(stringResource(R.string.export_password_description_suffix))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Light),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PasswordTextField(
                        value = state.exportPassword,
                        onValueChange = onExportPasswordChanged,
                        label = { Text(stringResource(R.string.export_label_password)) },
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
                            text = stringResource(R.string.export_label_device_storage),
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
                            text = stringResource(R.string.export_enable_password_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(stringResource(R.string.export_label_folder_prefix))
                            }
                            val folderUri = state.exportFolderUri
                            if (!folderUri.isNullOrBlank()) {
                                append(
                                    folderUri.replace("content://com.android.externalstorage.documents/tree/", "")
                                        .replace("%3A", "/")
                                )
                            } else {
                                append(stringResource(R.string.export_folder_not_set))
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
                        Text(stringResource(R.string.export_button_select_folder))
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
                                text = stringResource(R.string.export_label_automatic),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.export_automatic_description),
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
                            text = stringResource(R.string.export_label_google_drive),
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
                Text(stringResource(if (state.isExporting) R.string.export_button_exporting else R.string.export_button_export_now))
            }

            val progress = state.exportProgress
            if (progress != null) {
                val progressMessage = when (val step = progress.step) {
                    ExportProgressStep.CollectingData -> stringResource(R.string.export_progress_collecting)
                    is ExportProgressStep.WritingArchiveData -> stringResource(R.string.export_progress_writing_archive)
                    is ExportProgressStep.WritingPhoto -> pluralStringResource(R.plurals.export_progress_writing_photo, step.total, step.current, step.total)
                    ExportProgressStep.CleaningUp -> stringResource(R.string.export_progress_cleaning)
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
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            val statusMessage = state.exportedFileName?.let { stringResource(R.string.export_success_message, it) }
            if (!statusMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = statusMessage,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            val errorMessage = when (val error = state.exportError) {
                is ExportSettingsError.Validation -> when (error.error) {
                    ExportValidationError.EnableDeviceStorage -> stringResource(R.string.export_error_enable_storage)
                    ExportValidationError.SelectFolder -> stringResource(R.string.export_error_select_folder)
                    ExportValidationError.EnterPassword -> stringResource(R.string.export_error_enter_password)
                }
                is ExportSettingsError.Action -> when (error.error) {
                    is ExportActionError.Validation -> when (error.error.error) {
                        ExportValidationError.EnableDeviceStorage -> stringResource(R.string.export_error_enable_storage)
                        ExportValidationError.SelectFolder -> stringResource(R.string.export_error_select_folder)
                        ExportValidationError.EnterPassword -> stringResource(R.string.export_error_enter_password)
                    }
                    ExportActionError.InvalidFolder -> stringResource(R.string.export_error_invalid_folder)
                    ExportActionError.PermissionDenied -> stringResource(R.string.export_error_permission_denied)
                    ExportActionError.WriteFailed -> stringResource(R.string.export_error_write_failed)
                    ExportActionError.Unknown -> stringResource(R.string.export_error_unknown)
                }
                null -> null
            }
            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
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
                    Text(stringResource(R.string.common_cancel))
                }
                Button(
                    onClick = onSaveClicked,
                    enabled = !state.isExporting,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.common_save))
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
                text = stringResource(R.string.export_error_banner_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.common_dismiss))
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
        title = { Text(stringResource(R.string.export_permission_dialog_title)) },
        text = {
            Text(stringResource(R.string.export_permission_dialog_body))
        },
        confirmButton = {
            Button(onClick = onContinue) {
                Text(stringResource(R.string.common_continue))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    )
}

private val AutomaticExportErrorKey.stringResourceId: Int
    @StringRes get() = when (this) {
        AutomaticExportErrorKey.EnableDeviceStorage -> R.string.auto_export_error_EnableDeviceStorage
        AutomaticExportErrorKey.SelectFolder -> R.string.auto_export_error_SelectFolder
        AutomaticExportErrorKey.EnterPassword -> R.string.auto_export_error_EnterPassword
        AutomaticExportErrorKey.InvalidFolder -> R.string.auto_export_error_InvalidFolder
        AutomaticExportErrorKey.PermissionDenied -> R.string.auto_export_error_PermissionDenied
        AutomaticExportErrorKey.WriteFailed -> R.string.auto_export_error_WriteFailed
        AutomaticExportErrorKey.Unknown -> R.string.auto_export_error_Unknown
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
