package de.t_animal.opensourcebodytracker.feature.settings.onboarding

import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MeasurementSettings
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import de.t_animal.opensourcebodytracker.domain.metrics.RequiredMeasurementsResolver
import kotlinx.coroutines.flow.MutableStateFlow

internal class AnalysisStepHandler(
    private val uiState: MutableStateFlow<OnboardingUiState>,
    private val requiredMeasurementsResolver: RequiredMeasurementsResolver,
    private val validatedProfile: () -> UserProfile?,
) {

    fun onBmiEnabledChanged(enabled: Boolean) {
        updateSettings { it.copy(bmiEnabled = enabled) }
    }

    fun onNavyBodyFatEnabledChanged(enabled: Boolean) {
        updateSettings { it.copy(navyBodyFatEnabled = enabled) }
    }

    fun onSkinfoldBodyFatEnabledChanged(enabled: Boolean) {
        updateSettings { it.copy(skinfoldBodyFatEnabled = enabled) }
    }

    fun onWaistHipRatioEnabledChanged(enabled: Boolean) {
        updateSettings { it.copy(waistHipRatioEnabled = enabled) }
    }

    fun onWaistHeightRatioEnabledChanged(enabled: Boolean) {
        updateSettings { it.copy(waistHeightRatioEnabled = enabled) }
    }

    fun onMeasurementEnabledChanged(
        measurementType: MeasuredBodyMetric,
        enabled: Boolean,
    ) {
        val required = uiState.value.analysis.requiredMeasurements
        if (measurementType in required && !enabled) {
            return
        }

        updateSettings { settings ->
            settings.copy(
                enabledMeasurements = if (enabled) {
                    settings.enabledMeasurements + measurementType
                } else {
                    settings.enabledMeasurements - measurementType
                },
            )
        }
    }

    fun recomputeDependencies(profile: UserProfile) {
        val currentAnalysis = uiState.value.analysis
        val effective = requiredMeasurementsResolver.ensureRequired(
            currentAnalysis.settings,
            profile,
        )
        uiState.value = uiState.value.copy(
            analysis = currentAnalysis.copy(
                settings = effective.settings,
                requiredMeasurements = effective.dependencies.requiredMeasurements,
                measurementToAnalysisMethods = effective.dependencies.measurementToAnalysisMethods,
                isLoading = false,
            ),
        )
    }

    private fun updateSettings(transform: (MeasurementSettings) -> MeasurementSettings) {
        val currentAnalysis = uiState.value.analysis
        val transformed = transform(currentAnalysis.settings)
        val profile = validatedProfile() ?: return // TODO: If the profile is not valid, the user should not be able to reach this. 

        val effective = requiredMeasurementsResolver.ensureRequired(transformed, profile)
        uiState.value = uiState.value.copy(
            analysis = currentAnalysis.copy(
                settings = effective.settings,
                requiredMeasurements = effective.dependencies.requiredMeasurements,
                measurementToAnalysisMethods = effective.dependencies.measurementToAnalysisMethods,
            ),
        )
    }
}
