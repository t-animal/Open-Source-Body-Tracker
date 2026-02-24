package de.t_animal.opensourcebodytracker.domain.measurements

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Random
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

class GenerateFakeMeasurementsUseCase(
    private val measurementRepository: MeasurementRepository,
    private val nowProvider: () -> Instant = { Instant.now() },
) {
    suspend operator fun invoke(
        profile: UserProfile?,
        leanBodyWeightKg: Double,
        minFatBodyWeightKg: Double,
        maxFatBodyWeightKg: Double,
    ) {
        measurementRepository.replaceAll(
            generateMeasurements(
                sex = profile?.sex,
                heightCm = profile?.heightCm?.toDouble(),
                dateOfBirthEpochMillis = profile?.dateOfBirthEpochMillis,
                leanBodyWeightKg = leanBodyWeightKg,
                minFatBodyWeightKg = minFatBodyWeightKg,
                maxFatBodyWeightKg = maxFatBodyWeightKg,
            ),
        )
    }

    internal fun generateMeasurements(
        sex: Sex?,
        heightCm: Double?,
        dateOfBirthEpochMillis: Long?,
        leanBodyWeightKg: Double,
        minFatBodyWeightKg: Double,
        maxFatBodyWeightKg: Double,
        count: Int = 120,
        seed: Long = 20_240_224L,
    ): List<BodyMeasurement> {
        require(count > 1) { "count must be greater than 1" }
        require(leanBodyWeightKg > 0.0) { "leanBodyWeightKg must be greater than 0" }
        require(minFatBodyWeightKg >= 0.0) { "minFatBodyWeightKg must be at least 0" }
        require(maxFatBodyWeightKg >= 0.0) { "maxFatBodyWeightKg must be at least 0" }

        val random = Random(seed)
        val effectiveSex = sex ?: Sex.Male
        val effectiveHeightCm = (heightCm ?: 175.0).coerceAtLeast(120.0)
        val zoneId = ZoneId.systemDefault()
        val today = nowProvider().atZone(zoneId).toLocalDate()
        val minFatKg = min(minFatBodyWeightKg, maxFatBodyWeightKg)
        val maxFatKg = max(minFatBodyWeightKg, maxFatBodyWeightKg)

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
        var previousNeckCm: Double? = null

        return dates.mapIndexed { index, currentDate ->
            val daysFromStart = ChronoUnit.DAYS.between(startDate, currentDate)
            val fatKg = interpolateFatKg(
                daysFromStart = daysFromStart,
                minFatKg = minFatKg,
                maxFatKg = maxFatKg,
                random = random,
            )
            val weightKg = roundToOneDecimal(leanBodyWeightKg + fatKg)
            val targetBodyFatPercent = (fatKg / (leanBodyWeightKg + fatKg)) * 100.0
            val ageYears = dateOfBirthEpochMillis?.let {
                calculateAgeYearsAtDate(
                    dateOfBirthEpochMillis = it,
                    atDate = currentDate,
                    zoneId = zoneId,
                )
            } ?: 30

            val circumferences = computeCircumferences(
                sex = effectiveSex,
                heightCm = effectiveHeightCm,
                targetBodyFatPercent = targetBodyFatPercent,
                weightKg = weightKg,
                random = random,
                previousNeckCm = previousNeckCm,
            )
            previousNeckCm = circumferences.neckCm
            val skinfolds = computeSkinfolds(
                sex = effectiveSex,
                ageYears = ageYears,
                targetBodyFatPercent = targetBodyFatPercent,
                circumferences = circumferences,
                random = random,
            )

            BodyMeasurement(
                id = 0,
                dateEpochMillis = currentDate.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                weightKg = weightKg,
                neckCircumferenceCm = roundToOneDecimal(circumferences.neckCm),
                chestCircumferenceCm = roundToOneDecimal(circumferences.chestCm),
                waistCircumferenceCm = roundToOneDecimal(circumferences.waistCm),
                abdomenCircumferenceCm = roundToOneDecimal(circumferences.abdomenCm),
                hipCircumferenceCm = roundToOneDecimal(circumferences.hipCm),
                chestSkinfoldMm = roundToOneDecimal(skinfolds.chestMm),
                abdomenSkinfoldMm = roundToOneDecimal(skinfolds.abdomenMm),
                thighSkinfoldMm = roundToOneDecimal(skinfolds.thighMm),
                tricepsSkinfoldMm = roundToOneDecimal(skinfolds.tricepsMm),
                suprailiacSkinfoldMm = roundToOneDecimal(skinfolds.suprailiacMm),
            )
        }
    }
}

private data class GeneratedCircumferences(
    val neckCm: Double,
    val chestCm: Double,
    val waistCm: Double,
    val abdomenCm: Double,
    val hipCm: Double,
)

private data class GeneratedSkinfolds(
    val chestMm: Double,
    val abdomenMm: Double,
    val thighMm: Double,
    val tricepsMm: Double,
    val suprailiacMm: Double,
)

private fun interpolateFatKg(
    daysFromStart: Long,
    minFatKg: Double,
    maxFatKg: Double,
    random: Random,
): Double {
    val lossDays = 182.0
    val gainDays = 122.0
    val cycleDays = lossDays + gainDays
    val dayInCycle = ((daysFromStart % cycleDays.toLong()) + cycleDays.toLong()) % cycleDays.toLong()
    val phaseDay = dayInCycle.toDouble()

    val interpolatedFatKg = if (phaseDay <= lossDays) {
        lerp(start = maxFatKg, end = minFatKg, progress = phaseDay / lossDays)
    } else {
        lerp(start = minFatKg, end = maxFatKg, progress = (phaseDay - lossDays) / gainDays)
    }

    val noiseKg = random.nextDouble(-1.0, 1.0)
    return (interpolatedFatKg + noiseKg).coerceIn(minFatKg, maxFatKg)
}

private fun computeCircumferences(
    sex: Sex,
    heightCm: Double,
    targetBodyFatPercent: Double,
    weightKg: Double,
    random: Random,
    previousNeckCm: Double?,
): GeneratedCircumferences {
    val navy = computeNavyInversion(sex = sex, targetBodyFatPercent = targetBodyFatPercent, heightCm = heightCm)
    val defaultWaist = roundToOneDecimal(
        (if (sex == Sex.Female) 0.43 * weightKg + 50.0 else 0.40 * weightKg + 48.0)
            .coerceIn(if (sex == Sex.Female) 50.0 else 60.0, 150.0),
    )

    val neckTargetCm = if (navy != null) {
        (if (sex == Sex.Female) 33.5 else 38.0) + random.nextGaussian() * 0.2
    } else {
        defaultWaist - if (sex == Sex.Female) 24.0 else 20.0
    }.coerceIn(26.0, 50.0)

    val neckSmoothedCm = previousNeckCm?.let { previous ->
        val ema = previous + ((neckTargetCm - previous) * 0.75)
        ema.coerceIn(previous - 0.2, previous + 0.2)
    } ?: neckTargetCm

    val neck = roundToOneDecimal(neckSmoothedCm)

    val waist = roundToOneDecimal(
        if (navy != null) {
            if (sex == Sex.Male) {
                (neck + navy).coerceIn(60.0, 150.0)
            } else {
                val hipBase = (if (sex == Sex.Female) 0.53 * weightKg + 55.0 else 0.46 * weightKg + 54.0)
                    .coerceIn(65.0, 160.0)
                (navy - hipBase + neck).coerceIn(50.0, 150.0)
            }
        } else {
            defaultWaist
        },
    )

    val hip = roundToOneDecimal(
        if (sex == Sex.Female) {
            if (navy != null) {
                (navy - waist + neck).coerceIn(65.0, 160.0)
            } else {
                waist + 20.0
            }
        } else {
            (waist + 6.0).coerceIn(62.0, 150.0)
        },
    )

    val abdomen = roundToOneDecimal((waist + 2.0 + random.nextGaussian() * 1.5).coerceAtLeast(58.0))
    val chest = roundToOneDecimal(
        (waist + if (sex == Sex.Female) 4.0 else 12.0) + random.nextGaussian() * 1.5
            .coerceAtLeast(65.0),
    )

    return GeneratedCircumferences(
        neckCm = neck,
        chestCm = chest,
        waistCm = waist,
        abdomenCm = abdomen,
        hipCm = hip,
    )
}

private fun computeNavyInversion(
    sex: Sex,
    targetBodyFatPercent: Double,
    heightCm: Double,
): Double? {
    if (heightCm <= 0.0) {
        return null
    }

    return when (sex) {
        Sex.Male -> {
            val exponent = (targetBodyFatPercent + (70.041 * log10(heightCm)) - 30.30) / 86.010
            10.0.pow(exponent)
        }

        Sex.Female -> {
            val exponent = (targetBodyFatPercent + (97.684 * log10(heightCm)) + 104.912) / 163.205
            10.0.pow(exponent)
        }
    }
}

private fun computeSkinfolds(
    sex: Sex,
    ageYears: Int,
    targetBodyFatPercent: Double,
    circumferences: GeneratedCircumferences,
    random: Random,
): GeneratedSkinfolds {
    val targetDensity = 495.0 / (targetBodyFatPercent + 450.0)
    val sum3 = solveSkinfoldSum3(sex = sex, ageYears = ageYears, targetDensity = targetDensity)

    val thighBase = ((circumferences.waistCm / 8.5) + random.nextGaussian() * 0.7).coerceIn(3.0, 60.0)

    return if (sum3 != null) {
        val jitter = random.nextGaussian() * 0.5
        when (sex) {
            Sex.Male -> {
                val chest = (sum3 * 0.24 + jitter).coerceIn(2.0, 70.0)
                val abdomen = (sum3 * 0.46 - jitter*1/3).coerceIn(2.0, 70.0)
                val thigh = (sum3 * 0.30 - jitter*2/3).coerceIn(2.0, 70.0)
                GeneratedSkinfolds(
                    chestMm = chest,
                    abdomenMm = abdomen,
                    thighMm = thigh,
                    tricepsMm = (chest + 2.0).coerceIn(2.0, 70.0),
                    suprailiacMm = (abdomen - 1.0).coerceIn(2.0, 70.0),
                )
            }

            Sex.Female -> {
                val triceps = (sum3 * 0.30 + jitter).coerceIn(2.0, 70.0)
                val suprailiac = (sum3 * 0.32 - jitter*1/3).coerceIn(2.0, 70.0)
                val thigh = (sum3 * 0.8 - jitter*2/3).coerceIn(2.0, 70.0)
                GeneratedSkinfolds(
                    chestMm = (triceps - 4.0).coerceIn(2.0, 70.0),
                    abdomenMm = (suprailiac + 2.0).coerceIn(2.0, 70.0),
                    thighMm = thigh,
                    tricepsMm = triceps,
                    suprailiacMm = suprailiac,
                )
            }
        }
    } else {
        when (sex) {
            Sex.Male -> {
                GeneratedSkinfolds(
                    chestMm = (thighBase - 3.0).coerceIn(2.0, 70.0),
                    abdomenMm = (thighBase + 4.0).coerceIn(2.0, 70.0),
                    thighMm = thighBase,
                    tricepsMm = (thighBase - 1.0).coerceIn(2.0, 70.0),
                    suprailiacMm = (thighBase + 2.0).coerceIn(2.0, 70.0),
                )
            }

            Sex.Female -> {
                GeneratedSkinfolds(
                    chestMm = (thighBase - 2.0).coerceIn(2.0, 70.0),
                    abdomenMm = (thighBase + 3.0).coerceIn(2.0, 70.0),
                    thighMm = thighBase,
                    tricepsMm = (thighBase + 1.0).coerceIn(2.0, 70.0),
                    suprailiacMm = (thighBase + 2.0).coerceIn(2.0, 70.0),
                )
            }
        }
    }
}

private fun solveSkinfoldSum3(
    sex: Sex,
    ageYears: Int,
    targetDensity: Double,
): Double? {
    val coefficients = when (sex) {
        Sex.Male -> QuadCoefficients(
            a = 0.0000016,
            b = -0.0008267,
            c = 1.10938 - (0.0002574 * ageYears) - targetDensity,
        )

        Sex.Female -> QuadCoefficients(
            a = 0.0000023,
            b = -0.0009929,
            c = 1.0994921 - (0.0001392 * ageYears) - targetDensity,
        )
    }

    val discriminant = (coefficients.b * coefficients.b) - (4.0 * coefficients.a * coefficients.c)
    if (discriminant < 0.0) {
        return null
    }

    val sqrtDisc = sqrt(discriminant)
    val rootSmall = ((-coefficients.b) - sqrtDisc) / (2.0 * coefficients.a)
    val rootLarge = ((-coefficients.b) + sqrtDisc) / (2.0 * coefficients.a)

    val chosen = listOf(rootSmall, rootLarge)
        .filter { it.isFinite() && it > 0.0 }
        .minOrNull()
        ?: return null

    return chosen.coerceIn(10.0, 150.0)
}

private data class QuadCoefficients(
    val a: Double,
    val b: Double,
    val c: Double,
)

private fun calculateAgeYearsAtDate(
    dateOfBirthEpochMillis: Long,
    atDate: LocalDate,
    zoneId: ZoneId,
): Int {
    val dateOfBirth = Instant.ofEpochMilli(dateOfBirthEpochMillis)
        .atZone(zoneId)
        .toLocalDate()

    if (atDate.isBefore(dateOfBirth)) {
        return 18
    }

    return Period.between(dateOfBirth, atDate).years.coerceAtLeast(18)
}

private fun lerp(start: Double, end: Double, progress: Double): Double {
    return start + ((end - start) * progress)
}

private fun roundToOneDecimal(value: Double): Double {
    return round(value * 10.0) / 10.0
}
