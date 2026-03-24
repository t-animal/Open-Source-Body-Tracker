package de.t_animal.opensourcebodytracker.feature.settings.measurements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.t_animal.opensourcebodytracker.R
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
    onNavigateBack: (() -> Unit)? = null,
    onContinueClicked: (() -> Unit)? = null,
    onMeasurementEnabledChanged: (MeasuredBodyMetric, Boolean) -> Unit,
    onBmiEnabledChanged: (Boolean) -> Unit,
    onNavyBodyFatEnabledChanged: (Boolean) -> Unit,
    onSkinfoldBodyFatEnabledChanged: (Boolean) -> Unit,
    onWaistHipRatioEnabledChanged: (Boolean) -> Unit,
    onWaistHeightRatioEnabledChanged: (Boolean) -> Unit,
) {
    val isOnboarding = state.mode == MeasurementSettingsMode.Onboarding

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (isOnboarding) R.string.onboarding_analysis_title
                            else R.string.settings_measurements_analysis_title,
                        ),
                    )
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back),
                            )
                        }
                    }
                },
            )
        },
    ) { contentPadding ->
        when (state) {
        is ChooseMeasurementSettingsUiState.Loading -> Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator()
        }
        is ChooseMeasurementSettingsUiState.Loaded -> LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.settings_measurements_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (isOnboarding) {
                    Text(
                        text = stringResource(R.string.onboarding_analysis_hint),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.settings_measurements_analysis_methods_header),
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
                    text = stringResource(R.string.settings_measurements_header),
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
                        text = stringResource(R.string.settings_measurements_error_save_failed),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            if (onContinueClicked != null) {
                item {
                    Column {
                        Button(
                            onClick = onContinueClicked,
                            enabled = !state.isSaving,
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
    }
}

@Preview(showBackground = true)
@Composable
private fun ChooseMeasurementSettingsScreenSettingsPreview() {
    val settings = MeasurementSettings(
        enabledMeasurements = MeasuredBodyMetric.entries.toSet() - MeasuredBodyMetric.SuprailiacSkinfold,
    )

    BodyTrackerTheme {
        ChooseMeasurementSettingsScreen(
            state = ChooseMeasurementSettingsUiState.Loaded(
                mode = MeasurementSettingsMode.Settings,
                isSaving = false,
                settings = settings,
                requiredMeasurements = setOf(MeasuredBodyMetric.WaistCircumference),
                measurementToAnalysisMethods = mapOf(
                    MeasuredBodyMetric.WaistCircumference to setOf(
                        AnalysisMethod.NavyBodyFat,
                        AnalysisMethod.WaistHipRatio,
                        AnalysisMethod.WaistHeightRatio,
                    ),
                ),
                hasError = false,
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

@Preview(showBackground = true)
@Composable
private fun ChooseMeasurementSettingsScreenOnboardingPreview() {
    BodyTrackerTheme {
        ChooseMeasurementSettingsScreen(
            state = ChooseMeasurementSettingsUiState.Loaded(
                mode = MeasurementSettingsMode.Onboarding,
                isSaving = false,
                settings = MeasurementSettings(),
                requiredMeasurements = setOf(MeasuredBodyMetric.WaistCircumference),
                measurementToAnalysisMethods = mapOf(
                    MeasuredBodyMetric.WaistCircumference to setOf(
                        AnalysisMethod.NavyBodyFat,
                        AnalysisMethod.WaistHipRatio,
                        AnalysisMethod.WaistHeightRatio,
                    ),
                ),
                hasError = false,
            ),
            onContinueClicked = {},
            onMeasurementEnabledChanged = { _, _ -> },
            onBmiEnabledChanged = {},
            onNavyBodyFatEnabledChanged = {},
            onSkinfoldBodyFatEnabledChanged = {},
            onWaistHipRatioEnabledChanged = {},
            onWaistHeightRatioEnabledChanged = {},
        )
    }
}
