package de.t_animal.opensourcebodytracker.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.settings.SettingsDependencyResolver
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@Composable
fun SettingsRoute(
    settingsRepository: SettingsRepository,
    profileRepository: ProfileRepository,
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues,
) {
    val vm: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            settingsRepository = settingsRepository,
            profileRepository = profileRepository,
            dependencyResolver = SettingsDependencyResolver(),
        ),
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        contentPadding = contentPadding,
        onNavyBodyFatEnabledChanged = vm::onNavyBodyFatEnabledChanged,
        onSkinfoldBodyFatEnabledChanged = vm::onSkinfoldBodyFatEnabledChanged,
        onMeasurementEnabledChanged = vm::onMeasurementEnabledChanged,
        onDisplayPlacementChanged = vm::onDisplayPlacementChanged,
    )
}

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues,
    onNavyBodyFatEnabledChanged: (Boolean) -> Unit,
    onSkinfoldBodyFatEnabledChanged: (Boolean) -> Unit,
    onMeasurementEnabledChanged: (BodyMetric, Boolean) -> Unit,
    onDisplayPlacementChanged: (BodyMetric, DisplayPlacement) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }

        item {
            AnalysisMethodsSection(
                navyBodyFatEnabled = state.settings.navyBodyFatEnabled,
                skinfoldBodyFatEnabled = state.settings.skinfoldBodyFatEnabled,
                onNavyBodyFatEnabledChanged = onNavyBodyFatEnabledChanged,
                onSkinfoldBodyFatEnabledChanged = onSkinfoldBodyFatEnabledChanged,
            )
        }

        item {
            MeasurementCollectionSection(
                enabledMeasurements = state.settings.enabledMeasurements,
                requiredMeasurements = state.requiredMeasurements,
                onMeasurementEnabledChanged = onMeasurementEnabledChanged,
            )
        }

        item {
            DisplayConfigurationSection(
                state = state,
                onDisplayPlacementChanged = onDisplayPlacementChanged,
            )
        }

        if (!state.errorMessage.isNullOrBlank()) {
            item {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun AnalysisMethodsSection(
    navyBodyFatEnabled: Boolean,
    skinfoldBodyFatEnabled: Boolean,
    onNavyBodyFatEnabledChanged: (Boolean) -> Unit,
    onSkinfoldBodyFatEnabledChanged: (Boolean) -> Unit,
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Analysis Methods", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            CheckRow(
                label = "Navy Body Fat %",
                checked = navyBodyFatEnabled,
                enabled = true,
                onCheckedChange = onNavyBodyFatEnabledChanged,
            )
            CheckRow(
                label = "Skinfold Body Fat %",
                checked = skinfoldBodyFatEnabled,
                enabled = true,
                onCheckedChange = onSkinfoldBodyFatEnabledChanged,
            )
        }
    }
}

@Composable
private fun MeasurementCollectionSection(
    enabledMeasurements: Set<BodyMetric>,
    requiredMeasurements: Set<BodyMetric>,
    onMeasurementEnabledChanged: (BodyMetric, Boolean) -> Unit,
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Measurement Collection", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            BodyMetric.entries.filter { it.isMeasured }.forEach { measurement ->
                val required = measurement in requiredMeasurements
                val label = if (required) {
                    "${measurement.label()} (required)"
                } else {
                    measurement.label()
                }
                CheckRow(
                    label = label,
                    checked = measurement in enabledMeasurements,
                    enabled = !required,
                    onCheckedChange = { onMeasurementEnabledChanged(measurement, it) },
                )
            }
        }
    }
}

@Composable
private fun DisplayConfigurationSection(
    state: SettingsUiState,
    onDisplayPlacementChanged: (BodyMetric, DisplayPlacement) -> Unit,
) {
    val visibleMetricTypes = BodyMetric.entries.filter {
        when (it) {
            BodyMetric.NavyBodyFatPercent -> state.settings.navyBodyFatEnabled
            BodyMetric.SkinfoldBodyFatPercent -> state.settings.skinfoldBodyFatEnabled
            else -> true
        }
    }

    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Display Configuration", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            visibleMetricTypes.forEach { metric ->
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
}

@Composable
private fun CheckRow(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        Switch(
            checked = checked,
            onCheckedChange = if (enabled) onCheckedChange else null,
            enabled = enabled,
            modifier = Modifier.size(30.dp),
        )
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
                    .heightIn(max=45.dp)
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
    BodyMetric.Weight -> "Weight"
    BodyMetric.NeckCircumference -> "Neck"
    BodyMetric.WaistCircumference -> "Waist"
    BodyMetric.HipCircumference -> "Hip"
    BodyMetric.ChestCircumference -> "Chest"
    BodyMetric.AbdomenCircumference -> "Abdomen"
    BodyMetric.ChestSkinfold -> "Chest Skinfold"
    BodyMetric.AbdomenSkinfold -> "Abdomen Skinfold"
    BodyMetric.ThighSkinfold -> "Thigh Skinfold"
    BodyMetric.TricepsSkinfold -> "Triceps Skinfold"
    BodyMetric.SuprailiacSkinfold -> "Suprailiac Skinfold"
    BodyMetric.Bmi -> "BMI"
    BodyMetric.NavyBodyFatPercent -> "Navy Body Fat %"
    BodyMetric.SkinfoldBodyFatPercent -> "Skinfold Body Fat %"
    BodyMetric.WaistHipRatio -> "Waist–Hip Ratio"
    BodyMetric.WaistHeightRatio -> "Waist–Height Ratio"
    BodyMetric.HipHeightRatio -> "Hip–Height Ratio"
}


@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    BodyTrackerTheme {
        SettingsScreen(
            state = SettingsUiState(),
            onNavigateBack = {},
            contentPadding = PaddingValues(0.dp),
            onNavyBodyFatEnabledChanged = {},
            onSkinfoldBodyFatEnabledChanged = {},
            onMeasurementEnabledChanged = { _, _ -> },
            onDisplayPlacementChanged = { _, _ -> },
        )
    }
}
