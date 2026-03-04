package de.t_animal.opensourcebodytracker.feature.photos.helpers

fun togglePhotoSelection(
    selectedMeasurementIds: List<Long>,
    clickedMeasurementId: Long,
    maxSelection: Int? = 2,
): PhotoSelectionResult {
    val selectionLimit = maxSelection?.coerceAtLeast(1)

    return if (selectedMeasurementIds.contains(clickedMeasurementId)) {
        PhotoSelectionResult(
            selectedMeasurementIds = selectedMeasurementIds - clickedMeasurementId,
            selectionLimitReached = false,
        )
    } else if (selectionLimit != null && selectedMeasurementIds.size >= selectionLimit) {
        PhotoSelectionResult(
            selectedMeasurementIds = selectedMeasurementIds,
            selectionLimitReached = true,
        )
    } else {
        PhotoSelectionResult(
            selectedMeasurementIds = selectedMeasurementIds + clickedMeasurementId,
            selectionLimitReached = false,
        )
    }
}

fun orderedCompareSelection(
    selectedMeasurementIds: List<Long>,
    items: List<PhotosItemUiModel>,
): Pair<Long, Long>? {
    val orderedIds = orderedAnimationSelection(
        selectedMeasurementIds = selectedMeasurementIds,
        items = items,
    )

    if (orderedIds.size != 2) {
        return null
    }

    return orderedIds[0] to orderedIds[1]
}

fun orderedAnimationSelection(
    selectedMeasurementIds: List<Long>,
    items: List<PhotosItemUiModel>,
): List<Long> {
    val itemById = items.associateBy { it.measurementId }
    return selectedMeasurementIds
        .mapNotNull { itemById[it] }
        .sortedWith(
            compareBy<PhotosItemUiModel> { it.dateEpochMillis }
                .thenBy { it.measurementId },
        )
        .map { it.measurementId }
}
