package de.t_animal.opensourcebodytracker.domain.demodata

import org.junit.Assert.assertEquals
import org.junit.Test

class DemoDataPhotoSeederTest {
    @Test
    fun buildDemoDataPhotoTargetBodyFatPercents_distributesEvenlyFromMaxToMin() {
        val targets = buildDemoDataPhotoTargetBodyFatPercents(
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
    fun closestDemoDataPhotoLabel_returnsNearestTargetIndexPlusOne() {
        val targets = buildDemoDataPhotoTargetBodyFatPercents(
            minBodyFatPercent = 10.0,
            maxBodyFatPercent = 28.0,
            photoCount = 10,
        )

        assertEquals(1, closestDemoDataPhotoLabel(27.1, targets))
        assertEquals(5, closestDemoDataPhotoLabel(20.1, targets))
        assertEquals(10, closestDemoDataPhotoLabel(9.7, targets))
    }

    @Test
    fun buildDemoDataPhotoTargetBodyFatPercents_returnsEmptyForInvalidCount() {
        val targets = buildDemoDataPhotoTargetBodyFatPercents(
            minBodyFatPercent = 10.0,
            maxBodyFatPercent = 28.0,
            photoCount = 0,
        )

        assertEquals(emptyList<Double>(), targets)
    }
}
