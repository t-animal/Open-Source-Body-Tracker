package de.t_animal.opensourcebodytracker.domain.measurements

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Random
import kotlin.math.round

class GenerateFakeMeasurementsUseCase(
    private val measurementRepository: MeasurementRepository,
    private val nowProvider: () -> Instant = { Instant.now() },
) {
    suspend operator fun invoke(sex: Sex?) {
        measurementRepository.replaceAll(generateMeasurements(sex = sex))
    }

    internal fun generateMeasurements(
        sex: Sex?,
        count: Int = 120,
        seed: Long = 20_240_224L,
    ): List<BodyMeasurement> {
        require(count > 1) { "count must be greater than 1" }

        val random = Random(seed)
        val zoneId = ZoneId.systemDefault()
        val today = nowProvider().atZone(zoneId).toLocalDate()

        val reverseDates = ArrayList<LocalDate>(count)
        var date = today
        repeat(count) { index ->
            reverseDates += date
            if (index < count - 1) {
                date = date.minusDays(if (random.nextBoolean()) 6L else 7L)
            }
        }
        val dates = reverseDates.asReversed()

        val startDate = dates.first()
        val endDate = dates.last()
        val totalDays = ChronoUnit.DAYS.between(startDate, endDate).coerceAtLeast(1)

        val tempSeries = dates.map { currentDate ->
            val daysFromStart = ChronoUnit.DAYS.between(startDate, currentDate)
            val cycleProgress = (daysFromStart % 365.0) / 365.0
            val cycleComponent = if (cycleProgress <= 0.5) {
                -(cycleProgress / 0.5)
            } else {
                -1.0 + 0.5 * ((cycleProgress - 0.5) / 0.5)
            }
            val seasonal = cycleComponent * 6.0
            val noise = random.nextGaussian() * 0.22
            seasonal + noise
        }

        val tempStart = tempSeries.first()
        val tempEnd = tempSeries.last()

        return dates.mapIndexed { index, currentDate ->
            val daysFromStart = ChronoUnit.DAYS.between(startDate, currentDate)
            val progress = daysFromStart.toDouble() / totalDays.toDouble()

            val baselineWeight = lerp(start = 86.0, end = 75.0, progress = progress)
            val endpointAlignedTemp = tempSeries[index] - lerp(
                start = tempStart,
                end = tempEnd,
                progress = progress,
            )
            val weightKg = roundToOneDecimal((baselineWeight + endpointAlignedTemp).coerceIn(60.0, 96.0))

            val waist = roundToOneDecimal((65.0 + weightKg * 0.36 + random.nextGaussian() * 1.1).coerceAtLeast(55.0))
            val neck = roundToOneDecimal((30.0 + weightKg * 0.12 + random.nextGaussian() * 0.6).coerceAtLeast(26.0))
            val chest = roundToOneDecimal((72.0 + weightKg * 0.33 + random.nextGaussian() * 1.2).coerceAtLeast(65.0))
            val abdomen = roundToOneDecimal((waist + 2.0 + random.nextGaussian() * 1.1).coerceAtLeast(58.0))

            val hipBaseFactor = if (sex == Sex.Female) 1.10 else 1.03
            val hip = roundToOneDecimal((waist * hipBaseFactor + random.nextGaussian() * 1.0).coerceAtLeast(62.0))

            val adiposity = ((weightKg - 62.0) / 30.0).coerceIn(0.0, 1.0)
            val chestSkinfold = roundToOneDecimal(
                (if (sex == Sex.Female) 12.0 else 8.0) + adiposity * 14.0 + random.nextGaussian() * 1.1,
            ).coerceAtLeast(3.0)
            val abdomenSkinfold = roundToOneDecimal(
                (if (sex == Sex.Female) 15.0 else 11.0) + adiposity * 18.0 + random.nextGaussian() * 1.3,
            ).coerceAtLeast(3.0)
            val thighSkinfold = roundToOneDecimal(
                (if (sex == Sex.Female) 17.0 else 12.0) + adiposity * 17.0 + random.nextGaussian() * 1.3,
            ).coerceAtLeast(3.0)
            val tricepsSkinfold = roundToOneDecimal(
                (if (sex == Sex.Female) 16.0 else 9.0) + adiposity * 15.0 + random.nextGaussian() * 1.2,
            ).coerceAtLeast(3.0)
            val suprailiacSkinfold = roundToOneDecimal(
                (if (sex == Sex.Female) 16.0 else 10.0) + adiposity * 16.0 + random.nextGaussian() * 1.2,
            ).coerceAtLeast(3.0)

            BodyMeasurement(
                id = 0,
                dateEpochMillis = currentDate.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                weightKg = weightKg,
                neckCircumferenceCm = neck,
                chestCircumferenceCm = chest,
                waistCircumferenceCm = waist,
                abdomenCircumferenceCm = abdomen,
                hipCircumferenceCm = hip,
                chestSkinfoldMm = chestSkinfold,
                abdomenSkinfoldMm = abdomenSkinfold,
                thighSkinfoldMm = thighSkinfold,
                tricepsSkinfoldMm = tricepsSkinfold,
                suprailiacSkinfoldMm = suprailiacSkinfold,
            )
        }
    }
}

private fun lerp(start: Double, end: Double, progress: Double): Double {
    return start + ((end - start) * progress)
}

private fun roundToOneDecimal(value: Double): Double {
    return round(value * 10.0) / 10.0
}