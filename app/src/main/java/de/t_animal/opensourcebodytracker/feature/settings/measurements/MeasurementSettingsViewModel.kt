package de.t_animal.opensourcebodytracker.feature.settings.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.core.model.AnalysisMethod
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.SettingsState
import de.t_animal.opensourcebodytracker.core.model.defaultSettingsState
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.metrics.DerivedMetricsDependencyResolver
import de.t_animal.opensourcebodytracker.domain.metrics.enabledAnalysisMethods
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MeasurementSettingsUiState(
    val isLoading: Boolean = true,
    val settings: SettingsState = defaultSettingsState(),
    val requiredMeasurements: Set<MeasuredBodyMetric> = emptySet(),
    val measurementToAnalysisMethods: Map<MeasuredBodyMetric, Set<AnalysisMethod>> = emptyMap(),
    val errorMessage: String? = null,
)

class MeasurementSettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    private val dependencyResolver: DerivedMetricsDependencyResolver,
) : ViewModel() {

    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MeasurementSettingsUiState> = combine(
        settingsRepository.settingsFlow,
        profileRepository.requiredProfileFlow,
        errorMessage,
    ) { persistedSettings, profile, error ->
        val dependencies = dependencyResolver
            .resolve(persistedSettings.enabledAnalysisMethods(), profile)
        val requiredMeasurements = dependencies.requiredMeasurements

        val effectiveSettings = persistedSettings.copy(
            enabledMeasurements = persistedSettings.enabledMeasurements + requiredMeasurements,
        )

        MeasurementSettingsUiState(
            isLoading = false,
            settings = effectiveSettings,
            requiredMeasurements = requiredMeasurements,
            measurementToAnalysisMethods = dependencies.measurementToAnalysisMethods,
            errorMessage = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MeasurementSettingsUiState(),
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

    private fun updateAndPersist(transform: (SettingsState) -> SettingsState) {
        val base = uiState.value.settings
        val transformed = transform(base)

        viewModelScope.launch {
            val profile = profileRepository.requiredProfileFlow.first()
            val requiredMeasurements = dependencyResolver
                .resolve(transformed.enabledAnalysisMethods(), profile)
                .requiredMeasurements
            val effective = transformed.copy(
                enabledMeasurements = transformed.enabledMeasurements + requiredMeasurements,
            )
            settingsRepository.saveSettings(effective)
        }
    }
}

class MeasurementSettingsViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    private val dependencyResolver: DerivedMetricsDependencyResolver,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MeasurementSettingsViewModel(
            settingsRepository = settingsRepository,
            profileRepository = profileRepository,
            dependencyResolver = dependencyResolver,
        ) as T
    }
}
