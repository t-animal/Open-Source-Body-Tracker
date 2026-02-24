package de.t_animal.opensourcebodytracker.domain.measurements

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateFakeMeasurementsUseCaseTest {
    private val now = Instant.parse("2026-02-24T12:00:00Z")
    private val zoneId = ZoneId.systemDefault()

    @Test
    fun generateMeasurements_hasExpectedCountSpacingAndAnchors() {
        val useCase = GenerateFakeMeasurementsUseCase(
            measurementRepository = FakeMeasurementRepository(),
            nowProvider = { now },
        )

        val measurements = useCase.generateMeasurements(sex = Sex.Male)

        assertEquals(120, measurements.size)
        assertEquals(86.0, measurements.first().weightKg ?: 0.0, 0.0001)
        assertEquals(75.0, measurements.last().weightKg ?: 0.0, 0.0001)

        measurements.zipWithNext().forEach { (left, right) ->
            val leftDate = Instant.ofEpochMilli(left.dateEpochMillis).atZone(zoneId).toLocalDate()
            val rightDate = Instant.ofEpochMilli(right.dateEpochMillis).atZone(zoneId).toLocalDate()
            val days = ChronoUnit.DAYS.between(leftDate, rightDate)
            assertTrue(days == 6L || days == 7L)
        }
    }

    @Test
    fun generateMeasurements_firstYearLosesThenRegainsPartially() {
        val useCase = GenerateFakeMeasurementsUseCase(
            measurementRepository = FakeMeasurementRepository(),
            nowProvider = { now },
        )

        val measurements = useCase.generateMeasurements(sex = Sex.Male)
        val startDate = Instant.ofEpochMilli(measurements.first().dateEpochMillis).atZone(zoneId).toLocalDate()

        val startWeight = measurements.first().weightKg ?: 0.0
        val halfYearWeight = weightClosestTo(measurements, startDate.plusDays(182))
        val oneYearWeight = weightClosestTo(measurements, startDate.plusDays(365))

        assertTrue(halfYearWeight < startWeight)
        assertTrue(oneYearWeight > halfYearWeight)
        assertTrue(oneYearWeight < startWeight)
    }

    @Test
    fun generateMeasurements_isDeterministicForSameSeedAndNow() {
        val useCase = GenerateFakeMeasurementsUseCase(
            measurementRepository = FakeMeasurementRepository(),
            nowProvider = { now },
        )

        val first = useCase.generateMeasurements(sex = Sex.Female)
        val second = useCase.generateMeasurements(sex = Sex.Female)

        assertEquals(first, second)
    }

    private fun weightClosestTo(
        measurements: List<BodyMeasurement>,
        targetDate: java.time.LocalDate,
    ): Double {
        return (measurements.minByOrNull { measurement ->
            val measurementDate = Instant.ofEpochMilli(measurement.dateEpochMillis).atZone(zoneId).toLocalDate()
            abs(ChronoUnit.DAYS.between(measurementDate, targetDate))
        }?.weightKg) ?: error("Expected at least one measurement")
    }
}

private class FakeMeasurementRepository : MeasurementRepository {
    override fun observeAll(): Flow<List<BodyMeasurement>> = flowOf(emptyList())

    override suspend fun getById(id: Long): BodyMeasurement? = null

    override suspend fun insert(measurement: BodyMeasurement): Long = 0

    override suspend fun update(measurement: BodyMeasurement) = Unit

    override suspend fun replaceAll(measurements: List<BodyMeasurement>) = Unit
}