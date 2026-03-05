package de.t_animal.opensourcebodytracker.feature.photos

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoAnimationLogicTest {

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
