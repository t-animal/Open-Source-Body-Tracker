package de.t_animal.opensourcebodytracker.feature.measurements

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.t_animal.opensourcebodytracker.core.model.BodyMetricType
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.metrics.DerivedMetricsDependencyResolver
import de.t_animal.opensourcebodytracker.feature.measurements.components.DiscardChangesDialog
import de.t_animal.opensourcebodytracker.feature.measurements.components.MeasurementEditFabColumn
import de.t_animal.opensourcebodytracker.feature.measurements.components.MeasurementEditTopBar
import de.t_animal.opensourcebodytracker.feature.measurements.components.MetricSections
import de.t_animal.opensourcebodytracker.feature.measurements.components.PhotoPreviewCard
import de.t_animal.opensourcebodytracker.feature.measurements.components.PhotoPreviewDialog
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.PendingCaptureTarget
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.createTemporaryImageUri
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.resolveVisibleMeasuredMetrics
import de.t_animal.opensourcebodytracker.ui.components.DateInputField
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme
import java.io.File
import kotlinx.coroutines.launch

@Composable
fun MeasurementEditRoute(
    repository: MeasurementRepository,
    photoStorage: InternalPhotoStorage,
    profileRepository: ProfileRepository,
    settingsRepository: SettingsRepository,
    measurementId: Long?,
    onFinished: () -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val vm: MeasurementEditViewModel = viewModel(
        factory = MeasurementEditViewModelFactory(
            repository = repository,
            photoStorage = photoStorage,
            profileRepository = profileRepository,
            settingsRepository = settingsRepository,
            dependencyResolver = DerivedMetricsDependencyResolver(),
            measurementId = measurementId,
        ),
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    var pendingCaptureTarget by remember { mutableStateOf<PendingCaptureTarget?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { didCapture ->
        val capturedPhotoPath = pendingCaptureTarget?.let {
            if (didCapture) {
                it.file.absolutePath
            } else {
                it.file.delete()
                null
            }
        }
        vm.onPhotoCaptured(capturedPhotoPath)
        pendingCaptureTarget = null
    }

    val newPhotoTaken =!state.pendingPhotoAbsolutePath.isNullOrBlank()
    val oldPhotoExistsAndNotDeleted = !state.persistedPhotoFilePath.isNullOrBlank() && !state.isPhotoMarkedForDeletion
    val photoPreviewModel: File? = when {
         newPhotoTaken-> {
            state.pendingPhotoAbsolutePath?.let(::File)
        }
         oldPhotoExistsAndNotDeleted -> {
            state.persistedPhotoFilePath?.let(photoStorage::resolvePhotoFile)
        }

        else -> null
    }

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                MeasurementEditEvent.Saved -> {
                    photoStorage.clearTemporaryCapturePhotos()
                    onFinished()
                }
            }
        }
    }

    MeasurementEditScreen(
        state = state,
        onDateChanged = vm::onDateChanged,
        onMetricChanged = vm::onMetricChanged,
        photoPreviewModel = photoPreviewModel,
        onTakePhotoClicked = {
            val captureTarget = createTemporaryImageUri(context)
            if (captureTarget != null) {
                pendingCaptureTarget = captureTarget
                takePictureLauncher.launch(captureTarget.uri)
            }
        },
        onDeletePhotoClicked = vm::onDeletePhotoClicked,
        onPhotoPreviewDialogVisibilityChanged = vm::onPhotoPreviewDialogVisibilityChanged,
        onSaveClicked = vm::onSaveClicked,
        onBackClicked = {
            coroutineScope.launch {
                photoStorage.clearTemporaryCapturePhotos()
                onCancel()
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementEditScreen(
    state: MeasurementEditUiState,
    onDateChanged: (Long) -> Unit,
    onMetricChanged: (MeasuredBodyMetric, String) -> Unit,
    photoPreviewModel: File?,
    onTakePhotoClicked: () -> Unit,
    onDeletePhotoClicked: () -> Unit,
    onPhotoPreviewDialogVisibilityChanged: (Boolean) -> Unit,
    onSaveClicked: () -> Unit,
    onBackClicked: () -> Unit,
) {
    val enabledMeasurements = state.enabledMeasurements
    val title = if (state.measurementId == null) "Add Measurement" else "Edit Measurement"
    var showDiscardDialog by remember { mutableStateOf(false) }
    val visibleMetrics = remember(enabledMeasurements, state.sex) {
        resolveVisibleMeasuredMetrics(
            enabledMeasurements = enabledMeasurements,
            sex = state.sex,
        )
    }

    val hasEnteredAnyInput = state.metricInputs.values.any { it.isNotBlank() } || photoPreviewModel != null

    val handleBackClick = {
        if (hasEnteredAnyInput) {
            showDiscardDialog = true
        } else {
            onBackClicked()
        }
    }

    Scaffold(
        topBar = {
            MeasurementEditTopBar(
                title = title,
                onBackClicked = handleBackClick,
            )
        },
        floatingActionButton = {
            MeasurementEditFabColumn(
                hasPhoto = photoPreviewModel != null,
                onTakePhotoClicked = onTakePhotoClicked,
                onDeletePhotoClicked = onDeletePhotoClicked,
                onSaveClicked = onSaveClicked,
            )
        },
    ) { padding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(16.dp),
        ) {
            DateInputField(
                label = "Date",
                valueText = state.dateText,
                selectedDateMillis = state.dateEpochMillis,
                onDateSelected = onDateChanged,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            MetricSections(
                metrics = visibleMetrics,
                metricInputs = state.metricInputs,
                onMetricChanged = onMetricChanged,
            )

            if (photoPreviewModel != null) {
                Spacer(modifier = Modifier.height(16.dp))
                PhotoPreviewCard(
                    photoPreviewModel = photoPreviewModel,
                    onClick = { onPhotoPreviewDialogVisibilityChanged(true) },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val error = state.errorMessage
            if (!error.isNullOrBlank()) {
                Text(text = error, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (photoPreviewModel == null) {
                Spacer(modifier = Modifier.height(150.dp))
            }
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
}


@Preview(showBackground = true)
@Composable
private fun MeasurementEditScreenPreview_Add() {
    BodyTrackerTheme {
        MeasurementEditScreen(
            state = MeasurementEditUiState(
                measurementId = null,
                dateEpochMillis = 1_700_000_000_000,
                dateText = "2024-01-01",
            ),
            onDateChanged = {},
            onMetricChanged = { _, _ -> },
            photoPreviewModel = null,
            onTakePhotoClicked = {},
            onDeletePhotoClicked = {},
            onPhotoPreviewDialogVisibilityChanged = {},
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
            state = MeasurementEditUiState(
                measurementId = null,
                dateEpochMillis = 1_700_000_000_000,
                dateText = "2024-01-01",
                errorMessage = "Enter at least one value",
            ),
            onDateChanged = {},
            onMetricChanged = { _, _ -> },
            photoPreviewModel = null,
            onTakePhotoClicked = {},
            onDeletePhotoClicked = {},
            onPhotoPreviewDialogVisibilityChanged = {},
            onSaveClicked = {},
            onBackClicked = {},
        )
    }
}
