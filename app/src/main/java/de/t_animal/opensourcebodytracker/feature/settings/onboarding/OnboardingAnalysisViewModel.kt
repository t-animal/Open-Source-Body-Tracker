package de.t_animal.opensourcebodytracker.feature.settings.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import de.t_animal.opensourcebodytracker.core.model.AnalysisMethod
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.SettingsState
import de.t_animal.opensourcebodytracker.core.model.defaultSettingsState
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.metrics.DerivedBodyMetricsDependencies
import de.t_animal.opensourcebodytracker.domain.metrics.DerivedMetricsDependencyResolver
import de.t_animal.opensourcebodytracker.domain.metrics.enabledAnalysisMethods
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
    val settings: SettingsState = defaultSettingsState(),
    val requiredMeasurements: Set<MeasuredBodyMetric> = emptySet(),
    val measurementToAnalysisMethods: Map<MeasuredBodyMetric, Set<AnalysisMethod>> = emptyMap(),
    val errorMessage: String? = null,
)

sealed interface OnboardingAnalysisEvent {
    data object Completed : OnboardingAnalysisEvent
}

@HiltViewModel
class OnboardingAnalysisViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    private val dependencyResolver: DerivedMetricsDependencyResolver,
) : ViewModel() {
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isSaving = MutableStateFlow(false)

    private val _events = MutableSharedFlow<OnboardingAnalysisEvent>()
    val events = _events.asSharedFlow()

    private val settingsWriteMutex = Mutex()
    private var lastPersistJob: Job? = null

    val uiState: StateFlow<OnboardingAnalysisUiState> = combine(
        settingsRepository.settingsFlow,
        profileRepository.profileFlow,
        _isSaving,
        _errorMessage,
    ) { settings, profile, isSaving, errorMessage ->
        val dependencies = profile?.let {
            dependencyResolver.resolve(settings.enabledAnalysisMethods(), it)
        } ?: DerivedBodyMetricsDependencies()

        val requiredMeasurements = dependencies.requiredMeasurements
        val effectiveSettings = settings.copy(
            enabledMeasurements = settings.enabledMeasurements + requiredMeasurements,
        )

        OnboardingAnalysisUiState(
            isLoading = profile == null,
            isSaving = isSaving,
            settings = effectiveSettings,
            requiredMeasurements = requiredMeasurements,
            measurementToAnalysisMethods = dependencies.measurementToAnalysisMethods,
            errorMessage = errorMessage,
        )
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
            _errorMessage.value = null

            runCatching {
                lastPersistJob?.join()
                _events.emit(OnboardingAnalysisEvent.Completed)
            }.onFailure { throwable ->
                _errorMessage.value = throwable.message ?: "Could not continue onboarding"
            }

            _isSaving.value = false
        }
    }

    private fun enqueueUpdateAndPersist(transform: (SettingsState) -> SettingsState) {
        lastPersistJob = viewModelScope.launch {
            updateAndPersist(transform)
        }
    }

    private suspend fun updateAndPersist(transform: (SettingsState) -> SettingsState) {
        settingsWriteMutex.withLock {
            runCatching {
                val profile = profileRepository.requiredProfileFlow.first()
                val base = settingsRepository.settingsFlow.first()
                val transformed = transform(base)
                val requiredMeasurements = dependencyResolver
                    .resolve(transformed.enabledAnalysisMethods(), profile)
                    .requiredMeasurements
                val effective = transformed.copy(
                    enabledMeasurements = transformed.enabledMeasurements + requiredMeasurements,
                )
                if (effective != base) {
                    settingsRepository.saveSettings(effective)
                }
            }.onFailure { throwable ->
                _errorMessage.value = throwable.message ?: "Could not save onboarding settings"
            }
        }
    }
}
