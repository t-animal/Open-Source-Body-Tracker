package de.t_animal.opensourcebodytracker.feature.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.DerivedMetrics
import de.t_animal.opensourcebodytracker.core.model.BodyMetric.Companion.entries
import de.t_animal.opensourcebodytracker.core.model.visibleInTableOrdered
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.metrics.CalculateMeasurementDerivedMetricsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MeasurementListUiState(
    val latestMeasurement: MeasurementListItemUiModel? = null,
    val previewMeasurements: List<MeasurementListItemUiModel> = emptyList(),
    val allMeasurements: List<MeasurementListItemUiModel> = emptyList(),
    val hasMoreMeasurements: Boolean = false,
    val visibleInTableMetrics: List<BodyMetric> = entries,
    val isEmpty: Boolean = true,
    val isLoading: Boolean = true,
)

data class MeasurementListItemUiModel(
    val measurement: BodyMeasurement,
    val derivedMetrics: DerivedMetrics,
)

class MeasurementListViewModel(
    measurementRepository: MeasurementRepository,
    profileRepository: ProfileRepository,
    settingsRepository: SettingsRepository,
    calculateMeasurementDerivedMetrics: CalculateMeasurementDerivedMetricsUseCase,
) : ViewModel() {
    val uiState: StateFlow<MeasurementListUiState> = combine(
        measurementRepository.observeAll(),
        profileRepository.profileFlow,
        settingsRepository.settingsFlow,
    ) { measurements, profile, settings ->
        val items = measurements.map { measurement ->
            MeasurementListItemUiModel(
                measurement = measurement,
                derivedMetrics = calculateMeasurementDerivedMetrics(profile, measurement),
            )
        }

        val orderedVisibleInTableMetrics = settings.visibleInTableOrdered(entries)

        MeasurementListUiState(
            latestMeasurement = items.firstOrNull(),
            previewMeasurements = items.take(PREVIEW_LIMIT),
            allMeasurements = items,
            hasMoreMeasurements = items.size > PREVIEW_LIMIT,
            visibleInTableMetrics = orderedVisibleInTableMetrics,
            isEmpty = items.isEmpty(),
            isLoading = false,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MeasurementListUiState(),
        )

    companion object {
        private const val PREVIEW_LIMIT = 20
    }
}

class MeasurementListViewModelFactory(
    private val measurementRepository: MeasurementRepository,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val calculateMeasurementDerivedMetrics: CalculateMeasurementDerivedMetricsUseCase,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MeasurementListViewModel(
            measurementRepository = measurementRepository,
            profileRepository = profileRepository,
            settingsRepository = settingsRepository,
            calculateMeasurementDerivedMetrics = calculateMeasurementDerivedMetrics,
        ) as T
    }
}
