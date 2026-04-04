package de.t_animal.opensourcebodytracker.feature.analysis

import de.t_animal.opensourcebodytracker.core.model.AnalysisDuration
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.UnitSystem

data class AnalysisChartPoint(
    val epochMillis: Long,
    val value: Double,
    val note: String? = null,
)

data class AnalysisYAxisRange(
    val min: Double,
    val max: Double,
    val step: Double = 1.0,
)

data class AnalysisMetricChartUiModel(
    val definition: BodyMetric,
    val points: List<AnalysisChartPoint>,
    val yAxisRange: AnalysisYAxisRange?,
)

sealed interface AnalysisUiState {
    data object Loading : AnalysisUiState

    data class Loaded(
        val selectedDuration: AnalysisDuration,
        val metricCharts: List<AnalysisMetricChartUiModel>,
        val collapsedChartIds: Set<String>,
        val unitSystem: UnitSystem,
    ) : AnalysisUiState
}
