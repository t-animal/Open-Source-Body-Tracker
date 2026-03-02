package de.t_animal.opensourcebodytracker.feature.photos.helpers

fun togglePhotoSelection(
    selectedMeasurementIds: List<Long>,
    clickedMeasurementId: Long,
): PhotoSelectionResult {
    return if (selectedMeasurementIds.contains(clickedMeasurementId)) {
        PhotoSelectionResult(
            selectedMeasurementIds = selectedMeasurementIds - clickedMeasurementId,
            selectionLimitReached = false,
        )
    } else if (selectedMeasurementIds.size >= 2) {
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
    if (selectedMeasurementIds.size != 2) {
        return null
    }
    val itemById = items.associateBy { it.measurementId }
    val selectedItems = selectedMeasurementIds.mapNotNull { itemById[it] }
    if (selectedItems.size != 2) {
        return null
    }
    val ordered = selectedItems.sortedWith(
        compareBy<PhotosItemUiModel> { it.dateEpochMillis }
            .thenBy { it.measurementId },
    )
    return ordered[0].measurementId to ordered[1].measurementId
}
