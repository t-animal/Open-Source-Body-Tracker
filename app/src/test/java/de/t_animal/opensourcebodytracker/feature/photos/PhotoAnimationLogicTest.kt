package de.t_animal.opensourcebodytracker.feature.photos

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.feature.photos.helpers.DEFAULT_ANIMATION_SPEED_FPS
import de.t_animal.opensourcebodytracker.feature.photos.helpers.MAX_ANIMATION_SPEED_FPS
import de.t_animal.opensourcebodytracker.feature.photos.helpers.MIN_ANIMATION_SPEED_FPS
import de.t_animal.opensourcebodytracker.feature.photos.helpers.canDecreaseSpeed
import de.t_animal.opensourcebodytracker.feature.photos.helpers.canIncreaseSpeed
import de.t_animal.opensourcebodytracker.feature.photos.helpers.nextFasterSpeedFps
import de.t_animal.opensourcebodytracker.feature.photos.helpers.nextFrameIndex
import de.t_animal.opensourcebodytracker.feature.photos.helpers.nextSlowerSpeedFps
import de.t_animal.opensourcebodytracker.feature.photos.helpers.previousFrameIndex
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoAnimationLogicTest {

    @Test
    fun nextSlowerSpeedFps_usesSmallerStepsAtLowerSpeeds() {
        assertEquals(4f, nextSlowerSpeedFps(5f), 0f)
        assertEquals(2f, nextSlowerSpeedFps(4f), 0f)
        assertEquals(0.25f, nextSlowerSpeedFps(0.5f), 0f)
    }

    @Test
    fun nextFasterSpeedFps_usesLargerStepsAtHigherSpeeds() {
        assertEquals(6f, nextFasterSpeedFps(5f), 0f)
        assertEquals(12f, nextFasterSpeedFps(10f), 0f)
        assertEquals(15f, nextFasterSpeedFps(12f), 0f)
    }

    @Test
    fun speedBounds_areClampedAtMinAndMax() {
        assertEquals(MIN_ANIMATION_SPEED_FPS, nextSlowerSpeedFps(MIN_ANIMATION_SPEED_FPS), 0f)
        assertEquals(MAX_ANIMATION_SPEED_FPS, nextFasterSpeedFps(MAX_ANIMATION_SPEED_FPS), 0f)

        assertFalse(canDecreaseSpeed(MIN_ANIMATION_SPEED_FPS))
        assertFalse(canIncreaseSpeed(MAX_ANIMATION_SPEED_FPS))
        assertTrue(canDecreaseSpeed(DEFAULT_ANIMATION_SPEED_FPS))
        assertTrue(canIncreaseSpeed(DEFAULT_ANIMATION_SPEED_FPS))
    }

    @Test
    fun frameNavigation_wrapsInBothDirections() {
        assertEquals(4, previousFrameIndex(currentIndex = 0, frameCount = 5))
        assertEquals(0, nextFrameIndex(currentIndex = 4, frameCount = 5))
        assertEquals(0, previousFrameIndex(currentIndex = 0, frameCount = 0))
        assertEquals(0, nextFrameIndex(currentIndex = 0, frameCount = 0))
    }

    @Test
    fun buildAnimationFrameItems_keepsSelectedOrder_andFiltersInvalidEntries() {
        val selectedIds = listOf(20L, 40L, 10L, 30L)
        val measurementsById = mapOf(
            10L to measurement(
                id = 10L,
                dateEpochMillis = 1_760_000_000_000,
                photoFilePath = "photo_10.jpg",
            ),
            20L to measurement(
                id = 20L,
                dateEpochMillis = 1_770_000_000_000,
                photoFilePath = "photo_20.jpg",
            ),
            30L to measurement(
                id = 30L,
                dateEpochMillis = 1_780_000_000_000,
                photoFilePath = "missing.jpg",
            ),
            40L to measurement(
                id = 40L,
                dateEpochMillis = 1_790_000_000_000,
                photoFilePath = null,
            ),
        )

        val frames = buildAnimationFrameItems(
            selectedMeasurementIds = selectedIds,
            measurementsById = measurementsById,
            resolvePhotoFile = { path -> File(path) },
            fileExists = { file -> file.path != "missing.jpg" },
        )

        assertEquals(listOf(20L, 10L), frames.map { it.measurementId })
    }

    @Test
    fun buildAnimationFrameItems_returnsEmpty_whenNoValidPhotosRemain() {
        val selectedIds = listOf(10L, 20L)
        val measurementsById = mapOf(
            10L to measurement(
                id = 10L,
                dateEpochMillis = 1_760_000_000_000,
                photoFilePath = null,
            ),
            20L to measurement(
                id = 20L,
                dateEpochMillis = 1_770_000_000_000,
                photoFilePath = "missing.jpg",
            ),
        )

        val frames = buildAnimationFrameItems(
            selectedMeasurementIds = selectedIds,
            measurementsById = measurementsById,
            resolvePhotoFile = { path -> File(path) },
            fileExists = { _ -> false },
        )

        assertEquals(emptyList<Long>(), frames.map { it.measurementId })
    }

    private fun measurement(
        id: Long,
        dateEpochMillis: Long,
        photoFilePath: String?,
    ): BodyMeasurement {
        return BodyMeasurement(
            id = id,
            dateEpochMillis = dateEpochMillis,
            photoFilePath = photoFilePath,
        )
    }
}
