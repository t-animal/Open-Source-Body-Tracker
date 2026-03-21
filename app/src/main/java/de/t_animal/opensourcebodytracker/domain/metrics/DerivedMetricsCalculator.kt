package de.t_animal.opensourcebodytracker.domain.metrics

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.DerivedMetrics
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.log10

class DerivedMetricsCalculator @Inject constructor() {
    fun calculate(
        profile: UserProfile,
        measurement: BodyMeasurement,
    ): DerivedMetrics {
        val heightCm = profile.heightCm.toDouble()
        val weightKg = measurement.weightKg
        val waistCm = measurement.waistCircumferenceCm
        val hipCm = measurement.hipCircumferenceCm
        val neckCm = measurement.neckCircumferenceCm
        val ageYears = calculateAgeYearsAtMeasurement(
            dateOfBirth = profile.dateOfBirth,
            measurementEpochMillis = measurement.dateEpochMillis,
        )

        val bmi = calculateBmi(heightCm = heightCm, weightKg = weightKg)
        val navyBodyFatPercent = calculateNavyBodyFatPercent(
            sex = profile.sex,
            heightCm = heightCm,
            waistCm = waistCm,
            hipCm = hipCm,
            neckCm = neckCm,
        )
        val skinfold3SiteBodyFatPercent = calculate3SiteSkinfoldBodyFatPercent(
            sex = profile.sex,
            ageYears = ageYears,
            chestSkinfoldMm = measurement.chestSkinfoldMm,
            abdomenSkinfoldMm = measurement.abdomenSkinfoldMm,
            thighSkinfoldMm = measurement.thighSkinfoldMm,
            tricepsSkinfoldMm = measurement.tricepsSkinfoldMm,
            suprailiacSkinfoldMm = measurement.suprailiacSkinfoldMm,
        )
        val waistHipRatio = calculateWaistHipRatio(waistCm = waistCm, hipCm = hipCm)
        val waistHeightRatio = calculateWaistHeightRatio(waistCm = waistCm, heightCm = heightCm)

        return DerivedMetrics(
            bmi = bmi,
            navyBodyFatPercent = navyBodyFatPercent,
            skinfold3SiteBodyFatPercent = skinfold3SiteBodyFatPercent,
            waistHipRatio = waistHipRatio,
            waistHeightRatio = waistHeightRatio,
        )
    }

    private fun calculateBmi(
        heightCm: Double,
        weightKg: Double?,
    ): Double? {
        if (heightCm <= 0 || weightKg == null || weightKg <= 0) {
            return null
        }

        val heightM = heightCm / 100.0
        return weightKg / (heightM * heightM)
    }

    private fun calculateNavyBodyFatPercent(
        sex: Sex,
        heightCm: Double,
        waistCm: Double?,
        hipCm: Double?,
        neckCm: Double?,
    ): Double? {
        if (heightCm <= 0 || waistCm == null || neckCm == null || waistCm <= 0 || neckCm <= 0) {
            return null
        }

        return when (sex) {
            Sex.Male -> {
                val logInput = waistCm - neckCm
                if (logInput <= 0) {
                    return null
                }

                86.010 * log10(logInput) -
                    70.041 * log10(heightCm) +
                    30.30
            }

            Sex.Female -> {
                if (hipCm == null || hipCm <= 0) {
                    return null
                }

                val logInput = waistCm + hipCm - neckCm
                if (logInput <= 0) {
                    return null
                }

                163.205 * log10(logInput) -
                    97.684 * log10(heightCm) -
                    104.912
            }
        }
    }

    private fun calculate3SiteSkinfoldBodyFatPercent(
        sex: Sex,
        ageYears: Int?,
        chestSkinfoldMm: Double?,
        abdomenSkinfoldMm: Double?,
        thighSkinfoldMm: Double?,
        tricepsSkinfoldMm: Double?,
        suprailiacSkinfoldMm: Double?,
    ): Double? {
        if (ageYears == null || ageYears <= 0) {
            return null
        }

        val sum3 = when (sex) {
            Sex.Male -> {
                if (
                    chestSkinfoldMm == null || chestSkinfoldMm <= 0 ||
                    abdomenSkinfoldMm == null || abdomenSkinfoldMm <= 0 ||
                    thighSkinfoldMm == null || thighSkinfoldMm <= 0
                ) {
                    return null
                }

                chestSkinfoldMm + abdomenSkinfoldMm + thighSkinfoldMm
            }

            Sex.Female -> {
                if (
                    tricepsSkinfoldMm == null || tricepsSkinfoldMm <= 0 ||
                    suprailiacSkinfoldMm == null || suprailiacSkinfoldMm <= 0 ||
                    thighSkinfoldMm == null || thighSkinfoldMm <= 0
                ) {
                    return null
                }

                tricepsSkinfoldMm + suprailiacSkinfoldMm + thighSkinfoldMm
            }
        }

        if (sum3 <= 0) {
            return null
        }

        val bodyDensity = when (sex) {
            Sex.Male -> {
                1.10938 -
                    (0.0008267 * sum3) +
                    (0.0000016 * sum3 * sum3) -
                    (0.0002574 * ageYears)
            }

            Sex.Female -> {
                1.0994921 -
                    (0.0009929 * sum3) +
                    (0.0000023 * sum3 * sum3) -
                    (0.0001392 * ageYears)
            }
        }

        if (bodyDensity <= 0) {
            return null
        }

        return (495.0 / bodyDensity) - 450.0
    }

    private fun calculateAgeYearsAtMeasurement(
        dateOfBirth: LocalDate,
        measurementEpochMillis: Long,
    ): Int? {
        val zoneId = ZoneId.systemDefault()
        val measurementDate = Instant.ofEpochMilli(measurementEpochMillis)
            .atZone(zoneId)
            .toLocalDate()

        if (measurementDate.isBefore(dateOfBirth)) {
            return null
        }

        return Period.between(dateOfBirth, measurementDate).years
    }

    private fun calculateWaistHipRatio(
        waistCm: Double?,
        hipCm: Double?,
    ): Double? {
        if (waistCm == null || hipCm == null || waistCm <= 0 || hipCm <= 0) {
            return null
        }

        return waistCm / hipCm
    }

    private fun calculateWaistHeightRatio(
        waistCm: Double?,
        heightCm: Double,
    ): Double? {
        if (waistCm == null || waistCm <= 0 || heightCm <= 0) {
            return null
        }

        return waistCm / heightCm
    }
}
