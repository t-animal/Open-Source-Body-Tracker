package de.t_animal.opensourcebodytracker.domain.measurements

import de.t_animal.opensourcebodytracker.core.photos.PersistedPhotoPath
import de.t_animal.opensourcebodytracker.core.photos.TemporaryCapturePhotoPath
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage

data class DeleteMeasurementCommand(
    val measurementId: Long,
    val pendingPhotoPath: TemporaryCapturePhotoPath?,
    val persistedPhotoPath: PersistedPhotoPath?,
)

sealed interface DeleteMeasurementResult {
    data object Success : DeleteMeasurementResult
}

class DeleteMeasurementUseCase(
    private val repository: MeasurementRepository,
    private val photoStorage: InternalPhotoStorage,
) {
    suspend operator fun invoke(command: DeleteMeasurementCommand): DeleteMeasurementResult {
        repository.deleteById(command.measurementId)
        command.pendingPhotoPath?.let { photoStorage.deleteTemporaryCapturePhoto(it) }
        command.persistedPhotoPath?.let { photoStorage.deletePhoto(it) }
        return DeleteMeasurementResult.Success
    }
}
