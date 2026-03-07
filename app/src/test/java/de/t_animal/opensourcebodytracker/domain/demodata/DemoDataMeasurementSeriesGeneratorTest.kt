package de.t_animal.opensourcebodytracker.domain.demodata

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.Sex
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoDataMeasurementSeriesGeneratorTest {
    private val now = Instant.parse("2026-02-24T12:00:00Z")
    private val zoneId = ZoneId.systemDefault()

    @Test
    fun generateMeasurements_hasExpectedCountSpacingAndAnchors() {
        val generator = DemoDataMeasurementSeriesGenerator(nowProvider = { now })

        val measurements = generator.generateMeasurements(
            sex = Sex.Male,
            heightCm = 178.0,
            dateOfBirthEpochMillis = null,
            leanBodyWeightKg = 67.0,
            minFatBodyWeightKg = 8.0,
            maxFatBodyWeightKg = 20.0,
        )

        assertEquals(120, measurements.size)
        assertTrue((measurements.first().weightKg ?: 0.0) in 75.0..90.0)
        assertTrue((measurements.last().weightKg ?: 0.0) in 75.0..90.0)

        measurements.zipWithNext().forEach { (left, right) ->
            val leftDate = Instant.ofEpochMilli(left.dateEpochMillis).atZone(zoneId).toLocalDate()
            val rightDate = Instant.ofEpochMilli(right.dateEpochMillis).atZone(zoneId).toLocalDate()
            val days = ChronoUnit.DAYS.between(leftDate, rightDate)
            assertTrue(days == 6L || days == 7L)
        }

        measurements.forEach { measurement ->
            val weight = measurement.weightKg ?: 0.0
            assertTrue(weight in 75.0..90.0)
            assertTrue((measurement.waistCircumferenceCm ?: 0.0) > (measurement.neckCircumferenceCm ?: 0.0))
        }
    }

    @Test
    fun generateMeasurements_firstCycleLosesThenRegains() {
        val generator = DemoDataMeasurementSeriesGenerator(nowProvider = { now })

        val measurements = generator.generateMeasurements(
            sex = Sex.Male,
            heightCm = 178.0,
            dateOfBirthEpochMillis = null,
            leanBodyWeightKg = 67.0,
            minFatBodyWeightKg = 8.0,
            maxFatBodyWeightKg = 20.0,
        )
        val startDate = Instant.ofEpochMilli(measurements.first().dateEpochMillis).atZone(zoneId).toLocalDate()

        val startWeight = measurements.first().weightKg ?: 0.0
        val sixMonthWeight = weightClosestTo(measurements, startDate.plusDays(182))
        val tenMonthWeight = weightClosestTo(measurements, startDate.plusDays(304))

        assertTrue(sixMonthWeight < startWeight)
        assertTrue(tenMonthWeight > sixMonthWeight)
    }

    @Test
    fun generateMeasurements_isDeterministicForSameSeedAndNow() {
        val generator = DemoDataMeasurementSeriesGenerator(nowProvider = { now })

        val first = generator.generateMeasurements(
            sex = Sex.Female,
            heightCm = 165.0,
            dateOfBirthEpochMillis = null,
            leanBodyWeightKg = 52.0,
            minFatBodyWeightKg = 10.0,
            maxFatBodyWeightKg = 24.0,
        )
        val second = generator.generateMeasurements(
            sex = Sex.Female,
            heightCm = 165.0,
            dateOfBirthEpochMillis = null,
            leanBodyWeightKg = 52.0,
            minFatBodyWeightKg = 10.0,
            maxFatBodyWeightKg = 24.0,
        )

        assertEquals(first, second)
    }

    private fun weightClosestTo(
        measurements: List<BodyMeasurement>,
        targetDate: LocalDate,
    ): Double {
        return (measurements.minByOrNull { measurement ->
            val measurementDate = Instant.ofEpochMilli(measurement.dateEpochMillis).atZone(zoneId).toLocalDate()
            abs(ChronoUnit.DAYS.between(measurementDate, targetDate))
        }?.weightKg) ?: error("Expected at least one measurement")
    }
}
