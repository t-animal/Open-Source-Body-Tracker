package de.t_animal.opensourcebodytracker.data.measurements

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochMillis: Long,
    val photoFilePath: String? = null,
    val weightKg: Double? = null,
    val bodyFatPercent: Double? = null,
    val neckCircumferenceCm: Double? = null,
    val chestCircumferenceCm: Double? = null,
    val waistCircumferenceCm: Double? = null,
    val abdomenCircumferenceCm: Double? = null,
    val hipCircumferenceCm: Double? = null,
    val chestSkinfoldMm: Double? = null,
    val abdomenSkinfoldMm: Double? = null,
    val thighSkinfoldMm: Double? = null,
    val tricepsSkinfoldMm: Double? = null,
    val suprailiacSkinfoldMm: Double? = null,
)
