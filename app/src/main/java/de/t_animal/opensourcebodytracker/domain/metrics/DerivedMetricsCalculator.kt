package de.t_animal.opensourcebodytracker.domain.metrics

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.DerivedMetrics
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import kotlin.math.log10

class DerivedMetricsCalculator {
    fun calculate(
        profile: UserProfile,
        measurement: BodyMeasurement,
    ): DerivedMetrics {
        val heightCm = profile.heightCm.toDouble()
        val weightKg = measurement.weightKg
        val waistCm = measurement.waistCircumferenceCm
        val hipCm = measurement.hipCircumferenceCm
        val neckCm = measurement.neckCircumferenceCm

        val bmi = calculateBmi(heightCm = heightCm, weightKg = weightKg)
        val bodyFatPercent = calculateBodyFatPercent(
            sex = profile.sex,
            heightCm = heightCm,
            waistCm = waistCm,
            hipCm = hipCm,
            neckCm = neckCm,
        )
        val waistHipRatio = calculateWaistHipRatio(waistCm = waistCm, hipCm = hipCm)
        val waistHeightRatio = calculateWaistHeightRatio(waistCm = waistCm, heightCm = heightCm)
        val hipHeightRatio = calculateHipHeightRatio(hipCm = hipCm, heightCm = heightCm)

        return DerivedMetrics(
            bmi = bmi,
            bodyFatPercent = bodyFatPercent,
            waistHipRatio = waistHipRatio,
            waistHeightRatio = waistHeightRatio,
            hipHeightRatio = hipHeightRatio,
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

    private fun calculateBodyFatPercent(
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

    private fun calculateHipHeightRatio(
        hipCm: Double?,
        heightCm: Double,
    ): Double? {
        if (hipCm == null || hipCm <= 0 || heightCm <= 0) {
            return null
        }

        return hipCm / heightCm
    }
}
