package de.t_animal.opensourcebodytracker.data.settings

import de.t_animal.opensourcebodytracker.core.model.AnalysisDuration
import de.t_animal.opensourcebodytracker.core.model.BodyMetric

data class UiSettings(
    val analysisChartOrder: List<BodyMetric>,
    val analysisCollapsedCharts: Set<String>,
    val analysisDuration: AnalysisDuration,
)

fun defaultUiSettings() = UiSettings(
    analysisChartOrder = emptyList(),
    analysisCollapsedCharts = emptySet(),
    analysisDuration = AnalysisDuration.ThreeMonths,
)
