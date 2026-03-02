package de.t_animal.opensourcebodytracker.domain.measurements

import org.junit.Assert.assertEquals
import org.junit.Test

class GenerateFakeMeasurementsWithPhotosUseCaseTest {
    @Test
    fun buildPhotoTargetBodyFatPercents_distributesEvenlyFromMaxToMin() {
        val targets = buildPhotoTargetBodyFatPercents(
            minBodyFatPercent = 10.0,
            maxBodyFatPercent = 28.0,
            photoCount = 10,
        )

        assertEquals(10, targets.size)
        assertEquals(28.0, targets.first(), 0.0001)
        assertEquals(10.0, targets.last(), 0.0001)
        assertEquals(26.0, targets[1], 0.0001)
        assertEquals(24.0, targets[2], 0.0001)
    }

    @Test
    fun closestPhotoLabel_returnsNearestTargetIndexPlusOne() {
        val targets = buildPhotoTargetBodyFatPercents(
            minBodyFatPercent = 10.0,
            maxBodyFatPercent = 28.0,
            photoCount = 10,
        )

        assertEquals(1, closestPhotoLabel(27.1, targets))
        assertEquals(5, closestPhotoLabel(20.1, targets))
        assertEquals(10, closestPhotoLabel(9.7, targets))
    }

    @Test
    fun buildPhotoTargetBodyFatPercents_returnsEmptyForInvalidCount() {
        val targets = buildPhotoTargetBodyFatPercents(
            minBodyFatPercent = 10.0,
            maxBodyFatPercent = 28.0,
            photoCount = 0,
        )

        assertEquals(emptyList<Double>(), targets)
    }
}
