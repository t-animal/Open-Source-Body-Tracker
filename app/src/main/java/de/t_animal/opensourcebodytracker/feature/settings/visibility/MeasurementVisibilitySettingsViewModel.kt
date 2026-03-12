package de.t_animal.opensourcebodytracker.feature.settings.visibility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
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

enum class DisplayPlacement {
    Hidden,
    OnlyInTable,
    OnlyInAnalysis,
    InBoth,
}

data class SettingsUiState(
    val isLoading: Boolean = true,
    val settings: SettingsState = defaultSettingsState(),
    val requiredMeasurements: Set<MeasuredBodyMetric> = emptySet(),
    val errorMessage: String? = null,
)

class MeasurementVisibilitySettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    private val dependencyResolver: DerivedMetricsDependencyResolver,
) : ViewModel() {

    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settingsFlow,
        profileRepository.requiredProfileFlow,
        errorMessage,
    ) { persistedSettings, profile, error ->
        val requiredMeasurements = dependencyResolver
            .resolve(persistedSettings.enabledAnalysisMethods(), profile)
            .requiredMeasurements

        val effectiveSettings = persistedSettings.copy(
            enabledMeasurements = persistedSettings.enabledMeasurements + requiredMeasurements,
        )

        SettingsUiState(
            isLoading = false,
            settings = effectiveSettings,
            requiredMeasurements = requiredMeasurements,
            errorMessage = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

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

class SettingsViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    private val dependencyResolver: DerivedMetricsDependencyResolver,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MeasurementVisibilitySettingsViewModel(
            settingsRepository = settingsRepository,
            profileRepository = profileRepository,
            dependencyResolver = dependencyResolver,
        ) as T
    }
}
