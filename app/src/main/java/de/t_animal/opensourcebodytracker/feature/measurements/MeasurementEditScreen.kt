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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@Composable
fun MeasurementEditRoute(
    repository: MeasurementRepository,
    measurementId: Long?,
    onFinished: () -> Unit,
) {
    val vm: MeasurementEditViewModel = viewModel(
        factory = MeasurementEditViewModelFactory(repository = repository, measurementId = measurementId),
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
        onWeightChanged = vm::onWeightChanged,
        onNeckChanged = vm::onNeckChanged,
        onChestChanged = vm::onChestChanged,
        onWaistChanged = vm::onWaistChanged,
        onAbdomenChanged = vm::onAbdomenChanged,
        onHipChanged = vm::onHipChanged,
        onSaveClicked = vm::onSaveClicked,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementEditScreen(
    state: MeasurementEditUiState,
    onWeightChanged: (String) -> Unit,
    onNeckChanged: (String) -> Unit,
    onChestChanged: (String) -> Unit,
    onWaistChanged: (String) -> Unit,
    onAbdomenChanged: (String) -> Unit,
    onHipChanged: (String) -> Unit,
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
            Text(text = "Date: ${state.dateText}")
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.weightKgText,
                onValueChange = onWeightChanged,
                label = { Text("Weight (kg)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.neckCmText,
                onValueChange = onNeckChanged,
                label = { Text("Neck (cm)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.chestCmText,
                onValueChange = onChestChanged,
                label = { Text("Chest (cm)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.waistCmText,
                onValueChange = onWaistChanged,
                label = { Text("Waist (cm)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.abdomenCmText,
                onValueChange = onAbdomenChanged,
                label = { Text("Abdomen (cm)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.hipCmText,
                onValueChange = onHipChanged,
                label = { Text("Hip (cm)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

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
            onWeightChanged = {},
            onNeckChanged = {},
            onChestChanged = {},
            onWaistChanged = {},
            onAbdomenChanged = {},
            onHipChanged = {},
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
            onWeightChanged = {},
            onNeckChanged = {},
            onChestChanged = {},
            onWaistChanged = {},
            onAbdomenChanged = {},
            onHipChanged = {},
            onSaveClicked = {},
        )
    }
}
