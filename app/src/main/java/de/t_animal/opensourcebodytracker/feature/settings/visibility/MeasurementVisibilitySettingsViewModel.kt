package de.t_animal.opensourcebodytracker.feature.settings.visibility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MeasurementSettings
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.MeasurementSettingsRepository
import de.t_animal.opensourcebodytracker.domain.metrics.DerivedMetricsDependencyResolver
import javax.inject.Inject
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
    val settings: MeasurementSettings = MeasurementSettings(),
    val requiredMeasurements: Set<MeasuredBodyMetric> = emptySet(),
    val errorMessage: String? = null,
)

@HiltViewModel
class MeasurementVisibilitySettingsViewModel @Inject constructor(
    private val measurementSettingsRepository: MeasurementSettingsRepository,
    private val profileRepository: ProfileRepository,
    private val dependencyResolver: DerivedMetricsDependencyResolver,
) : ViewModel() {

    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        measurementSettingsRepository.settingsFlow,
        profileRepository.requiredProfileFlow,
        errorMessage,
    ) { persistedSettings, profile, error ->
        val requiredMeasurements = dependencyResolver
            .resolve(persistedSettings.enabledAnalysisMethods, profile)
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

    private fun updateAndPersist(transform: (MeasurementSettings) -> MeasurementSettings) {
        val base = uiState.value.settings
        val transformed = transform(base)

        viewModelScope.launch {
            val profile = profileRepository.requiredProfileFlow.first()
            val requiredMeasurements = dependencyResolver
                .resolve(transformed.enabledAnalysisMethods, profile)
                .requiredMeasurements
            val effective = transformed.copy(
                enabledMeasurements = transformed.enabledMeasurements + requiredMeasurements,
            )
            measurementSettingsRepository.saveSettings(effective)
        }
    }
}
