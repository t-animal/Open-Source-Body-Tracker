package de.t_animal.opensourcebodytracker.feature.photos.helpers

import java.io.File

data class PhotosItemUiModel(
    val measurementId: Long,
    val dateEpochMillis: Long,
    val photoFile: File,
)

data class PhotosUiState(
    val items: List<PhotosItemUiModel> = emptyList(),
    val mode: PhotoMode = PhotoMode.NORMAL,
    val selectedMeasurementIds: List<Long> = emptyList(),
    val snackbarMessage: String? = null,
)

enum class PhotoMode {
    NORMAL,
    COMPARE,
}

data class PhotoSelectionResult(
    val selectedMeasurementIds: List<Long>,
    val selectionLimitReached: Boolean,
)
