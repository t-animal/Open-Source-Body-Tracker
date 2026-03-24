package de.t_animal.opensourcebodytracker.feature.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.DerivedMetricRatings
import de.t_animal.opensourcebodytracker.core.model.DerivedMetrics
import de.t_animal.opensourcebodytracker.core.model.UnitSystem
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.GeneralSettingsRepository
import de.t_animal.opensourcebodytracker.data.settings.MeasurementSettingsRepository
import de.t_animal.opensourcebodytracker.domain.metrics.CalculateAndRateDerivedMetricsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed interface MeasurementListUiState {
    data object Loading : MeasurementListUiState

    data class Loaded(
        val latestMeasurement: MeasurementListItemUiModel?,
        val previewMeasurements: List<MeasurementListItemUiModel>,
        val allMeasurements: List<MeasurementListItemUiModel>,
        val hasMoreMeasurements: Boolean,
        val visibleInTableMetrics: List<BodyMetric>,
        val unitSystem: UnitSystem,
        val isEmpty: Boolean,
    ) : MeasurementListUiState
}

data class MeasurementListItemUiModel(
    val measurement: BodyMeasurement,
    val derivedMetrics: DerivedMetrics,
    val derivedMetricRatings: DerivedMetricRatings = DerivedMetricRatings(),
)

@HiltViewModel
class MeasurementListViewModel @Inject constructor(
    measurementRepository: MeasurementRepository,
    profileRepository: ProfileRepository,
    measurementSettingsRepository: MeasurementSettingsRepository,
    generalSettingsRepository: GeneralSettingsRepository,
    calculateMeasurementDerivedMetrics: CalculateAndRateDerivedMetricsUseCase,
) : ViewModel() {
    val uiState: StateFlow<MeasurementListUiState> = combine(
        measurementRepository.observeAll(),
        profileRepository.requiredProfileFlow,
        measurementSettingsRepository.settingsFlow,
        generalSettingsRepository.settingsFlow,
    ) { measurements, profile, settings, generalSettings ->
        val items = measurements.map { measurement ->
            val analysis = calculateMeasurementDerivedMetrics(profile, measurement)
            MeasurementListItemUiModel(
                measurement = measurement,
                derivedMetrics = analysis.metrics,
                derivedMetricRatings = analysis.ratings,
            )
        }

        val orderedVisibleInTableMetrics = settings.visibleInTableOrdered

        MeasurementListUiState.Loaded(
            latestMeasurement = items.firstOrNull(),
            previewMeasurements = items.take(PREVIEW_LIMIT),
            allMeasurements = items,
            hasMoreMeasurements = items.size > PREVIEW_LIMIT,
            visibleInTableMetrics = orderedVisibleInTableMetrics,
            unitSystem = generalSettings.unitSystem,
            isEmpty = items.isEmpty(),
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MeasurementListUiState.Loading,
        )

    companion object {
        private const val PREVIEW_LIMIT = 20
    }
}
