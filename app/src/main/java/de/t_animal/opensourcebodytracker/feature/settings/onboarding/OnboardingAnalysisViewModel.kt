package de.t_animal.opensourcebodytracker.feature.settings.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import de.t_animal.opensourcebodytracker.core.model.AnalysisMethod
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MeasurementSettings
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.MeasurementSettingsRepository
import de.t_animal.opensourcebodytracker.domain.metrics.RequiredMeasurementsResolver
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class OnboardingAnalysisUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val settings: MeasurementSettings = MeasurementSettings(),
    val requiredMeasurements: Set<MeasuredBodyMetric> = emptySet(),
    val measurementToAnalysisMethods: Map<MeasuredBodyMetric, Set<AnalysisMethod>> = emptyMap(),
    val hasError: Boolean = false,
)

sealed interface OnboardingAnalysisEvent {
    data object Completed : OnboardingAnalysisEvent
}

@HiltViewModel
class OnboardingAnalysisViewModel @Inject constructor(
    private val measurementSettingsRepository: MeasurementSettingsRepository,
    private val profileRepository: ProfileRepository,
    private val requiredMeasurementsResolver: RequiredMeasurementsResolver,
) : ViewModel() {
    private val _hasError = MutableStateFlow(false)
    private val _isSaving = MutableStateFlow(false)

    private val _events = MutableSharedFlow<OnboardingAnalysisEvent>()
    val events = _events.asSharedFlow()

    private val settingsWriteMutex = Mutex()
    private var lastPersistJob: Job? = null

    val uiState: StateFlow<OnboardingAnalysisUiState> = combine(
        measurementSettingsRepository.settingsFlow,
        profileRepository.profileFlow,
        _isSaving,
        _hasError,
    ) { settings, profile, isSaving, hasError ->
        if (profile == null) {
            OnboardingAnalysisUiState(isLoading = true, isSaving = isSaving)
        } else {
            val effective = requiredMeasurementsResolver.ensureRequired(settings, profile)
            OnboardingAnalysisUiState(
                isLoading = false,
                isSaving = isSaving,
                settings = effective.settings,
                requiredMeasurements = effective.dependencies.requiredMeasurements,
                measurementToAnalysisMethods = effective.dependencies.measurementToAnalysisMethods,
                hasError = hasError,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5_000),
        initialValue = OnboardingAnalysisUiState(),
    )

    fun onNavyBodyFatEnabledChanged(enabled: Boolean) {
        enqueueUpdateAndPersist { it.copy(navyBodyFatEnabled = enabled) }
    }

    fun onBmiEnabledChanged(enabled: Boolean) {
        enqueueUpdateAndPersist { it.copy(bmiEnabled = enabled) }
    }

    fun onSkinfoldBodyFatEnabledChanged(enabled: Boolean) {
        enqueueUpdateAndPersist { it.copy(skinfoldBodyFatEnabled = enabled) }
    }

    fun onWaistHipRatioEnabledChanged(enabled: Boolean) {
        enqueueUpdateAndPersist { it.copy(waistHipRatioEnabled = enabled) }
    }

    fun onWaistHeightRatioEnabledChanged(enabled: Boolean) {
        enqueueUpdateAndPersist { it.copy(waistHeightRatioEnabled = enabled) }
    }

    fun onMeasurementEnabledChanged(
        measurementType: MeasuredBodyMetric,
        enabled: Boolean,
    ) {
        val required = uiState.value.requiredMeasurements
        if (measurementType in required && !enabled) {
            return
        }

        enqueueUpdateAndPersist { settings ->
            settings.copy(
                enabledMeasurements = if (enabled) {
                    settings.enabledMeasurements + measurementType
                } else {
                    settings.enabledMeasurements - measurementType
                },
            )
        }
    }

    fun onFinishClicked() {
        if (_isSaving.value) return

        viewModelScope.launch {
            _isSaving.value = true
            _hasError.value = false

            runCatching {
                lastPersistJob?.join()
                _events.emit(OnboardingAnalysisEvent.Completed)
            }.onFailure { throwable ->
                _hasError.value = true
            }

            _isSaving.value = false
        }
    }

    private fun enqueueUpdateAndPersist(transform: (MeasurementSettings) -> MeasurementSettings) {
        lastPersistJob = viewModelScope.launch {
            updateAndPersist(transform)
        }
    }

    private suspend fun updateAndPersist(transform: (MeasurementSettings) -> MeasurementSettings) {
        settingsWriteMutex.withLock {
            runCatching {
                val base = measurementSettingsRepository.settingsFlow.first()
                val transformed = transform(base)
                val effective = requiredMeasurementsResolver.ensureRequiredWithCurrentProfile(transformed)
                if (effective.settings != base) {
                    measurementSettingsRepository.saveSettings(effective.settings)
                }
            }.onFailure { throwable ->
                _hasError.value = true
            }
        }
    }
}
