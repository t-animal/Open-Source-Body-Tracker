package de.t_animal.opensourcebodytracker.domain.measurements

import de.t_animal.opensourcebodytracker.core.photos.PersistedPhotoPath
import de.t_animal.opensourcebodytracker.core.photos.TemporaryCapturePhotoPath
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import de.t_animal.opensourcebodytracker.domain.export.SetAutomaticExportPendingUseCase

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
    private val setAutomaticExportPendingUseCase: SetAutomaticExportPendingUseCase,
) {
    suspend operator fun invoke(command: DeleteMeasurementCommand): DeleteMeasurementResult {
        repository.deleteById(command.measurementId)
        command.pendingPhotoPath?.let { photoStorage.deleteTemporaryCapturePhoto(it) }
        command.persistedPhotoPath?.let { photoStorage.deletePhoto(it) }
        
        setAutomaticExportPendingUseCase.invoke(true)
        
        return DeleteMeasurementResult.Success
    }
}
