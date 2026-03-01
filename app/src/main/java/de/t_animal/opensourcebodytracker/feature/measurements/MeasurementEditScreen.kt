package de.t_animal.opensourcebodytracker.feature.measurements

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.metrics.DerivedMetricsDependencyResolver
import de.t_animal.opensourcebodytracker.ui.components.DateInputField
import de.t_animal.opensourcebodytracker.ui.components.DecimalNumberInputField
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme
import java.io.File

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
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { didCapture ->
        val captureUri = pendingCaptureUri
        val capturedPhoto = if (didCapture && captureUri != null) {
            decodeBitmapFromUri(context, captureUri)
        } else {
            null
        }
        vm.onPhotoCaptured(capturedPhoto)
        pendingCaptureUri = null
    }

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                MeasurementEditEvent.Saved -> onFinished()
            }
        }
    }

    MeasurementEditScreen(
        state = state,
        onDateChanged = vm::onDateChanged,
        onWeightChanged = vm::onWeightChanged,
        onNeckChanged = vm::onNeckChanged,
        onChestChanged = vm::onChestChanged,
        onWaistChanged = vm::onWaistChanged,
        onAbdomenChanged = vm::onAbdomenChanged,
        onHipChanged = vm::onHipChanged,
        onChestSkinfoldChanged = vm::onChestSkinfoldChanged,
        onAbdomenSkinfoldChanged = vm::onAbdomenSkinfoldChanged,
        onThighSkinfoldChanged = vm::onThighSkinfoldChanged,
        onTricepsSkinfoldChanged = vm::onTricepsSkinfoldChanged,
        onSuprailiacSkinfoldChanged = vm::onSuprailiacSkinfoldChanged,
        onTakePhotoClicked = {
            val imageUri = createTemporaryImageUri(context)
            if (imageUri != null) {
                pendingCaptureUri = imageUri
                takePictureLauncher.launch(imageUri)
            }
        },
        onDeletePhotoClicked = vm::onDeletePhotoClicked,
        onPhotoPreviewDialogVisibilityChanged = vm::onPhotoPreviewDialogVisibilityChanged,
        onSaveClicked = vm::onSaveClicked,
        onBackClicked = onCancel,
    )
}

private fun createTemporaryImageUri(context: Context): Uri? {
    val cameraDir = File(context.cacheDir, "images").apply { mkdirs() }
    val imageFile = runCatching {
        File.createTempFile("capture_", ".jpg", cameraDir)
    }.getOrNull() ?: return null

    return runCatching {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile,
        )
    }.getOrNull()
}

private fun decodeBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    }.getOrNull()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementEditScreen(
    state: MeasurementEditUiState,
    onDateChanged: (Long) -> Unit,
    onWeightChanged: (String) -> Unit,
    onNeckChanged: (String) -> Unit,
    onChestChanged: (String) -> Unit,
    onWaistChanged: (String) -> Unit,
    onAbdomenChanged: (String) -> Unit,
    onHipChanged: (String) -> Unit,
    onChestSkinfoldChanged: (String) -> Unit,
    onAbdomenSkinfoldChanged: (String) -> Unit,
    onThighSkinfoldChanged: (String) -> Unit,
    onTricepsSkinfoldChanged: (String) -> Unit,
    onSuprailiacSkinfoldChanged: (String) -> Unit,
    onTakePhotoClicked: () -> Unit,
    onDeletePhotoClicked: () -> Unit,
    onPhotoPreviewDialogVisibilityChanged: (Boolean) -> Unit,
    onSaveClicked: () -> Unit,
    onBackClicked: () -> Unit,
) {
    val enabledMeasurements = state.enabledMeasurements
    val title = if (state.measurementId == null) "Add Measurement" else "Edit Measurement"
    var showDiscardDialog by remember { mutableStateOf(false) }

    val hasEnteredAnyInput = listOf(
        state.weightKgText,
        state.neckCmText,
        state.chestCmText,
        state.waistCmText,
        state.abdomenCmText,
        state.hipCmText,
        state.chestSkinfoldMmText,
        state.abdomenSkinfoldMmText,
        state.thighSkinfoldMmText,
        state.tricepsSkinfoldMmText,
        state.suprailiacSkinfoldMmText,
    ).any { it.isNotBlank() } || state.photoBinaryContent != null

    val handleBackClick = {
        if (hasEnteredAnyInput) {
            showDiscardDialog = true
        } else {
            onBackClicked()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = handleBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            Column(modifier = Modifier.imePadding()) {
                val hasPhoto = state.photoBinaryContent != null
                FloatingActionButton(
                    onClick = if (hasPhoto) onDeletePhotoClicked else onTakePhotoClicked,
                ) {
                    Icon(
                        imageVector = if (hasPhoto) Icons.Filled.Delete else Icons.Filled.CameraAlt,
                        contentDescription = if (hasPhoto) "Delete photo" else "Take photo",
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                ExtendedFloatingActionButton(onClick = onSaveClicked) {
                    Text("Save")
                }
            }
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

            MetricInputField(
                isVisible = MeasuredBodyMetric.Weight in enabledMeasurements,
                label = "Weight (kg)",
                value = state.weightKgText,
                onValueChange = onWeightChanged,
                imeAction = ImeAction.Next,
            )

            MetricInputField(
                isVisible = MeasuredBodyMetric.NeckCircumference in enabledMeasurements,
                label = "Neck (cm)",
                value = state.neckCmText,
                onValueChange = onNeckChanged,
                imeAction = ImeAction.Next,
            )

            MetricInputField(
                isVisible = MeasuredBodyMetric.ChestCircumference in enabledMeasurements,
                label = "Chest (cm)",
                value = state.chestCmText,
                onValueChange = onChestChanged,
                imeAction = ImeAction.Next,
            )

            MetricInputField(
                isVisible = MeasuredBodyMetric.WaistCircumference in enabledMeasurements,
                label = "Waist (cm)",
                value = state.waistCmText,
                onValueChange = onWaistChanged,
                imeAction = ImeAction.Next,
            )

            MetricInputField(
                isVisible = MeasuredBodyMetric.AbdomenCircumference in enabledMeasurements,
                label = "Abdomen (cm)",
                value = state.abdomenCmText,
                onValueChange = onAbdomenChanged,
                imeAction = ImeAction.Next,
            )

            MetricInputField(
                isVisible = MeasuredBodyMetric.HipCircumference in enabledMeasurements,
                label = "Hip (cm)",
                value = state.hipCmText,
                onValueChange = onHipChanged,
                imeAction = ImeAction.Next,
                addBottomSpacing = false,
            )

            when (state.sex) {
                Sex.Male -> {
                    val maleSkinfoldsVisible = MeasuredBodyMetric.ChestSkinfold in enabledMeasurements ||
                        MeasuredBodyMetric.AbdomenSkinfold in enabledMeasurements ||
                        MeasuredBodyMetric.ThighSkinfold in enabledMeasurements
                    if (maleSkinfoldsVisible) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    MetricInputField(
                        isVisible = MeasuredBodyMetric.ChestSkinfold in enabledMeasurements,
                        label = "Chest Skinfold (mm)",
                        value = state.chestSkinfoldMmText,
                        onValueChange = onChestSkinfoldChanged,
                        imeAction = ImeAction.Next,
                    )

                    MetricInputField(
                        isVisible = MeasuredBodyMetric.AbdomenSkinfold in enabledMeasurements,
                        label = "Abdomen Skinfold (mm)",
                        value = state.abdomenSkinfoldMmText,
                        onValueChange = onAbdomenSkinfoldChanged,
                        imeAction = ImeAction.Next,
                    )

                    MetricInputField(
                        isVisible = MeasuredBodyMetric.ThighSkinfold in enabledMeasurements,
                        label = "Thigh Skinfold (mm)",
                        value = state.thighSkinfoldMmText,
                        onValueChange = onThighSkinfoldChanged,
                        imeAction = ImeAction.Done,
                        addBottomSpacing = false,
                    )
                }

                Sex.Female -> {
                    val femaleSkinfoldsVisible = MeasuredBodyMetric.TricepsSkinfold in enabledMeasurements ||
                        MeasuredBodyMetric.SuprailiacSkinfold in enabledMeasurements ||
                        MeasuredBodyMetric.ThighSkinfold in enabledMeasurements
                    if (femaleSkinfoldsVisible) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    MetricInputField(
                        isVisible = MeasuredBodyMetric.TricepsSkinfold in enabledMeasurements,
                        label = "Triceps Skinfold (mm)",
                        value = state.tricepsSkinfoldMmText,
                        onValueChange = onTricepsSkinfoldChanged,
                        imeAction = ImeAction.Next,
                    )

                    MetricInputField(
                        isVisible = MeasuredBodyMetric.SuprailiacSkinfold in enabledMeasurements,
                        label = "Suprailiac Skinfold (mm)",
                        value = state.suprailiacSkinfoldMmText,
                        onValueChange = onSuprailiacSkinfoldChanged,
                        imeAction = ImeAction.Next,
                    )

                    MetricInputField(
                        isVisible = MeasuredBodyMetric.ThighSkinfold in enabledMeasurements,
                        label = "Thigh Skinfold (mm)",
                        value = state.thighSkinfoldMmText,
                        onValueChange = onThighSkinfoldChanged,
                        imeAction = ImeAction.Done,
                        addBottomSpacing = false,
                    )
                }

                null -> Unit
            }

            state.photoBinaryContent?.let { capturedPhoto ->
                Spacer(modifier = Modifier.height(16.dp))
                val previewShape = MaterialTheme.shapes.medium

                Image(
                    bitmap = capturedPhoto.asImageBitmap(),
                    contentDescription = "Captured photo preview",
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .heightIn(max = 500.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(previewShape)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = previewShape,
                        )
                        .clickable { onPhotoPreviewDialogVisibilityChanged(true) },
                    contentScale = ContentScale.Crop,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val error = state.errorMessage
            if (!error.isNullOrBlank()) {
                Text(text = error, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (state.photoBinaryContent == null) {
                Spacer(modifier = Modifier.height(150.dp))
            }
        }
    }

    if (state.isPhotoPreviewDialogVisible) {
        val previewPhoto = state.photoBinaryContent
        if (previewPhoto != null) {
            Dialog(
                onDismissRequest = { onPhotoPreviewDialogVisibilityChanged(false) },
                properties = DialogProperties(usePlatformDefaultWidth = false),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                ) {
                    Image(
                        bitmap = previewPhoto.asImageBitmap(),
                        contentDescription = "Captured photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentScale = ContentScale.Fit,
                    )

                    IconButton(
                        onClick = { onPhotoPreviewDialogVisibilityChanged(false) },
                        modifier = Modifier.align(Alignment.TopEnd),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                        )
                    }
                }
            }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard input?") },
            text = { Text("You have entered values. Discard them and go back?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onBackClicked()
                    },
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep editing")
                }
            },
        )
    }
}

@Composable
private fun MetricInputField(
    isVisible: Boolean,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    imeAction: ImeAction,
    addBottomSpacing: Boolean = true,
) {
    if (!isVisible) {
        return
    }

    DecimalNumberInputField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        imeAction = imeAction,
    )

    if (addBottomSpacing) {
        Spacer(modifier = Modifier.height(8.dp))
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
            onWeightChanged = {},
            onNeckChanged = {},
            onChestChanged = {},
            onWaistChanged = {},
            onAbdomenChanged = {},
            onHipChanged = {},
            onChestSkinfoldChanged = {},
            onAbdomenSkinfoldChanged = {},
            onThighSkinfoldChanged = {},
            onTricepsSkinfoldChanged = {},
            onSuprailiacSkinfoldChanged = {},
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
            onWeightChanged = {},
            onNeckChanged = {},
            onChestChanged = {},
            onWaistChanged = {},
            onAbdomenChanged = {},
            onHipChanged = {},
            onChestSkinfoldChanged = {},
            onAbdomenSkinfoldChanged = {},
            onThighSkinfoldChanged = {},
            onTricepsSkinfoldChanged = {},
            onSuprailiacSkinfoldChanged = {},
            onTakePhotoClicked = {},
            onDeletePhotoClicked = {},
            onPhotoPreviewDialogVisibilityChanged = {},
            onSaveClicked = {},
            onBackClicked = {},
        )
    }
}
