package de.t_animal.opensourcebodytracker.feature.analysis

import de.t_animal.opensourcebodytracker.core.model.AnalysisDuration
import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.DerivedMetrics
import java.time.Instant
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

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
            AnalysisChartPoint(
                epochMillis = item.measurement.dateEpochMillis,
                value = value,
                note = item.measurement.note,
            )
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

    // For a flat line, manufacture a small range so the chart isn't empty.
    val rawRange = if (minValue == maxValue) {
        maxOf(abs(minValue) * 0.1, 1.0)
    } else {
        maxValue - minValue
    }

    // Pick a "nice" step size by rounding a rough estimate up to the nearest 1, 2, or 5
    // times a power of ten (e.g. 0.2, 5, 20, 500, …). This ensures that axis bounds
    // (which are snapped to a multiple of the step below) land on human-readable values
    // regardless of how many ticks Vico actually places.
    //
    // magnitude is the largest power of ten that fits inside roughStep — it gives us the
    // order of magnitude to work with (e.g. roughStep = 3.7 → magnitude = 1, so we
    // search among 1, 2, 5, 10 and pick 5).
    val roughStep = rawRange / 4.0
    val magnitude = 10.0.pow(floor(log10(roughStep)))
    val niceFactor = when {
        roughStep / magnitude <= 1.0 -> 1.0
        roughStep / magnitude <= 2.0 -> 2.0
        roughStep / magnitude <= 5.0 -> 5.0
        else -> 10.0
    }
    val step = niceFactor * magnitude

    // Snap the axis bounds outward to the nearest step multiple so tick labels are whole numbers.
    return AnalysisYAxisRange(
        min = floor(minValue / step) * step,
        max = ceil(maxValue / step) * step,
        step = step,
    )
}
