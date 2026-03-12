package de.t_animal.opensourcebodytracker.feature.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.core.model.AnalysisDuration
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.data.settings.UiSettings
import de.t_animal.opensourcebodytracker.data.settings.UiSettingsRepository
import de.t_animal.opensourcebodytracker.core.model.visibleInAnalysisOrdered
import de.t_animal.opensourcebodytracker.domain.metrics.CalculateMeasurementDerivedMetricsUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

@OptIn(FlowPreview::class)
class AnalysisViewModel(
    measurementRepository: MeasurementRepository,
    profileRepository: ProfileRepository,
    settingsRepository: SettingsRepository,
    private val uiSettingsRepository: UiSettingsRepository,
    private val calculateMeasurementDerivedMetrics: CalculateMeasurementDerivedMetricsUseCase,
    private val nowProvider: () -> Instant = { Instant.now() },
    private val zoneIdProvider: () -> ZoneId = { ZoneId.systemDefault() },
) : ViewModel() {

    // Null means "no in-progress drag; use stored order".
    private val customChartOrderOverride = MutableStateFlow<List<BodyMetric>?>(null)

    init {
        viewModelScope.launch {
            customChartOrderOverride.filterNotNull().debounce(500).collect { order ->
                uiSettingsRepository.updateSettings { it.copy(analysisChartOrder = order) }
            }
        }
    }

    val uiState: StateFlow<AnalysisUiState> = combine(
        measurementRepository.observeAll(),
        profileRepository.requiredProfileFlow,
        settingsRepository.settingsFlow,
        uiSettingsRepository.settingsFlow,
        customChartOrderOverride,
    ) { measurements, profile, settings, uiSettings, customChartOrderUntilSaved->
        val withDerived = measurements.map { measurement ->
            MeasurementWithDerived(
                measurement = measurement,
                derivedMetrics = calculateMeasurementDerivedMetrics(profile, measurement),
            )
        }

        val baseOrder = settings.visibleInAnalysisOrdered()
        val chartOrder = customChartOrderUntilSaved ?: uiSettings.analysisChartOrder
        val orderedMetrics = if (chartOrder.isEmpty()) {
            baseOrder
        } else {
            val baseSet = baseOrder.toSet()
            baseOrder.filter { it !in chartOrder.toSet() } + chartOrder.filter { it in baseSet }
        }

        val filteredItems = filterByDuration(
            items = withDerived,
            duration = uiSettings.analysisDuration,
            now = nowProvider(),
            zoneId = zoneIdProvider(),
        )

        AnalysisUiState(
            selectedDuration = uiSettings.analysisDuration,
            collapsedChartIds = uiSettings.analysisCollapsedCharts,
            metricCharts = buildMetricCharts(filteredItems, orderedMetrics),
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AnalysisUiState(),
    )

    fun onDurationSelected(duration: AnalysisDuration) {
        viewModelScope.launch {
            uiSettingsRepository.updateSettings { it.copy(analysisDuration = duration) }
        }
    }

    fun onChartOrderChanged(newOrder: List<BodyMetric>) {
        customChartOrderOverride.value = newOrder
        // debounced saved in constructor
    }

    fun onToggleCollapsed(chartId: String) {
        viewModelScope.launch {
            uiSettingsRepository.updateSettings {
                it.copy(analysisCollapsedCharts = it.analysisCollapsedCharts.toggle(chartId))
            }
        }
    }
}

private fun <T> Set<T>.toggle(item: T) = if (item in this) this - item else this + item

class AnalysisViewModelFactory(
    private val measurementRepository: MeasurementRepository,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val uiSettingsRepository: UiSettingsRepository,
    private val calculateMeasurementDerivedMetrics: CalculateMeasurementDerivedMetricsUseCase,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AnalysisViewModel(
            measurementRepository = measurementRepository,
            profileRepository = profileRepository,
            settingsRepository = settingsRepository,
            uiSettingsRepository = uiSettingsRepository,
            calculateMeasurementDerivedMetrics = calculateMeasurementDerivedMetrics,
        ) as T
    }
}
