package de.t_animal.opensourcebodytracker.feature.photos

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoSelectionLogicTest {

    @Test
    fun togglePhotoSelection_addsMeasurement_whenNotSelectedAndBelowLimit() {
        val result = togglePhotoSelection(
            selectedMeasurementIds = listOf(11L),
            clickedMeasurementId = 22L,
        )

        assertEquals(listOf(11L, 22L), result.selectedMeasurementIds)
        assertFalse(result.selectionLimitReached)
    }

    @Test
    fun togglePhotoSelection_removesMeasurement_whenAlreadySelected() {
        val result = togglePhotoSelection(
            selectedMeasurementIds = listOf(11L, 22L),
            clickedMeasurementId = 22L,
        )

        assertEquals(listOf(11L), result.selectedMeasurementIds)
        assertFalse(result.selectionLimitReached)
    }

    @Test
    fun togglePhotoSelection_doesNotAddThirdMeasurement_whenLimitReached() {
        val result = togglePhotoSelection(
            selectedMeasurementIds = listOf(11L, 22L),
            clickedMeasurementId = 33L,
        )

        assertEquals(listOf(11L, 22L), result.selectedMeasurementIds)
        assertTrue(result.selectionLimitReached)
    }

    @Test
    fun orderedCompareSelection_sortsByDateAscending() {
        val items = listOf(
            photoItem(measurementId = 11L, dateEpochMillis = 1_770_000_000_000),
            photoItem(measurementId = 22L, dateEpochMillis = 1_760_000_000_000),
        )

        val selection = orderedCompareSelection(
            selectedMeasurementIds = listOf(11L, 22L),
            items = items,
        )

        assertEquals(22L to 11L, selection)
    }

    @Test
    fun orderedCompareSelection_returnsNull_whenSelectionNotExactlyTwo() {
        val selection = orderedCompareSelection(
            selectedMeasurementIds = listOf(11L),
            items = listOf(photoItem(measurementId = 11L, dateEpochMillis = 1_770_000_000_000)),
        )

        assertNull(selection)
    }

    private fun photoItem(measurementId: Long, dateEpochMillis: Long): PhotosItemUiModel {
        return PhotosItemUiModel(
            measurementId = measurementId,
            dateEpochMillis = dateEpochMillis,
            photoFile = File("/tmp/photo_$measurementId.jpg"),
        )
    }
}
