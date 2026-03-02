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

    override suspend fun getAll(): List<BodyMeasurement> = dao.getAll().map { it.toDomain() }

    override suspend fun getById(id: Long): BodyMeasurement? = dao.getById(id)?.toDomain()

    override suspend fun insert(measurement: BodyMeasurement): Long {
        return dao.insert(measurement.toEntityForInsert())
    }

    override suspend fun update(measurement: BodyMeasurement) {
        dao.update(measurement.toEntityForUpdate())
    }

    override suspend fun replaceAll(measurements: List<BodyMeasurement>) {
        dao.replaceAll(measurements.map { it.toEntityForInsert() })
    }
}

private fun MeasurementEntity.toDomain(): BodyMeasurement = BodyMeasurement(
    id = id,
    dateEpochMillis = dateEpochMillis,
    photoFilePath = photoFilePath,
    weightKg = weightKg,
    neckCircumferenceCm = neckCircumferenceCm,
    chestCircumferenceCm = chestCircumferenceCm,
    waistCircumferenceCm = waistCircumferenceCm,
    abdomenCircumferenceCm = abdomenCircumferenceCm,
    hipCircumferenceCm = hipCircumferenceCm,
    chestSkinfoldMm = chestSkinfoldMm,
    abdomenSkinfoldMm = abdomenSkinfoldMm,
    thighSkinfoldMm = thighSkinfoldMm,
    tricepsSkinfoldMm = tricepsSkinfoldMm,
    suprailiacSkinfoldMm = suprailiacSkinfoldMm,
)

private fun BodyMeasurement.toEntityForInsert(): MeasurementEntity = MeasurementEntity(
    id = 0,
    dateEpochMillis = dateEpochMillis,
    photoFilePath = photoFilePath,
    weightKg = weightKg,
    neckCircumferenceCm = neckCircumferenceCm,
    chestCircumferenceCm = chestCircumferenceCm,
    waistCircumferenceCm = waistCircumferenceCm,
    abdomenCircumferenceCm = abdomenCircumferenceCm,
    hipCircumferenceCm = hipCircumferenceCm,
    chestSkinfoldMm = chestSkinfoldMm,
    abdomenSkinfoldMm = abdomenSkinfoldMm,
    thighSkinfoldMm = thighSkinfoldMm,
    tricepsSkinfoldMm = tricepsSkinfoldMm,
    suprailiacSkinfoldMm = suprailiacSkinfoldMm,
)

private fun BodyMeasurement.toEntityForUpdate(): MeasurementEntity = toEntityForInsert().copy(id = id)