package de.t_animal.opensourcebodytracker.core.model

data class UserProfile(
    val sex: Sex,
    val dateOfBirthEpochMillis: Long,
    val heightCm: Float,
)
