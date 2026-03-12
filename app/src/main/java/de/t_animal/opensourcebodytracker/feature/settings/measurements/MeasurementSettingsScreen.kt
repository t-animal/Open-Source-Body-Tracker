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
import androidx.lifecycle.viewmodel.compose.viewModel
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.defaultSettingsState
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.metrics.DerivedMetricsDependencyResolver
import de.t_animal.opensourcebodytracker.feature.settings.components.AnalysisMethodsSection
import de.t_animal.opensourcebodytracker.feature.settings.components.MeasurementCollectionSection
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@Composable
fun MeasurementSettingsRoute(
    settingsRepository: SettingsRepository,
    profileRepository: ProfileRepository,
    onNavigateBack: () -> Unit,
) {
    val vm: MeasurementSettingsViewModel = viewModel(
        factory = MeasurementSettingsViewModelFactory(
            settingsRepository = settingsRepository,
            profileRepository = profileRepository,
            dependencyResolver = DerivedMetricsDependencyResolver(),
        ),
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    MeasurementSettingsScreen(
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
fun MeasurementSettingsScreen(
    state: MeasurementSettingsUiState,
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
                .padding(contentPadding),
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
private fun MeasurementSettingsScreenPreview() {
    val settings = defaultSettingsState().copy(
        enabledMeasurements = MeasuredBodyMetric.entries.toSet() - MeasuredBodyMetric.SuprailiacSkinfold,
    )

    BodyTrackerTheme {
        MeasurementSettingsScreen(
            state = MeasurementSettingsUiState(
                isLoading = false,
                settings = settings,
                requiredMeasurements = setOf(MeasuredBodyMetric.WaistCircumference),
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
