package de.t_animal.opensourcebodytracker.domain.export

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.photos.PhotoStorageContract
import de.t_animal.opensourcebodytracker.data.export.ExportArchiveEntry
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage

data class CollectedExportPhotos(
    val entries: List<ExportArchiveEntry.FileEntry>,
    val exportedImageCount: Int,
    val missingImageCount: Int,
)

data class ExportPhotoCollectionProgress(
    val processedMeasurementCount: Int,
    val totalMeasurementCount: Int,
    val exportedPhotoCount: Int,
    val missingPhotoCount: Int,
)

interface ExportPhotoCollector {
    fun collect(
        measurements: List<BodyMeasurement>,
        onProgress: ((ExportPhotoCollectionProgress) -> Unit)? = null,
    ): CollectedExportPhotos
}

class InternalStorageExportPhotoCollector(
    private val photoStorage: InternalPhotoStorage,
) : ExportPhotoCollector {
    override fun collect(
        measurements: List<BodyMeasurement>,
        onProgress: ((ExportPhotoCollectionProgress) -> Unit)?,
    ): CollectedExportPhotos {
        val entries = mutableListOf<ExportArchiveEntry.FileEntry>()
        var missingImageCount = 0
        val totalMeasurementCount = measurements.size

        measurements.forEachIndexed { index, measurement ->
            val photoPath = measurement.photoFilePath
            if (photoPath != null) {
                val photoFile = photoStorage.resolvePhotoFile(photoPath)
                if (!photoFile.isFile) {
                    missingImageCount += 1
                } else {
                    entries.add(
                        ExportArchiveEntry.FileEntry(
                            path = "${PhotoStorageContract.PERSISTED_PHOTOS_DIRECTORY}/${photoPath.value}",
                            file = photoFile,
                        ),
                    )
                }
            }

            onProgress?.invoke(
                ExportPhotoCollectionProgress(
                    processedMeasurementCount = index + 1,
                    totalMeasurementCount = totalMeasurementCount,
                    exportedPhotoCount = entries.size,
                    missingPhotoCount = missingImageCount,
                ),
            )
        }

        return CollectedExportPhotos(
            entries = entries,
            exportedImageCount = entries.size,
            missingImageCount = missingImageCount,
        )
    }
}
