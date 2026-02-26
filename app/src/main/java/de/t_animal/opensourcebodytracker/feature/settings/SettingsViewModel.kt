package de.t_animal.opensourcebodytracker.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.SettingsState
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import de.t_animal.opensourcebodytracker.core.model.defaultSettingsState
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.metrics.DerivedMetricsDependencyResolver
import de.t_animal.opensourcebodytracker.domain.metrics.enabledAnalysisMethods
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DisplayPlacement {
    Hidden,
    OnlyInTable,
    OnlyInAnalysis,
    InBoth,
}

data class SettingsUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val settings: SettingsState = defaultSettingsState(),
    val requiredMeasurements: Set<BodyMetric> = emptySet(),
    val errorMessage: String? = null,
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    profileRepository: ProfileRepository,
    private val dependencyResolver: DerivedMetricsDependencyResolver,
) : ViewModel() {

    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settingsFlow,
        profileRepository.profileFlow,
        errorMessage,
    ) { persistedSettings, profile, error ->
        val editableSettings = persistedSettings
        val requiredMeasurements = profile
            ?.let { dependencyResolver.resolve(editableSettings.enabledAnalysisMethods(), it).requiredMeasurements }
            .orEmpty()

        val effectiveSettings = editableSettings.copy(
            enabledMeasurements = editableSettings.enabledMeasurements + requiredMeasurements,
        )

        SettingsUiState(
            isLoading = false,
            profile = profile,
            settings = effectiveSettings,
            requiredMeasurements = requiredMeasurements,
            errorMessage = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
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

    fun onMeasurementEnabledChanged(
        measurementType: BodyMetric,
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

    fun onDisplayPlacementChanged(
        metricType: BodyMetric,
        placement: DisplayPlacement,
    ) {
        updateAndPersist { settings ->
            settings.copy(
                visibleInAnalysis = when (placement) {
                    DisplayPlacement.Hidden, DisplayPlacement.OnlyInTable ->
                        settings.visibleInAnalysis - metricType

                    DisplayPlacement.OnlyInAnalysis, DisplayPlacement.InBoth ->
                        settings.visibleInAnalysis + metricType
                },
                visibleInTable = when (placement) {
                    DisplayPlacement.Hidden, DisplayPlacement.OnlyInAnalysis ->
                        settings.visibleInTable - metricType

                    DisplayPlacement.OnlyInTable, DisplayPlacement.InBoth ->
                        settings.visibleInTable + metricType
                },
            )
        }
    }

    private fun updateAndPersist(transform: (SettingsState) -> SettingsState) {
        val base = uiState.value.settings
        val transformed = transform(base)
        val requiredMeasurements = uiState.value.profile
            ?.let { dependencyResolver.resolve(transformed.enabledAnalysisMethods(), it).requiredMeasurements }
            .orEmpty()
        val effective = transformed.copy(
            enabledMeasurements = transformed.enabledMeasurements + requiredMeasurements,
        )

        viewModelScope.launch {
            settingsRepository.saveSettings(effective)
        }
    }
}

class SettingsViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    private val dependencyResolver: DerivedMetricsDependencyResolver,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(
            settingsRepository = settingsRepository,
            profileRepository = profileRepository,
            dependencyResolver = dependencyResolver,
        ) as T
    }
}
