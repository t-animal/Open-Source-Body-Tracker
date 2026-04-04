package de.t_animal.opensourcebodytracker.feature.analysis

import de.t_animal.opensourcebodytracker.core.model.AnalysisDuration
import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.DerivedMetrics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

class AnalysisTransformTest {
    @Test
    fun filterByDuration_threeMonths_filtersOutsideWindow() {
        val now = Instant.parse("2026-02-23T00:00:00Z")
        val zoneId = ZoneOffset.UTC
        val items = listOf(
            measurementWithDerived(id = 1, date = "2025-10-20T00:00:00Z", weight = 90.0),
            measurementWithDerived(id = 2, date = "2025-11-25T00:00:00Z", weight = 89.0),
            measurementWithDerived(id = 3, date = "2026-01-01T00:00:00Z", weight = 88.0),
        )

        val filtered = filterByDuration(
            items = items,
            duration = AnalysisDuration.ThreeMonths,
            now = now,
            zoneId = zoneId,
        )

        assertEquals(listOf(2L, 3L), filtered.map { it.measurement.id })
    }

    @Test
    fun buildMetricCharts_includesEveryMeasurementAndDerivedMetricDefinition() {
        val items = listOf(
            measurementWithDerived(
                id = 1,
                date = "2026-01-10T00:00:00Z",
                weight = 82.0,
                bmi = 25.0,
            ),
        )

        val charts = buildMetricCharts(items)

        assertEquals(BodyMetric.entries.size, charts.size)
        assertEquals(
            BodyMetric.entries.map { it.id },
            charts.map { it.definition.id },
        )
    }

    @Test
    fun calculateYAxisRange_addsPaddingForFlatSeries() {
        val points = listOf(
            AnalysisChartPoint(epochMillis = 1_736_380_800_000, value = 80.0),
            AnalysisChartPoint(epochMillis = 1_736_467_200_000, value = 80.0),
        )

        val range = calculateYAxisRange(points)

        assertNotNull(range)
        val nonNullRange = checkNotNull(range)
        assertEquals(76.0, nonNullRange.min, 0.0001)
        assertEquals(84.0, nonNullRange.max, 0.0001)
    }

    @Test
    fun calculateYAxisRange_snapsToNiceStepBoundsForNonFlatSeries() {
        val points = listOf(
            AnalysisChartPoint(epochMillis = 1_736_380_800_000, value = 70.0),
            AnalysisChartPoint(epochMillis = 1_736_467_200_000, value = 90.0),
        )

        val range = calculateYAxisRange(points)

        assertNotNull(range)
        val nonNullRange = checkNotNull(range)
        // range=20 → step=5; floor(70/5)*5=70, ceil(90/5)*5=90
        assertEquals(70.0, nonNullRange.min, 0.0001)
        assertEquals(90.0, nonNullRange.max, 0.0001)
    }

    @Test
    fun buildMetricCharts_keepsPointsChronologicallyAscending() {
        val items = listOf(
            measurementWithDerived(id = 1, date = "2026-02-20T00:00:00Z", weight = 81.5),
            measurementWithDerived(id = 2, date = "2026-01-20T00:00:00Z", weight = 82.0),
        )

        val weightChart = buildMetricCharts(items).first { it.definition.id == "weight_kg" }

        assertTrue(weightChart.points[0].epochMillis < weightChart.points[1].epochMillis)
    }

    private fun measurementWithDerived(
        id: Long,
        date: String,
        weight: Double,
        bmi: Double? = null,
    ): MeasurementWithDerived {
        val measurement = BodyMeasurement(
            id = id,
            dateEpochMillis = Instant.parse(date).toEpochMilli(),
            weightKg = weight,
            neckCircumferenceCm = 40.0,
            chestCircumferenceCm = 100.0,
            waistCircumferenceCm = 90.0,
            abdomenCircumferenceCm = 92.0,
            hipCircumferenceCm = 100.0,
            chestSkinfoldMm = 12.0,
            abdomenSkinfoldMm = 20.0,
            thighSkinfoldMm = 16.0,
            tricepsSkinfoldMm = 18.0,
            suprailiacSkinfoldMm = 17.0,
        )
        val derived = DerivedMetrics(
            bmi = bmi,
            navyBodyFatPercent = 20.0,
            skinfold3SiteBodyFatPercent = 18.0,
            waistHipRatio = 0.9,
            waistHeightRatio = 0.5,
        )
        return MeasurementWithDerived(measurement = measurement, derivedMetrics = derived)
    }
}
