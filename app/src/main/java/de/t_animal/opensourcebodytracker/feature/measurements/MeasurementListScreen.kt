package de.t_animal.opensourcebodytracker.feature.measurements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme
import java.time.Instant
import java.time.ZoneId

@Composable
fun MeasurementListRoute(
    repository: MeasurementRepository,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val vm: MeasurementListViewModel = viewModel(factory = MeasurementListViewModelFactory(repository))
    val state by vm.uiState.collectAsStateWithLifecycle()

    MeasurementListScreen(
        state = state,
        onAdd = onAdd,
        onEdit = onEdit,
        onOpenSettings = onOpenSettings,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementListScreen(
    state: MeasurementListUiState,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onOpenSettings: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Measurements") },
                actions = {
                    TextButton(onClick = onOpenSettings) {
                        Text("Settings")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAdd) {
                Text("Add")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
        ) {
            items(items = state.measurements, key = { it.id }) { measurement ->
                MeasurementRow(
                    measurement = measurement,
                    onClick = { onEdit(measurement.id) },
                )
            }
        }
    }
}

@Composable
private fun MeasurementRow(
    measurement: BodyMeasurement,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        Text(
            text = formatDate(measurement.dateEpochMillis),
            style = MaterialTheme.typography.titleMedium,
        )

        val weight = measurement.weightKg
        if (weight != null) {
            Text(text = "Weight: $weight kg")
        }
    }
}

private fun formatDate(epochMillis: Long): String {
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .toString()
}

@Preview(showBackground = true)
@Composable
private fun MeasurementListScreenPreview() {
    BodyTrackerTheme {
        MeasurementListScreen(
            state = MeasurementListUiState(
                measurements = listOf(
                    BodyMeasurement(id = 1, dateEpochMillis = 1_700_000_000_000, weightKg = 80.0),
                    BodyMeasurement(id = 2, dateEpochMillis = 1_710_000_000_000, neckCircumferenceCm = 40.0),
                ),
            ),
            onAdd = {},
            onEdit = {},
            onOpenSettings = {},
        )
    }
}
