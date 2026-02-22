package de.t_animal.opensourcebodytracker.feature.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.DerivedMetrics
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.domain.metrics.CalculateMeasurementDerivedMetricsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MeasurementListUiState(
    val measurements: List<MeasurementListItemUiModel> = emptyList(),
)

data class MeasurementListItemUiModel(
    val measurement: BodyMeasurement,
    val derivedMetrics: DerivedMetrics,
)

class MeasurementListViewModel(
    measurementRepository: MeasurementRepository,
    profileRepository: ProfileRepository,
    calculateMeasurementDerivedMetrics: CalculateMeasurementDerivedMetricsUseCase,
) : ViewModel() {
    val uiState: StateFlow<MeasurementListUiState> = combine(
        measurementRepository.observeAll(),
        profileRepository.profileFlow,
    ) { measurements, profile ->
        val items = measurements.map { measurement ->
            MeasurementListItemUiModel(
                measurement = measurement,
                derivedMetrics = calculateMeasurementDerivedMetrics(profile, measurement),
            )
        }

        MeasurementListUiState(measurements = items)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MeasurementListUiState(),
        )
}

class MeasurementListViewModelFactory(
    private val measurementRepository: MeasurementRepository,
    private val profileRepository: ProfileRepository,
    private val calculateMeasurementDerivedMetrics: CalculateMeasurementDerivedMetricsUseCase,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MeasurementListViewModel(
            measurementRepository = measurementRepository,
            profileRepository = profileRepository,
            calculateMeasurementDerivedMetrics = calculateMeasurementDerivedMetrics,
        ) as T
    }
}
