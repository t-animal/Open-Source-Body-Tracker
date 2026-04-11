package de.t_animal.opensourcebodytracker.feature.settings.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.t_animal.opensourcebodytracker.core.model.AnalysisMethod
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MeasurementSettings
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.domain.metrics.RequiredMeasurementsResolver
import de.t_animal.opensourcebodytracker.domain.metrics.SaveMeasurementSettingsUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ChooseMeasurementSettingsUiState {
    val mode: MeasurementSettingsMode

    data class Loading(override val mode: MeasurementSettingsMode) : ChooseMeasurementSettingsUiState

    data class Loaded(
        override val mode: MeasurementSettingsMode,
        val isSaving: Boolean,
        val settings: MeasurementSettings,
        val requiredMeasurements: Set<MeasuredBodyMetric>,
        val measurementToAnalysisMethods: Map<MeasuredBodyMetric, Set<AnalysisMethod>>,
        val hasError: Boolean,
        val sex: Sex?,
    ) : ChooseMeasurementSettingsUiState
}

@HiltViewModel
class ChooseMeasurementSettingsViewModel @Inject constructor(
    private val saveMeasurementSettingsUseCase: SaveMeasurementSettingsUseCase,
    private val requiredMeasurementsResolver: RequiredMeasurementsResolver,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val hasError = MutableStateFlow(false)

    val uiState: StateFlow<ChooseMeasurementSettingsUiState> = combine(
        requiredMeasurementsResolver.effectiveMeasurementSettingsFlow,
        hasError,
        profileRepository.profileFlow,
    ) { effective, error, profile ->
        ChooseMeasurementSettingsUiState.Loaded(
            mode = MeasurementSettingsMode.Settings,
            isSaving = false,
            settings = effective.settings,
            requiredMeasurements = effective.dependencies.requiredMeasurements,
            measurementToAnalysisMethods = effective.dependencies.measurementToAnalysisMethods,
            hasError = error,
            sex = profile?.sex,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChooseMeasurementSettingsUiState.Loading(MeasurementSettingsMode.Settings),
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
        val required = (uiState.value as? ChooseMeasurementSettingsUiState.Loaded)?.requiredMeasurements ?: return
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
        val base = (uiState.value as? ChooseMeasurementSettingsUiState.Loaded)?.settings ?: return
        val transformed = transform(base)

        viewModelScope.launch {
            saveMeasurementSettingsUseCase(transformed)
        }
    }
}
