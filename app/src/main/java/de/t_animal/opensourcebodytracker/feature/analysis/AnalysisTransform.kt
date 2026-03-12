package de.t_animal.opensourcebodytracker.feature.analysis

import de.t_animal.opensourcebodytracker.core.model.AnalysisDuration
import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.DerivedMetrics
import java.time.Instant
import java.time.ZoneId

internal data class MeasurementWithDerived(
    val measurement: BodyMeasurement,
    val derivedMetrics: DerivedMetrics,
)

internal fun filterByDuration(
    items: List<MeasurementWithDerived>,
    duration: AnalysisDuration,
    now: Instant,
    zoneId: ZoneId,
): List<MeasurementWithDerived> {
    if (duration == AnalysisDuration.All) {
        return items.filter { it.measurement.dateEpochMillis <= now.toEpochMilli() }
    }

    val nowDateTime = now.atZone(zoneId)
    val start = when (duration) {
        AnalysisDuration.OneMonth -> nowDateTime.minusMonths(1)
        AnalysisDuration.ThreeMonths -> nowDateTime.minusMonths(3)
        AnalysisDuration.SixMonths -> nowDateTime.minusMonths(6)
        AnalysisDuration.OneYear -> nowDateTime.minusYears(1)
        AnalysisDuration.All -> nowDateTime
    }.toInstant().toEpochMilli()

    val end = now.toEpochMilli()

    return items.filter {
        val timestamp = it.measurement.dateEpochMillis
        timestamp in start..end
    }
}

internal fun buildMetricCharts(
    items: List<MeasurementWithDerived>,
    definitions: List<BodyMetric> = BodyMetric.entries,
): List<AnalysisMetricChartUiModel> {
    val sortedItems = items.sortedBy { it.measurement.dateEpochMillis }

    return definitions.map { definition ->
        val points = sortedItems.mapNotNull { item ->
            val value = definition.valueSelector(item.measurement, item.derivedMetrics) ?: return@mapNotNull null
            AnalysisChartPoint(epochMillis = item.measurement.dateEpochMillis, value = value)
        }

        AnalysisMetricChartUiModel(
            definition = definition,
            points = points,
            yAxisRange = calculateYAxisRange(points),
        )
    }
}

internal fun calculateYAxisRange(points: List<AnalysisChartPoint>): AnalysisYAxisRange? {
    if (points.isEmpty()) return null

    val minValue = points.minOf { it.value }
    val maxValue = points.maxOf { it.value }

    val padding = if (minValue == maxValue) {
        maxOf(kotlin.math.abs(minValue) * 0.05, 0.5)
    } else {
        (maxValue - minValue) * 0.05
    }

    return AnalysisYAxisRange(
        min = minValue - padding,
        max = maxValue + padding,
    )
}
