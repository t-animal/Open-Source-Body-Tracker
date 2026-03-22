package de.t_animal.opensourcebodytracker.feature.photos.helpers

import java.io.File

data class PhotosItemUiModel(
    val measurementId: Long,
    val dateEpochMillis: Long,
    val photoFile: File,
)

sealed interface PhotosSnackbarMessage {
    data object SelectionLimitReached : PhotosSnackbarMessage
    data object MinimumSelectionRequired : PhotosSnackbarMessage
}

data class PhotosUiState(
    val items: List<PhotosItemUiModel> = emptyList(),
    val mode: PhotoMode = PhotoMode.NORMAL,
    val selectedMeasurementIds: List<Long> = emptyList(),
    val snackbarMessage: PhotosSnackbarMessage? = null,
)

enum class PhotoMode {
    NORMAL,
    COMPARE,
    ANIMATE,
}

data class PhotoSelectionResult(
    val selectedMeasurementIds: List<Long>,
    val selectionLimitReached: Boolean,
)
