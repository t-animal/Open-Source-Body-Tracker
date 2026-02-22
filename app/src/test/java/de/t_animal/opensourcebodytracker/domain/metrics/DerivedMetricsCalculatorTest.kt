package de.t_animal.opensourcebodytracker.domain.metrics

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DerivedMetricsCalculatorTest {
    private val calculator = DerivedMetricsCalculator()

    @Test
    fun calculate_returnsBmiAndRatios_whenInputsAreValid() {
        val profile = UserProfile(
            sex = Sex.Male,
            dateOfBirthEpochMillis = 0L,
            heightCm = 180f,
        )
        val measurement = BodyMeasurement(
            id = 1L,
            dateEpochMillis = 0L,
            weightKg = 80.0,
            waistCircumferenceCm = 90.0,
            hipCircumferenceCm = 100.0,
            neckCircumferenceCm = 40.0,
        )

        val result = calculator.calculate(profile, measurement)

        assertNotNull(result.bmi)
        assertNotNull(result.waistHipRatio)
        assertNotNull(result.waistHeightRatio)
        assertNotNull(result.hipHeightRatio)
        assertEquals(24.69, result.bmi ?: 0.0, 0.01)
        assertEquals(0.90, result.waistHipRatio ?: 0.0, 0.0001)
        assertEquals(0.50, result.waistHeightRatio ?: 0.0, 0.0001)
        assertEquals(0.5556, result.hipHeightRatio ?: 0.0, 0.0001)
    }

    @Test
    fun calculate_returnsMaleNavyBodyFat_whenMaleInputsAreValid() {
        val profile = UserProfile(
            sex = Sex.Male,
            dateOfBirthEpochMillis = 0L,
            heightCm = 180f,
        )
        val measurement = BodyMeasurement(
            id = 1L,
            dateEpochMillis = 0L,
            waistCircumferenceCm = 90.0,
            neckCircumferenceCm = 40.0,
        )

        val result = calculator.calculate(profile, measurement)

        assertNotNull(result.bodyFatPercent)
        val value = result.bodyFatPercent ?: 0.0
        assertTrue(value in 20.0..30.0)
    }

    @Test
    fun calculate_returnsFemaleNavyBodyFat_whenFemaleInputsAreValid() {
        val profile = UserProfile(
            sex = Sex.Female,
            dateOfBirthEpochMillis = 0L,
            heightCm = 165f,
        )
        val measurement = BodyMeasurement(
            id = 1L,
            dateEpochMillis = 0L,
            waistCircumferenceCm = 70.0,
            hipCircumferenceCm = 95.0,
            neckCircumferenceCm = 35.0,
        )

        val result = calculator.calculate(profile, measurement)

        assertNotNull(result.bodyFatPercent)
        val value = result.bodyFatPercent ?: 0.0
        assertTrue(value in 40.0..60.0)
    }

    @Test
    fun calculate_returnsNullBodyFat_whenNavyLogInputIsInvalid() {
        val profile = UserProfile(
            sex = Sex.Male,
            dateOfBirthEpochMillis = 0L,
            heightCm = 180f,
        )
        val measurement = BodyMeasurement(
            id = 1L,
            dateEpochMillis = 0L,
            waistCircumferenceCm = 40.0,
            neckCircumferenceCm = 40.0,
        )

        val result = calculator.calculate(profile, measurement)

        assertNull(result.bodyFatPercent)
    }
}
