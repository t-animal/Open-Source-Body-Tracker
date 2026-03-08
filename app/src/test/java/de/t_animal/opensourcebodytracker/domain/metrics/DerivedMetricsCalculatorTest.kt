package de.t_animal.opensourcebodytracker.domain.metrics

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DerivedMetricsCalculatorTest {
    private val calculator = DerivedMetricsCalculator()
    private val measurementDateEpochMillis = 1_704_067_200_000L
    private val adultDateOfBirth = LocalDate.of(1998, 1, 1)
    private val measurementDate = Instant.ofEpochMilli(measurementDateEpochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    @Test
    fun calculate_returnsBmiAndRatios_whenInputsAreValid() {
        val profile = UserProfile(
            sex = Sex.Male,
            dateOfBirth = adultDateOfBirth,
            heightCm = 180f,
        )
        val measurement = BodyMeasurement(
            id = 1L,
            dateEpochMillis = measurementDateEpochMillis,
            weightKg = 80.0,
            waistCircumferenceCm = 90.0,
            hipCircumferenceCm = 100.0,
            neckCircumferenceCm = 40.0,
        )

        val result = calculator.calculate(profile, measurement)

        assertNotNull(result.bmi)
        assertNotNull(result.waistHipRatio)
        assertNotNull(result.waistHeightRatio)
        assertEquals(24.69, result.bmi ?: 0.0, 0.01)
        assertEquals(0.90, result.waistHipRatio ?: 0.0, 0.0001)
        assertEquals(0.50, result.waistHeightRatio ?: 0.0, 0.0001)
    }

    @Test
    fun calculate_returnsMaleNavyBodyFat_whenMaleInputsAreValid() {
        val profile = UserProfile(
            sex = Sex.Male,
            dateOfBirth = adultDateOfBirth,
            heightCm = 180f,
        )
        val measurement = BodyMeasurement(
            id = 1L,
            dateEpochMillis = measurementDateEpochMillis,
            waistCircumferenceCm = 90.0,
            neckCircumferenceCm = 40.0,
        )

        val result = calculator.calculate(profile, measurement)

        assertNotNull(result.navyBodyFatPercent)
        val value = result.navyBodyFatPercent ?: 0.0
        assertTrue(value in 15.0..25.0)
    }

    @Test
    fun calculate_returnsFemaleNavyBodyFat_whenFemaleInputsAreValid() {
        val profile = UserProfile(
            sex = Sex.Female,
            dateOfBirth = adultDateOfBirth,
            heightCm = 165f,
        )
        val measurement = BodyMeasurement(
            id = 1L,
            dateEpochMillis = measurementDateEpochMillis,
            waistCircumferenceCm = 70.0,
            hipCircumferenceCm = 95.0,
            neckCircumferenceCm = 35.0,
        )

        val result = calculator.calculate(profile, measurement)

        assertNotNull(result.navyBodyFatPercent)
        val value = result.navyBodyFatPercent ?: 0.0
        assertTrue(value in 20.0..30.0)
    }

    @Test
    fun calculate_returnsNullBodyFat_whenNavyLogInputIsInvalid() {
        val profile = UserProfile(
            sex = Sex.Male,
            dateOfBirth = adultDateOfBirth,
            heightCm = 180f,
        )
        val measurement = BodyMeasurement(
            id = 1L,
            dateEpochMillis = measurementDateEpochMillis,
            waistCircumferenceCm = 40.0,
            neckCircumferenceCm = 40.0,
        )

        val result = calculator.calculate(profile, measurement)

        assertNull(result.navyBodyFatPercent)
    }

    @Test
    fun calculate_returnsMaleSkinfoldBodyFat_whenMaleInputsAreValid() {
        val profile = UserProfile(
            sex = Sex.Male,
            dateOfBirth = adultDateOfBirth,
            heightCm = 180f,
        )
        val measurement = BodyMeasurement(
            id = 1L,
            dateEpochMillis = measurementDateEpochMillis,
            chestSkinfoldMm = 12.0,
            abdomenSkinfoldMm = 18.0,
            thighSkinfoldMm = 16.0,
        )

        val result = calculator.calculate(profile, measurement)

        assertNotNull(result.skinfold3SiteBodyFatPercent)
        val value = result.skinfold3SiteBodyFatPercent ?: 0.0
        assertTrue(value in 10.0..25.0)
    }

    @Test
    fun calculate_returnsFemaleSkinfoldBodyFat_whenFemaleInputsAreValid() {
        val profile = UserProfile(
            sex = Sex.Female,
            dateOfBirth = adultDateOfBirth,
            heightCm = 165f,
        )
        val measurement = BodyMeasurement(
            id = 1L,
            dateEpochMillis = measurementDateEpochMillis,
            tricepsSkinfoldMm = 18.0,
            suprailiacSkinfoldMm = 20.0,
            thighSkinfoldMm = 24.0,
        )

        val result = calculator.calculate(profile, measurement)

        assertNotNull(result.skinfold3SiteBodyFatPercent)
        val value = result.skinfold3SiteBodyFatPercent ?: 0.0
        assertTrue(value in 15.0..35.0)
    }

    @Test
    fun calculate_returnsNullSkinfoldBodyFat_whenAnyRequiredSiteMissing() {
        val profile = UserProfile(
            sex = Sex.Male,
            dateOfBirth = adultDateOfBirth,
            heightCm = 180f,
        )
        val measurement = BodyMeasurement(
            id = 1L,
            dateEpochMillis = measurementDateEpochMillis,
            chestSkinfoldMm = 12.0,
            abdomenSkinfoldMm = 18.0,
            thighSkinfoldMm = null,
        )

        val result = calculator.calculate(profile, measurement)

        assertNull(result.skinfold3SiteBodyFatPercent)
    }

    @Test
    fun calculate_returnsNullSkinfoldBodyFat_whenAgeAtMeasurementIsNotPositive() {
        val profile = UserProfile(
            sex = Sex.Male,
            dateOfBirth = measurementDate,
            heightCm = 180f,
        )
        val measurement = BodyMeasurement(
            id = 1L,
            dateEpochMillis = measurementDateEpochMillis,
            chestSkinfoldMm = 12.0,
            abdomenSkinfoldMm = 18.0,
            thighSkinfoldMm = 16.0,
        )

        val result = calculator.calculate(profile, measurement)

        assertNull(result.skinfold3SiteBodyFatPercent)
    }
}
