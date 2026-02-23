package de.t_animal.opensourcebodytracker.feature.measurements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.ui.components.DateInputField
import de.t_animal.opensourcebodytracker.ui.components.DecimalNumberInputField
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@Composable
fun MeasurementEditRoute(
    repository: MeasurementRepository,
    profileRepository: ProfileRepository,
    measurementId: Long?,
    onFinished: () -> Unit,
) {
    val vm: MeasurementEditViewModel = viewModel(
        factory = MeasurementEditViewModelFactory(
            repository = repository,
            profileRepository = profileRepository,
            measurementId = measurementId,
        ),
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

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
        onSaveClicked = vm::onSaveClicked,
    )
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
    onSaveClicked: () -> Unit,
) {
    val title = if (state.measurementId == null) "Add Measurement" else "Edit Measurement"

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(title) })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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

            DecimalNumberInputField(
                label = "Weight (kg)",
                value = state.weightKgText,
                onValueChange = onWeightChanged,
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Next,
            )
            Spacer(modifier = Modifier.height(8.dp))

            DecimalNumberInputField(
                label = "Neck (cm)",
                value = state.neckCmText,
                onValueChange = onNeckChanged,
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Next,
            )
            Spacer(modifier = Modifier.height(8.dp))

            DecimalNumberInputField(
                label = "Chest (cm)",
                value = state.chestCmText,
                onValueChange = onChestChanged,
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Next,
            )
            Spacer(modifier = Modifier.height(8.dp))

            DecimalNumberInputField(
                label = "Waist (cm)",
                value = state.waistCmText,
                onValueChange = onWaistChanged,
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Next,
            )
            Spacer(modifier = Modifier.height(8.dp))

            DecimalNumberInputField(
                label = "Abdomen (cm)",
                value = state.abdomenCmText,
                onValueChange = onAbdomenChanged,
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Next,
            )
            Spacer(modifier = Modifier.height(8.dp))

            DecimalNumberInputField(
                label = "Hip (cm)",
                value = state.hipCmText,
                onValueChange = onHipChanged,
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Next,
            )

            when (state.sex) {
                Sex.Male -> {
                    Spacer(modifier = Modifier.height(16.dp))

                    DecimalNumberInputField(
                        label = "Chest Skinfold (mm)",
                        value = state.chestSkinfoldMmText,
                        onValueChange = onChestSkinfoldChanged,
                        modifier = Modifier.fillMaxWidth(),
                        imeAction = ImeAction.Next,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DecimalNumberInputField(
                        label = "Abdomen Skinfold (mm)",
                        value = state.abdomenSkinfoldMmText,
                        onValueChange = onAbdomenSkinfoldChanged,
                        modifier = Modifier.fillMaxWidth(),
                        imeAction = ImeAction.Next,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DecimalNumberInputField(
                        label = "Thigh Skinfold (mm)",
                        value = state.thighSkinfoldMmText,
                        onValueChange = onThighSkinfoldChanged,
                        modifier = Modifier.fillMaxWidth(),
                        imeAction = ImeAction.Done,
                    )
                }

                Sex.Female -> {
                    Spacer(modifier = Modifier.height(16.dp))

                    DecimalNumberInputField(
                        label = "Triceps Skinfold (mm)",
                        value = state.tricepsSkinfoldMmText,
                        onValueChange = onTricepsSkinfoldChanged,
                        modifier = Modifier.fillMaxWidth(),
                        imeAction = ImeAction.Next,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DecimalNumberInputField(
                        label = "Suprailiac Skinfold (mm)",
                        value = state.suprailiacSkinfoldMmText,
                        onValueChange = onSuprailiacSkinfoldChanged,
                        modifier = Modifier.fillMaxWidth(),
                        imeAction = ImeAction.Next,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DecimalNumberInputField(
                        label = "Thigh Skinfold (mm)",
                        value = state.thighSkinfoldMmText,
                        onValueChange = onThighSkinfoldChanged,
                        modifier = Modifier.fillMaxWidth(),
                        imeAction = ImeAction.Done,
                    )
                }

                null -> Unit
            }

            Spacer(modifier = Modifier.height(16.dp))

            val error = state.errorMessage
            if (!error.isNullOrBlank()) {
                Text(text = error, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(onClick = onSaveClicked) {
                Text("Save")
            }
        }
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
            onSaveClicked = {},
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
            onSaveClicked = {},
        )
    }
}
