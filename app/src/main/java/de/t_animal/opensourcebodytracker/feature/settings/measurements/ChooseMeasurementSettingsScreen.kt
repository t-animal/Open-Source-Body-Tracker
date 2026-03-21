package de.t_animal.opensourcebodytracker.feature.settings.measurements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import de.t_animal.opensourcebodytracker.core.model.AnalysisMethod
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MeasurementSettings
import de.t_animal.opensourcebodytracker.feature.settings.components.AnalysisMethodsSection
import de.t_animal.opensourcebodytracker.feature.settings.components.MeasurementCollectionSection
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@Composable
fun MeasurementSettingsRoute(
    onNavigateBack: () -> Unit,
) {
    val vm: ChooseMeasurementSettingsViewModel = hiltViewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    ChooseMeasurementSettingsScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onMeasurementEnabledChanged = vm::onMeasurementEnabledChanged,
        onBmiEnabledChanged = vm::onBmiEnabledChanged,
        onNavyBodyFatEnabledChanged = vm::onNavyBodyFatEnabledChanged,
        onSkinfoldBodyFatEnabledChanged = vm::onSkinfoldBodyFatEnabledChanged,
        onWaistHipRatioEnabledChanged = vm::onWaistHipRatioEnabledChanged,
        onWaistHeightRatioEnabledChanged = vm::onWaistHeightRatioEnabledChanged,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseMeasurementSettingsScreen(
    state: ChooseMeasurementSettingsUiState,
    onNavigateBack: () -> Unit,
    onMeasurementEnabledChanged: (MeasuredBodyMetric, Boolean) -> Unit,
    onBmiEnabledChanged: (Boolean) -> Unit,
    onNavyBodyFatEnabledChanged: (Boolean) -> Unit,
    onSkinfoldBodyFatEnabledChanged: (Boolean) -> Unit,
    onWaistHipRatioEnabledChanged: (Boolean) -> Unit,
    onWaistHeightRatioEnabledChanged: (Boolean) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Measurements & Analysis") },
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Configure measurement collection and analysis methods in one place. " +
                        "Required measurements stay enabled when selected analyses depend on them.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }

            item {
                Text(
                    text = "Analysis Methods",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            item {
                AnalysisMethodsSection(
                    bmiEnabled = state.settings.bmiEnabled,
                    navyBodyFatEnabled = state.settings.navyBodyFatEnabled,
                    skinfoldBodyFatEnabled = state.settings.skinfoldBodyFatEnabled,
                    waistHipRatioEnabled = state.settings.waistHipRatioEnabled,
                    waistHeightRatioEnabled = state.settings.waistHeightRatioEnabled,
                    onBmiEnabledChanged = onBmiEnabledChanged,
                    onNavyBodyFatEnabledChanged = onNavyBodyFatEnabledChanged,
                    onSkinfoldBodyFatEnabledChanged = onSkinfoldBodyFatEnabledChanged,
                    onWaistHipRatioEnabledChanged = onWaistHipRatioEnabledChanged,
                    onWaistHeightRatioEnabledChanged = onWaistHeightRatioEnabledChanged,
                )
            }

            item {
                Text(
                    text = "Measurements",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            item {
                MeasurementCollectionSection(
                    enabledMeasurements = state.settings.enabledMeasurements,
                    requiredMeasurements = state.requiredMeasurements,
                    measurementToAnalysisMethods = state.measurementToAnalysisMethods,
                    onMeasurementEnabledChanged = onMeasurementEnabledChanged,
                )
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

@Preview(showBackground = true)
@Composable
private fun ChooseMeasurementSettingsScreenPreview() {
    val settings = MeasurementSettings(
        enabledMeasurements = MeasuredBodyMetric.entries.toSet() - MeasuredBodyMetric.SuprailiacSkinfold,
    )

    BodyTrackerTheme {
        ChooseMeasurementSettingsScreen(
            state = ChooseMeasurementSettingsUiState(
                isLoading = false,
                settings = settings,
                requiredMeasurements = setOf(MeasuredBodyMetric.WaistCircumference),
                measurementToAnalysisMethods = mapOf(
                    MeasuredBodyMetric.WaistCircumference to setOf(
                        AnalysisMethod.NavyBodyFat,
                        AnalysisMethod.WaistHipRatio,
                        AnalysisMethod.WaistHeightRatio,
                    ),
                ),
            ),
            onNavigateBack = {},
            onMeasurementEnabledChanged = { _, _ -> },
            onBmiEnabledChanged = {},
            onNavyBodyFatEnabledChanged = {},
            onSkinfoldBodyFatEnabledChanged = {},
            onWaistHipRatioEnabledChanged = {},
            onWaistHeightRatioEnabledChanged = {},
        )
    }
}
