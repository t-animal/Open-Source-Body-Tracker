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
)

data class AnalysisMetricChartUiModel(
    val definition: BodyMetric,
    val points: List<AnalysisChartPoint>,
    val yAxisRange: AnalysisYAxisRange?,
)

data class AnalysisUiState(
    val selectedDuration: AnalysisDuration = AnalysisDuration.ThreeMonths,
    val metricCharts: List<AnalysisMetricChartUiModel> = emptyList(),
    val collapsedChartIds: Set<String> = emptySet(),
    val unitSystem: UnitSystem = UnitSystem.Metric,
    val isLoading: Boolean = true,
)
