package de.t_animal.opensourcebodytracker.feature.settings.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.AnalysisMethod
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.feature.settings.components.AnalysisMethodsSection
import de.t_animal.opensourcebodytracker.feature.settings.components.MeasurementCollectionSection
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@Composable
fun OnboardingAnalysisRoute(
    onFinished: () -> Unit,
) {
    val vm: OnboardingAnalysisViewModel = hiltViewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                OnboardingAnalysisEvent.Completed -> onFinished()
            }
        }
    }

    OnboardingAnalysisScreen(
        state = state,
        onBmiEnabledChanged = vm::onBmiEnabledChanged,
        onNavyBodyFatEnabledChanged = vm::onNavyBodyFatEnabledChanged,
        onSkinfoldBodyFatEnabledChanged = vm::onSkinfoldBodyFatEnabledChanged,
        onWaistHipRatioEnabledChanged = vm::onWaistHipRatioEnabledChanged,
        onWaistHeightRatioEnabledChanged = vm::onWaistHeightRatioEnabledChanged,
        onMeasurementEnabledChanged = vm::onMeasurementEnabledChanged,
        onFinishClicked = vm::onFinishClicked,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun OnboardingAnalysisScreen(
    state: OnboardingAnalysisUiState,
    onBmiEnabledChanged: (Boolean) -> Unit,
    onNavyBodyFatEnabledChanged: (Boolean) -> Unit,
    onSkinfoldBodyFatEnabledChanged: (Boolean) -> Unit,
    onWaistHipRatioEnabledChanged: (Boolean) -> Unit,
    onWaistHeightRatioEnabledChanged: (Boolean) -> Unit,
    onMeasurementEnabledChanged: (MeasuredBodyMetric, Boolean) -> Unit,
    onFinishClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.onboarding_analysis_title)) },
            )
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.onboarding_analysis_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.onboarding_analysis_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            item {
                Text(
                    text = stringResource(R.string.onboarding_analysis_methods_header),
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
                    text = stringResource(R.string.onboarding_measurements_header),
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

            if (state.hasError) {
                item {
                    Text(
                        text = stringResource(R.string.onboarding_error_save_failed),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            item {
                Column {
                    Button(
                        onClick = onFinishClicked,
                        enabled = !state.isLoading && !state.isSaving,
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(vertical = 2.dp),
                            )
                        } else {
                            Text(stringResource(R.string.common_continue))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingAnalysisScreenPreview() {
    BodyTrackerTheme {
        OnboardingAnalysisScreen(
            state = OnboardingAnalysisUiState(
                isLoading = false,
                requiredMeasurements = setOf(MeasuredBodyMetric.WaistCircumference),
                measurementToAnalysisMethods = mapOf(
                    MeasuredBodyMetric.WaistCircumference to setOf(
                        AnalysisMethod.NavyBodyFat,
                        AnalysisMethod.WaistHipRatio,
                        AnalysisMethod.WaistHeightRatio,
                    ),
                ),
            ),
            onBmiEnabledChanged = {},
            onNavyBodyFatEnabledChanged = {},
            onSkinfoldBodyFatEnabledChanged = {},
            onWaistHipRatioEnabledChanged = {},
            onWaistHeightRatioEnabledChanged = {},
            onMeasurementEnabledChanged = { _, _ -> },
            onFinishClicked = {},
        )
    }
}
