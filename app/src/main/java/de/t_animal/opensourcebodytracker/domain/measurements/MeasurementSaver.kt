package de.t_animal.opensourcebodytracker.domain.measurements

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.PhotoQuality
import de.t_animal.opensourcebodytracker.core.photos.PersistedPhotoPath
import de.t_animal.opensourcebodytracker.core.photos.TemporaryCapturePhotoPath
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import javax.inject.Inject

class MeasurementSaver @Inject constructor(
    private val repository: MeasurementRepository,
    private val photoStorage: InternalPhotoStorage,
) {
    suspend fun save(
        currentMeasurementId: Long?,
        dateEpochMillis: Long,
        newPhotoPath: TemporaryCapturePhotoPath?,
        originalPhotoPath: PersistedPhotoPath?,
        deleteOriginalPhoto: Boolean,
        enabledMeasurements: Set<MeasuredBodyMetric>,
        metricValues: Map<MeasuredBodyMetric, Double?>,
        note: String,
        photoQuality: PhotoQuality,
    ): BodyMeasurement {
        if (currentMeasurementId == null) {
            return saveNewMeasurement(dateEpochMillis, newPhotoPath, enabledMeasurements, metricValues, note, photoQuality)
        } else {
            return updateExistingMeasurement(
                currentMeasurementId,
                dateEpochMillis,
                newPhotoPath,
                originalPhotoPath,
                deleteOriginalPhoto,
                enabledMeasurements,
                metricValues,
                note,
                photoQuality,
            )
        }
    }

    private suspend fun saveNewMeasurement(
        dateEpochMillis: Long,
        newPhotoPath: TemporaryCapturePhotoPath?,
        enabledMeasurements: Set<MeasuredBodyMetric>,
        metricValues: Map<MeasuredBodyMetric, Double?>,
        note: String,
        photoQuality: PhotoQuality,
    ): BodyMeasurement {
        val measurementWithoutPhoto = MeasurementMetricMapper.toBodyMeasurement(
            id = 0,
            dateEpochMillis = dateEpochMillis,
            photoFilePath = null,
            values = metricValues,
            enabledMeasurements = enabledMeasurements,
            note = note,
        )
        val insertedId = repository.insert(measurementWithoutPhoto)
        val measurementWithId = measurementWithoutPhoto.copy(id = insertedId)

        if (newPhotoPath == null) {
            return measurementWithId
        }

        val savedPhotoPath = photoStorage.movePhotoForMeasurement(
            measurementId = insertedId,
            measurementDateEpochMillis = dateEpochMillis,
            sourceAbsolutePath = newPhotoPath,
            photoQuality = photoQuality,
        )
        val measurementWithPhoto = measurementWithId.copy(photoFilePath = savedPhotoPath)
        repository.update(measurementWithPhoto)
        return measurementWithPhoto
    }

    private suspend fun updateExistingMeasurement(
        currentMeasurementId: Long,
        dateEpochMillis: Long,
        newPhotoPath: TemporaryCapturePhotoPath?,
        originalPhotoPath: PersistedPhotoPath?,
        deleteOriginalPhoto: Boolean,
        enabledMeasurements: Set<MeasuredBodyMetric>,
        metricValues: Map<MeasuredBodyMetric, Double?>,
        note: String,
        photoQuality: PhotoQuality,
    ): BodyMeasurement {
        val hasNewPhoto = newPhotoPath != null
        val hasOldPhoto = originalPhotoPath != null

        val updatedPhotoPath = when {
            newPhotoPath != null -> photoStorage.movePhotoForMeasurement(
                measurementId = currentMeasurementId,
                measurementDateEpochMillis = dateEpochMillis,
                sourceAbsolutePath = newPhotoPath,
                photoQuality = photoQuality,
            )

            deleteOriginalPhoto -> null
            else -> originalPhotoPath
        }

        val updatedMeasurement = MeasurementMetricMapper.toBodyMeasurement(
            id = currentMeasurementId,
            dateEpochMillis = dateEpochMillis,
            photoFilePath = updatedPhotoPath,
            values = metricValues,
            enabledMeasurements = enabledMeasurements,
            note = note,
        )

        repository.update(updatedMeasurement)

        val photoWasReplaced = hasNewPhoto && hasOldPhoto && originalPhotoPath != updatedPhotoPath
        if ((deleteOriginalPhoto || photoWasReplaced) && hasOldPhoto) {
            photoStorage.deletePhoto(originalPhotoPath)
        }

        return updatedMeasurement
    }
}
