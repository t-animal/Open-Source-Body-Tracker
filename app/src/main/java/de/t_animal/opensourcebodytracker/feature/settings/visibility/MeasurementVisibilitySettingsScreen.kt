package de.t_animal.opensourcebodytracker.feature.settings.visibility

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.DerivedBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MeasurementSettings
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@Composable
fun MeasurementVisibilitySettingsRoute(
    onNavigateBack: () -> Unit,
) {
    val vm: MeasurementVisibilitySettingsViewModel = hiltViewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    MeasurementVisibilitySettingsScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onMeasurementVisibilityChanged = vm::onDisplayPlacementChanged
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementVisibilitySettingsScreen(
    state: SettingsUiState,
    onNavigateBack: () -> Unit,
    onMeasurementVisibilityChanged: (BodyMetric, DisplayPlacement) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Measurement Visibility") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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

        val recordedMeasurements = BodyMetric.entries.filter {
            it in state.settings.enabledMeasurements ||
            it in state.settings.enabledDerivedMetrics
        }
        val notRecordedMeasurements = BodyMetric.entries.filter { it !in recordedMeasurements }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Choose which recorded measurements are visible in tables and analysis views.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }

            item {
                DisplayConfigurationSection(
                    state = state,
                    metricTypes = recordedMeasurements,
                    onDisplayPlacementChanged = onMeasurementVisibilityChanged,
                )
            }

            if (notRecordedMeasurements.isNotEmpty()) {
                item {
                    Text(
                        text = "These measurements are currently disabled. It might still make sense to display them, e.g. if you have recorded them in the past.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }

                item {
                    DisplayConfigurationSection(
                        state = state,
                        metricTypes = notRecordedMeasurements,
                        onDisplayPlacementChanged = onMeasurementVisibilityChanged,
                    )
                }
            }

            if (!state.errorMessage.isNullOrBlank()) {
                item {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}


@Composable
private fun DisplayConfigurationSection(
    state: SettingsUiState,
    metricTypes: List<BodyMetric>,
    onDisplayPlacementChanged: (BodyMetric, DisplayPlacement) -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        metricTypes.forEach { metric ->
            DisplayPlacementRow(
                label = metric.label(),
                placement = when {
                    metric in state.settings.visibleInAnalysis && metric in state.settings.visibleInTable ->
                        DisplayPlacement.InBoth

                    metric in state.settings.visibleInAnalysis -> DisplayPlacement.OnlyInAnalysis
                    metric in state.settings.visibleInTable -> DisplayPlacement.OnlyInTable
                    else -> DisplayPlacement.Hidden
                },
                onPlacementSelected = { onDisplayPlacementChanged(metric, it) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisplayPlacementRow(
    label: String,
    placement: DisplayPlacement,
    onPlacementSelected: (DisplayPlacement) -> Unit,
) {
    var expanded by remember(placement, label) { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                state = TextFieldState(placement.label()),
                readOnly = true,
                label = { Text("Visibility") },
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .widthIn(max = 180.dp)
                    .heightIn(max = 45.dp)
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DisplayPlacement.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label()) },
                        onClick = {
                            expanded = false
                            onPlacementSelected(option)
                        },
                    )
                }
            }
        }
    }
}

private fun DisplayPlacement.label(): String = when (this) {
    DisplayPlacement.InBoth -> "In both"
    DisplayPlacement.OnlyInTable -> "Only in Table"
    DisplayPlacement.OnlyInAnalysis -> "Only in Analysis"
    DisplayPlacement.Hidden -> "Hidden"
}

private fun BodyMetric.label(): String = when (this) {
    is MeasuredBodyMetric -> when (this) {
        MeasuredBodyMetric.Weight -> "Weight"
        MeasuredBodyMetric.BodyFat -> "Body Fat"
        MeasuredBodyMetric.NeckCircumference -> "Neck"
        MeasuredBodyMetric.WaistCircumference -> "Waist"
        MeasuredBodyMetric.HipCircumference -> "Hip"
        MeasuredBodyMetric.ChestCircumference -> "Chest"
        MeasuredBodyMetric.AbdomenCircumference -> "Abdomen"
        MeasuredBodyMetric.ChestSkinfold -> "Chest Skinfold"
        MeasuredBodyMetric.AbdomenSkinfold -> "Abdomen Skinfold"
        MeasuredBodyMetric.ThighSkinfold -> "Thigh Skinfold"
        MeasuredBodyMetric.TricepsSkinfold -> "Triceps Skinfold"
        MeasuredBodyMetric.SuprailiacSkinfold -> "Suprailiac Skinfold"
    }

    is DerivedBodyMetric -> when (this) {
        DerivedBodyMetric.Bmi -> "BMI"
        DerivedBodyMetric.NavyBodyFatPercent -> "Navy Body Fat %"
        DerivedBodyMetric.SkinfoldBodyFatPercent -> "Skinfold Body Fat %"
        DerivedBodyMetric.WaistHipRatio -> "Waist–Hip Ratio"
        DerivedBodyMetric.WaistHeightRatio -> "Waist–Height Ratio"
    }

    else -> id
}


@Preview(showBackground = true)
@Composable
private fun MeasurementVisibilitySettingsScreenPreview() {
    val settings = MeasurementSettings(
        enabledMeasurements = MeasuredBodyMetric.entries.toSet() - setOf(
            MeasuredBodyMetric.ChestSkinfold,
            MeasuredBodyMetric.AbdomenSkinfold,
        ),
        visibleInAnalysis = MeasurementSettings().visibleInAnalysis - MeasuredBodyMetric.BodyFat,
    )

    BodyTrackerTheme {
        MeasurementVisibilitySettingsScreen(
            state = SettingsUiState(
                isLoading = false,
                settings = settings,
            ),
            onNavigateBack = {},
            onMeasurementVisibilityChanged = { _, _ -> },
        )
    }
}
