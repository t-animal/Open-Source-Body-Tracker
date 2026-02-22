package de.t_animal.opensourcebodytracker.data.measurements

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomMeasurementRepository(
    private val dao: MeasurementDao,
) : MeasurementRepository {
    override fun observeAll(): Flow<List<BodyMeasurement>> = dao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun getById(id: Long): BodyMeasurement? = dao.getById(id)?.toDomain()

    override suspend fun insert(measurement: BodyMeasurement): Long {
        return dao.insert(measurement.toEntityForInsert())
    }

    override suspend fun update(measurement: BodyMeasurement) {
        dao.update(measurement.toEntityForUpdate())
    }
}

private fun MeasurementEntity.toDomain(): BodyMeasurement = BodyMeasurement(
    id = id,
    dateEpochMillis = dateEpochMillis,
    weightKg = weightKg,
    neckCircumferenceCm = neckCircumferenceCm,
    chestCircumferenceCm = chestCircumferenceCm,
    waistCircumferenceCm = waistCircumferenceCm,
    abdomenCircumferenceCm = abdomenCircumferenceCm,
    hipCircumferenceCm = hipCircumferenceCm,
)

private fun BodyMeasurement.toEntityForInsert(): MeasurementEntity = MeasurementEntity(
    id = 0,
    dateEpochMillis = dateEpochMillis,
    weightKg = weightKg,
    neckCircumferenceCm = neckCircumferenceCm,
    chestCircumferenceCm = chestCircumferenceCm,
    waistCircumferenceCm = waistCircumferenceCm,
    abdomenCircumferenceCm = abdomenCircumferenceCm,
    hipCircumferenceCm = hipCircumferenceCm,
)

private fun BodyMeasurement.toEntityForUpdate(): MeasurementEntity = MeasurementEntity(
    id = id,
    dateEpochMillis = dateEpochMillis,
    weightKg = weightKg,
    neckCircumferenceCm = neckCircumferenceCm,
    chestCircumferenceCm = chestCircumferenceCm,
    waistCircumferenceCm = waistCircumferenceCm,
    abdomenCircumferenceCm = abdomenCircumferenceCm,
    hipCircumferenceCm = hipCircumferenceCm,
)
