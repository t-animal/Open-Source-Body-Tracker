package de.t_animal.opensourcebodytracker.domain.export

sealed interface ExportProgress {
    data object Validating : ExportProgress

    data object LoadingProfile : ExportProgress

    data object LoadingMeasurements : ExportProgress

    data class CollectingPhotos(
        val processedMeasurementCount: Int,
        val totalMeasurementCount: Int,
        val exportedPhotoCount: Int,
        val missingPhotoCount: Int,
    ) : ExportProgress

    data class WritingArchiveData(
        val currentDocumentIndex: Int,
        val totalDocumentCount: Int,
        val documentName: String,
    ) : ExportProgress

    data class WritingPhoto(
        val currentPhotoIndex: Int,
        val totalPhotoCount: Int,
        val photoName: String,
    ) : ExportProgress

    data object CleaningUpOldExports : ExportProgress
}