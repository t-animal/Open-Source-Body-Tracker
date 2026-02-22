package de.t_animal.opensourcebodytracker.core.model

data class BodyMeasurement(
    val id: Long,
    val dateEpochMillis: Long,
    val weightKg: Double? = null,
    val neckCircumferenceCm: Double? = null,
    val chestCircumferenceCm: Double? = null,
    val waistCircumferenceCm: Double? = null,
    val abdomenCircumferenceCm: Double? = null,
    val hipCircumferenceCm: Double? = null,
)
