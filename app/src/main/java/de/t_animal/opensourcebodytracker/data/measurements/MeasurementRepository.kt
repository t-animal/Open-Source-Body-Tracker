package de.t_animal.opensourcebodytracker.data.measurements

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import kotlinx.coroutines.flow.Flow

interface MeasurementRepository {
    fun observeAll(): Flow<List<BodyMeasurement>>

    suspend fun getAll(): List<BodyMeasurement>

    suspend fun getById(id: Long): BodyMeasurement?

    suspend fun insert(measurement: BodyMeasurement): Long

    suspend fun update(measurement: BodyMeasurement)

    suspend fun deleteById(id: Long)

    suspend fun replaceAll(measurements: List<BodyMeasurement>)
}
