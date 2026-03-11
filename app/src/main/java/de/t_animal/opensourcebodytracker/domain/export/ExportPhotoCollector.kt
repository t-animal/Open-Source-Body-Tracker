package de.t_animal.opensourcebodytracker.domain.export

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.photos.PhotoStorageContract
import de.t_animal.opensourcebodytracker.data.export.ExportArchiveEntry
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import kotlin.collections.plusAssign

data class CollectedExportPhotos(
    val entries: List<ExportArchiveEntry.FileEntry>,
    val exportedImageCount: Int,
    val missingImageCount: Int,
)

interface ExportPhotoCollector {
    fun collect(measurements: List<BodyMeasurement>): CollectedExportPhotos
}

class InternalStorageExportPhotoCollector(
    private val photoStorage: InternalPhotoStorage,
) : ExportPhotoCollector {
    override fun collect(measurements: List<BodyMeasurement>): CollectedExportPhotos {
        val entries = mutableListOf<ExportArchiveEntry.FileEntry>()
        var missingImageCount = 0

        measurements.forEach { measurement ->
            val photoPath = measurement.photoFilePath ?: return@forEach
            val photoFile = photoStorage.resolvePhotoFile(photoPath)
            if (!photoFile.isFile) {
                missingImageCount += 1
                return@forEach
            }

            entries.add(ExportArchiveEntry.FileEntry(
                path = "${PhotoStorageContract.PERSISTED_PHOTOS_DIRECTORY}/${photoPath.value}",
                file = photoFile,
            ))
        }

        return CollectedExportPhotos(
            entries = entries,
            exportedImageCount = entries.size,
            missingImageCount = missingImageCount,
        )
    }
}
