package de.t_animal.opensourcebodytracker.feature.photos.helpers

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import java.io.File

data class PhotosItemUiModel(
    val measurementId: Long,
    val dateEpochMillis: Long,
    val photoFile: File,
)

fun BodyMeasurement.toPhotoItemOrNull(storage: InternalPhotoStorage): PhotosItemUiModel? {
    val photoPath = photoFilePath ?: return null
    val photoFile = storage.resolvePhotoFile(photoPath)
    return PhotosItemUiModel(
        measurementId = id,
        dateEpochMillis = dateEpochMillis,
        photoFile = photoFile,
    )
}

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
    ANIMATE;

    val maxSelection: Int? get() = if (this == COMPARE) 2 else null
    val minSelection: Int get() = if (this == ANIMATE || this == COMPARE) 2 else 0
}

data class PhotoSelectionResult(
    val selectedMeasurementIds: List<Long>,
    val selectionLimitReached: Boolean,
)
