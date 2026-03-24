package de.t_animal.opensourcebodytracker.feature.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import de.t_animal.opensourcebodytracker.core.model.AnalysisDuration
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.GeneralSettingsRepository
import de.t_animal.opensourcebodytracker.data.settings.MeasurementSettingsRepository
import de.t_animal.opensourcebodytracker.data.uisettings.UiSettingsRepository
import de.t_animal.opensourcebodytracker.domain.metrics.CalculateAndRateDerivedMetricsUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock

@OptIn(FlowPreview::class)
@HiltViewModel
class AnalysisViewModel @Inject constructor(
    measurementRepository: MeasurementRepository,
    profileRepository: ProfileRepository,
    measurementSettingsRepository: MeasurementSettingsRepository,
    generalSettingsRepository: GeneralSettingsRepository,
    private val uiSettingsRepository: UiSettingsRepository,
    private val calculateMeasurementDerivedMetrics: CalculateAndRateDerivedMetricsUseCase,
    private val clock: Clock,
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
        measurementSettingsRepository.settingsFlow,
        uiSettingsRepository.settingsFlow,
        combine(customChartOrderOverride, generalSettingsRepository.settingsFlow, ::Pair),
    ) { measurements, profile, settings, uiSettings, (customChartOrderUntilSaved, generalSettings) ->
        val withDerived = measurements.map { measurement ->
            MeasurementWithDerived(
                measurement = measurement,
                derivedMetrics = calculateMeasurementDerivedMetrics(profile, measurement).metrics,
            )
        }

        val baseOrder = settings.visibleInAnalysisOrdered
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
            now = clock.instant(),
            zoneId = clock.zone,
        )

        AnalysisUiState.Loaded(
            selectedDuration = uiSettings.analysisDuration,
            collapsedChartIds = uiSettings.analysisCollapsedCharts,
            metricCharts = buildMetricCharts(filteredItems, orderedMetrics),
            unitSystem = generalSettings.unitSystem,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AnalysisUiState.Loading,
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
