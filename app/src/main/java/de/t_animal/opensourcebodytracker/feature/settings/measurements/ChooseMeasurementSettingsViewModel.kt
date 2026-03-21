package de.t_animal.opensourcebodytracker.feature.settings.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.t_animal.opensourcebodytracker.core.model.AnalysisMethod
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MeasurementSettings
import de.t_animal.opensourcebodytracker.data.settings.MeasurementSettingsRepository
import de.t_animal.opensourcebodytracker.domain.metrics.RequiredMeasurementsResolver
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChooseMeasurementSettingsUiState(
    val isLoading: Boolean = true,
    val settings: MeasurementSettings = MeasurementSettings(),
    val requiredMeasurements: Set<MeasuredBodyMetric> = emptySet(),
    val measurementToAnalysisMethods: Map<MeasuredBodyMetric, Set<AnalysisMethod>> = emptyMap(),
    val errorMessage: String? = null,
)

@HiltViewModel
class ChooseMeasurementSettingsViewModel @Inject constructor(
    private val measurementSettingsRepository: MeasurementSettingsRepository,
    private val requiredMeasurementsResolver: RequiredMeasurementsResolver,
) : ViewModel() {

    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ChooseMeasurementSettingsUiState> = combine(
        requiredMeasurementsResolver.effectiveMeasurementSettingsFlow,
        errorMessage,
    ) { effective, error ->
        ChooseMeasurementSettingsUiState(
            isLoading = false,
            settings = effective.settings,
            requiredMeasurements = effective.dependencies.requiredMeasurements,
            measurementToAnalysisMethods = effective.dependencies.measurementToAnalysisMethods,
            errorMessage = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChooseMeasurementSettingsUiState(),
    )

    fun onNavyBodyFatEnabledChanged(enabled: Boolean) {
        updateAndPersist { it.copy(navyBodyFatEnabled = enabled) }
    }

    fun onBmiEnabledChanged(enabled: Boolean) {
        updateAndPersist { it.copy(bmiEnabled = enabled) }
    }

    fun onSkinfoldBodyFatEnabledChanged(enabled: Boolean) {
        updateAndPersist { it.copy(skinfoldBodyFatEnabled = enabled) }
    }

    fun onWaistHipRatioEnabledChanged(enabled: Boolean) {
        updateAndPersist { it.copy(waistHipRatioEnabled = enabled) }
    }

    fun onWaistHeightRatioEnabledChanged(enabled: Boolean) {
        updateAndPersist { it.copy(waistHeightRatioEnabled = enabled) }
    }

    fun onMeasurementEnabledChanged(
        measurementType: MeasuredBodyMetric,
        enabled: Boolean,
    ) {
        val required = uiState.value.requiredMeasurements
        if (measurementType in required && !enabled) {
            return
        }

        updateAndPersist { settings ->
            settings.copy(
                enabledMeasurements = if (enabled) {
                    settings.enabledMeasurements + measurementType
                } else {
                    settings.enabledMeasurements - measurementType
                },
            )
        }
    }

    private fun updateAndPersist(transform: (MeasurementSettings) -> MeasurementSettings) {
        val base = uiState.value.settings
        val transformed = transform(base)

        viewModelScope.launch {
            val effective = requiredMeasurementsResolver.ensureRequiredWithCurrentProfile(transformed)
            measurementSettingsRepository.saveSettings(effective.settings)
        }
    }
}
