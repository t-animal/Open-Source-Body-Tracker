package de.t_animal.opensourcebodytracker.data.measurements

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements ORDER BY dateEpochMillis DESC")
    fun observeAll(): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements WHERE id = :id")
    suspend fun getById(id: Long): MeasurementEntity?

    @Insert
    suspend fun insert(entity: MeasurementEntity): Long

    @Update
    suspend fun update(entity: MeasurementEntity): Int
}
