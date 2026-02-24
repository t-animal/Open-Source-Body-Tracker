package de.t_animal.opensourcebodytracker.feature.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.domain.metrics.CalculateMeasurementDerivedMetricsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.ZoneId

class AnalysisViewModel(
    measurementRepository: MeasurementRepository,
    profileRepository: ProfileRepository,
    private val calculateMeasurementDerivedMetrics: CalculateMeasurementDerivedMetricsUseCase,
    private val nowProvider: () -> Instant = { Instant.now() },
    private val zoneIdProvider: () -> ZoneId = { ZoneId.systemDefault() },
) : ViewModel() {
    private val selectedDuration = MutableStateFlow(AnalysisDuration.ThreeMonths)

    val uiState: StateFlow<AnalysisUiState> = combine(
        measurementRepository.observeAll(),
        profileRepository.profileFlow,
        selectedDuration,
    ) { measurements, profile, duration ->
        val withDerived = measurements.map { measurement ->
            MeasurementWithDerived(
                measurement = measurement,
                derivedMetrics = calculateMeasurementDerivedMetrics(profile, measurement),
            )
        }

        val filteredItems = filterByDuration(
            items = withDerived,
            duration = duration,
            now = nowProvider(),
            zoneId = zoneIdProvider(),
        )

        AnalysisUiState(
            selectedDuration = duration,
            metricCharts = buildMetricCharts(filteredItems),
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AnalysisUiState(),
    )

    fun onDurationSelected(duration: AnalysisDuration) {
        selectedDuration.value = duration
    }
}

class AnalysisViewModelFactory(
    private val measurementRepository: MeasurementRepository,
    private val profileRepository: ProfileRepository,
    private val calculateMeasurementDerivedMetrics: CalculateMeasurementDerivedMetricsUseCase,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AnalysisViewModel(
            measurementRepository = measurementRepository,
            profileRepository = profileRepository,
            calculateMeasurementDerivedMetrics = calculateMeasurementDerivedMetrics,
        ) as T
    }
}
