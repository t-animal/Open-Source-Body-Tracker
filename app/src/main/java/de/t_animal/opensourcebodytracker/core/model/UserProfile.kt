package de.t_animal.opensourcebodytracker.core.model

import java.time.LocalDate

data class UserProfile(
    val sex: Sex,
    val dateOfBirth: LocalDate,
    val heightCm: Float,
)
