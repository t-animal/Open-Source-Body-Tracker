package de.t_animal.opensourcebodytracker.feature.measurements

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.domain.measurements.MeasurementValidationError
import de.t_animal.opensourcebodytracker.data.photos.NewPhotoCaptureTarget
import de.t_animal.opensourcebodytracker.feature.measurements.components.DeleteMeasurementDialog
import de.t_animal.opensourcebodytracker.feature.measurements.components.DiscardChangesDialog
import de.t_animal.opensourcebodytracker.feature.measurements.components.MeasurementEditFabColumn
import de.t_animal.opensourcebodytracker.feature.measurements.components.MetricSections
import de.t_animal.opensourcebodytracker.feature.measurements.components.PhotoPreviewCard
import de.t_animal.opensourcebodytracker.ui.components.PhotoPreviewDialog
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.resolveVisibleMeasuredMetrics
import de.t_animal.opensourcebodytracker.ui.components.DateInputField
import de.t_animal.opensourcebodytracker.ui.components.SecondaryScreenScaffold
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme
import java.io.File
import kotlinx.coroutines.launch

@Composable
fun MeasurementEditRoute(
    onFinished: () -> Unit,
    onCancel: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val vm: MeasurementEditViewModel = hiltViewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()
    var newPhotoCaptureTarget by remember { mutableStateOf<NewPhotoCaptureTarget?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { didCapture ->
        val capturedPhotoPath = newPhotoCaptureTarget?.let {
            if (didCapture) {
                it.absolutePath
            } else {
                it.file.delete()
                null
            }
        }
        vm.onPhotoCaptured(capturedPhotoPath)
        newPhotoCaptureTarget = null
    }

    val loadedState = state as? MeasurementEditUiState.Loaded
    val newPhotoTaken = loadedState?.pendingPhotoAbsolutePath != null
    val oldPhotoExistsAndNotDeleted =
        loadedState?.persistedPhotoFilePath != null && loadedState?.isPhotoMarkedForDeletion == false
    val photoPreviewModel: File? = when {
        newPhotoTaken -> {
            loadedState?.pendingPhotoAbsolutePath?.let(vm::resolveTemporaryCapturePhotoFile)
        }

        oldPhotoExistsAndNotDeleted -> {
            loadedState?.persistedPhotoFilePath?.let(vm::resolvePhotoFile)
        }

        else -> null
    }

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                MeasurementEditEvent.Saved,
                MeasurementEditEvent.Deleted -> {
                    vm.clearTemporaryCapturePhotos()
                    onFinished()
                }
            }
        }
    }

    MeasurementEditScreen(
        state = state,
        onDateChanged = vm::onDateChanged,
        onMetricChanged = vm::onMetricChanged,
        onNoteChanged = vm::onNoteChanged,
        photoPreviewModel = photoPreviewModel,
        onTakePhotoClicked = {
            val captureTarget = vm.createPhotoCaptureTarget()
            if (captureTarget != null) {
                newPhotoCaptureTarget = captureTarget
                takePictureLauncher.launch(captureTarget.uri)
            }
        },
        onDeletePhotoClicked = vm::onDeletePhotoClicked,
        onPhotoPreviewDialogVisibilityChanged = vm::onPhotoPreviewDialogVisibilityChanged,
        onDeleteMeasurementClicked = vm::onDeleteMeasurementClicked,
        onSaveClicked = vm::onSaveClicked,
        onBackClicked = {
            coroutineScope.launch {
                vm.clearTemporaryCapturePhotos()
                onCancel()
            }
        },
    )
}

@Composable
fun MeasurementEditScreen(
    state: MeasurementEditUiState,
    onDateChanged: (Long) -> Unit,
    onMetricChanged: (MeasuredBodyMetric, String) -> Unit,
    onNoteChanged: (String) -> Unit,
    photoPreviewModel: File?,
    onTakePhotoClicked: () -> Unit,
    onDeletePhotoClicked: () -> Unit,
    onPhotoPreviewDialogVisibilityChanged: (Boolean) -> Unit,
    onDeleteMeasurementClicked: () -> Unit,
    onSaveClicked: () -> Unit,
    onBackClicked: () -> Unit,
) {
    if (state is MeasurementEditUiState.Loading) {
        SecondaryScreenScaffold(
            title = stringResource(R.string.common_loading),
            onNavigateBack = onBackClicked,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }

    val loadedState = state as MeasurementEditUiState.Loaded
    MeasurementEditLoadedScreen(
        state = loadedState,
        onDateChanged = onDateChanged,
        onMetricChanged = onMetricChanged,
        onNoteChanged = onNoteChanged,
        photoPreviewModel = photoPreviewModel,
        onTakePhotoClicked = onTakePhotoClicked,
        onDeletePhotoClicked = onDeletePhotoClicked,
        onPhotoPreviewDialogVisibilityChanged = onPhotoPreviewDialogVisibilityChanged,
        onDeleteMeasurementClicked = onDeleteMeasurementClicked,
        onSaveClicked = onSaveClicked,
        onBackClicked = onBackClicked,
    )
}

@Composable
private fun MeasurementEditLoadedScreen(
    state: MeasurementEditUiState.Loaded,
    onDateChanged: (Long) -> Unit,
    onMetricChanged: (MeasuredBodyMetric, String) -> Unit,
    onNoteChanged: (String) -> Unit,
    photoPreviewModel: File?,
    onTakePhotoClicked: () -> Unit,
    onDeletePhotoClicked: () -> Unit,
    onPhotoPreviewDialogVisibilityChanged: (Boolean) -> Unit,
    onDeleteMeasurementClicked: () -> Unit,
    onSaveClicked: () -> Unit,
    onBackClicked: () -> Unit,
) {
    val enabledMeasurements = state.enabledMeasurements
    val isCreatingNew = state.measurementId == null
    val title = stringResource(if (isCreatingNew) R.string.measurement_edit_title_add else R.string.measurement_edit_title_edit)
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDeleteMeasurementDialog by remember { mutableStateOf(false) }
    val visibleMetrics = remember(enabledMeasurements, state.sex) {
        resolveVisibleMeasuredMetrics(
            enabledMeasurements = enabledMeasurements,
            sex = state.sex,
        )
    }

    val hasEnteredAnyInput =
        state.bodyMetricInputs.values.any { it.isNotBlank() } ||
            state.note.isNotBlank() ||
            photoPreviewModel != null
    val hasUnsavedInput = if (isCreatingNew) hasEnteredAnyInput else state.hasUnsavedChanges

    val handleBackClick = {
        if (hasUnsavedInput) {
            showDiscardDialog = true
        } else {
            onBackClicked()
        }
    }

    val isOverlayVisible =
        state.isPhotoPreviewDialogVisible || showDiscardDialog || showDeleteMeasurementDialog
    BackHandler(enabled = !isOverlayVisible) {
        handleBackClick()
    }

    SecondaryScreenScaffold(
        title = title,
        onNavigateBack = handleBackClick,
        floatingActionButton = {
            MeasurementEditFabColumn(
                isCreatingNew = isCreatingNew,
                hasPhoto = photoPreviewModel != null,
                onTakePhotoClicked = onTakePhotoClicked,
                onDeletePhotoClicked = onDeletePhotoClicked,
                onDeleteMeasurementClicked = { showDeleteMeasurementDialog = true },
                onSaveClicked = onSaveClicked,
            )
        },
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(16.dp),
        ) {
            DateInputField(
                label = stringResource(R.string.measurement_edit_label_date),
                valueText = state.dateText,
                selectedDateMillis = state.dateEpochMillis,
                onDateSelected = onDateChanged,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            MetricSections(
                metrics = visibleMetrics,
                bodyMetricInputs = state.bodyMetricInputs,
                onMetricChanged = onMetricChanged,
                unitSystem = state.unitSystem,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChanged,
                label = { Text(stringResource(R.string.measurement_edit_label_notes)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 8,
            )

            if (photoPreviewModel != null) {
                Spacer(modifier = Modifier.height(16.dp))
                PhotoPreviewCard(
                    photoPreviewModel = photoPreviewModel,
                    onClick = { onPhotoPreviewDialogVisibilityChanged(true) },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val errorMessage = when (val error = state.error) {
                is MeasurementEditError.Validation -> when (error.error) {
                    MeasurementValidationError.InvalidSkinfoldValues -> stringResource(R.string.validation_skinfold_must_be_positive)
                    MeasurementValidationError.InvalidBodyFat -> stringResource(R.string.validation_body_fat_range)
                    MeasurementValidationError.MissingInput -> stringResource(R.string.validation_missing_input)
                }
                MeasurementEditError.DeleteFailed -> stringResource(R.string.measurement_error_delete_failed)
                MeasurementEditError.SavePhotoFailed -> stringResource(R.string.measurement_error_save_photo_failed)
                null -> null
            }
            if (!errorMessage.isNullOrBlank()) {
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.measurement_edit_hint_before_bold))
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.measurement_edit_hint_bold))
                    }
                    append(stringResource(R.string.measurement_edit_hint_after_bold))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 88.dp)
                    .heightIn(min = if (photoPreviewModel == null) 100.dp else 0.dp),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Light),
            )
        }
    }

    PhotoPreviewDialog(
        isVisible = state.isPhotoPreviewDialogVisible,
        photoPreviewModel = photoPreviewModel,
        onDismiss = { onPhotoPreviewDialogVisibilityChanged(false) },
    )

    if (showDiscardDialog) {
        DiscardChangesDialog(
            onDismiss = { showDiscardDialog = false },
            onDiscard = {
                showDiscardDialog = false
                onBackClicked()
            },
        )
    }

    if (showDeleteMeasurementDialog) {
        DeleteMeasurementDialog(
            onDismiss = { showDeleteMeasurementDialog = false },
            onDelete = {
                showDeleteMeasurementDialog = false
                onDeleteMeasurementClicked()
            },
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun MeasurementEditScreenPreview_Add() {
    BodyTrackerTheme {
        MeasurementEditScreen(
            state = MeasurementEditUiState.Loaded(
                measurementId = null,
                sex = Sex.Male,
                enabledMeasurements = MeasuredBodyMetric.entries.toSet(),
                dateEpochMillis = 1_700_000_000_000,
                dateText = "2024-01-01",
            ),
            onDateChanged = {},
            onMetricChanged = { _, _ -> },
            onNoteChanged = {},
            photoPreviewModel = null,
            onTakePhotoClicked = {},
            onDeletePhotoClicked = {},
            onPhotoPreviewDialogVisibilityChanged = {},
            onDeleteMeasurementClicked = {},
            onSaveClicked = {},
            onBackClicked = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MeasurementEditScreenPreview_Error() {
    BodyTrackerTheme {
        MeasurementEditScreen(
            state = MeasurementEditUiState.Loaded(
                measurementId = null,
                sex = Sex.Male,
                enabledMeasurements = MeasuredBodyMetric.entries.toSet(),
                dateEpochMillis = 1_700_000_000_000,
                dateText = "2024-01-01",
                error = MeasurementEditError.Validation(MeasurementValidationError.MissingInput),
            ),
            onDateChanged = {},
            onMetricChanged = { _, _ -> },
            onNoteChanged = {},
            photoPreviewModel = null,
            onTakePhotoClicked = {},
            onDeletePhotoClicked = {},
            onPhotoPreviewDialogVisibilityChanged = {},
            onDeleteMeasurementClicked = {},
            onSaveClicked = {},
            onBackClicked = {},
        )
    }
}
