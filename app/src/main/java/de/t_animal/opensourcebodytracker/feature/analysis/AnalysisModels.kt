package de.t_animal.opensourcebodytracker.feature.analysis

import de.t_animal.opensourcebodytracker.core.model.BodyMetric

enum class AnalysisDuration(
    val label: String,
) {
    OneMonth("1M"),
    ThreeMonths("3M"),
    SixMonths("6M"),
    OneYear("1Y"),
    All("All"),
}

data class AnalysisChartPoint(
    val epochMillis: Long,
    val value: Double,
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
    val isLoading: Boolean = true,
)
