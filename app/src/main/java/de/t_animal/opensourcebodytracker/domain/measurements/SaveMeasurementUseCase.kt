package de.t_animal.opensourcebodytracker.domain.measurements

import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.photos.PersistedPhotoPath
import de.t_animal.opensourcebodytracker.core.photos.TemporaryCapturePhotoPath

data class SaveMeasurementCommand(
    val measurementId: Long?,
    val dateEpochMillis: Long,
    val enabledMeasurements: Set<MeasuredBodyMetric>,
    val metricValues: Map<MeasuredBodyMetric, Double?>,
    val existingPhotoPath: PersistedPhotoPath?,
    val newPhotoPath: TemporaryCapturePhotoPath?,
    val deleteExistingPhoto: Boolean,
)


sealed interface SaveMeasurementResult {
    data class Success(
        val measurementId: Long,
        val photoFilePath: PersistedPhotoPath?,
    ) : SaveMeasurementResult

    data class ValidationError(
        val message: String,
    ) : SaveMeasurementResult
}

class SaveMeasurementUseCase(
    private val validator: MeasurementSaveValidator,
    private val saver: MeasurementSaver,
) {
    suspend operator fun invoke(command: SaveMeasurementCommand): SaveMeasurementResult {
        val validationError = validator.validate(
            command.metricValues, command.newPhotoPath, command.existingPhotoPath, command.deleteExistingPhoto
        )
        if (validationError != null) {
            return SaveMeasurementResult.ValidationError(validationError)
        }
        val result = saver.save(
            command.measurementId,
            command.dateEpochMillis,
            command.newPhotoPath,
            command.existingPhotoPath,
            command.deleteExistingPhoto,
            command.enabledMeasurements,
            command.metricValues
        )

        return SaveMeasurementResult.Success(
            measurementId = result.id,
            photoFilePath = result.photoFilePath,
        )
    }
}
