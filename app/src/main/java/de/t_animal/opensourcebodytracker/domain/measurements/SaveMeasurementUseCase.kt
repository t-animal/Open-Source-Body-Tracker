package de.t_animal.opensourcebodytracker.domain.measurements

import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.photos.PersistedPhotoPath
import de.t_animal.opensourcebodytracker.core.photos.TemporaryCapturePhotoPath
import de.t_animal.opensourcebodytracker.domain.export.SetAutomaticExportPendingUseCase
import javax.inject.Inject

data class SaveMeasurementCommand(
    val measurementId: Long?,
    val dateEpochMillis: Long,
    val enabledMeasurements: Set<MeasuredBodyMetric>,
    val metricValues: Map<MeasuredBodyMetric, Double?>,
    val existingPhotoPath: PersistedPhotoPath?,
    val newPhotoPath: TemporaryCapturePhotoPath?,
    val deleteExistingPhoto: Boolean,
    val note: String = "",
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

class SaveMeasurementUseCase @Inject constructor(
    private val validator: MeasurementSaveValidator,
    private val saver: MeasurementSaver,
    private val setAutomaticExportPendingUseCase: SetAutomaticExportPendingUseCase,
) {
    suspend operator fun invoke(command: SaveMeasurementCommand): SaveMeasurementResult {
        val validationError = validator.validate(
            command.metricValues,
            command.newPhotoPath,
            command.existingPhotoPath,
            command.deleteExistingPhoto,
            command.note,
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
            command.metricValues,
            command.note,
        )

        setAutomaticExportPendingUseCase.invoke(true)

        return SaveMeasurementResult.Success(
            measurementId = result.id,
            photoFilePath = result.photoFilePath,
        )
    }
}
