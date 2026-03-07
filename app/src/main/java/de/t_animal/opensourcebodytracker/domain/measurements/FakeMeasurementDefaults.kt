package de.t_animal.opensourcebodytracker.domain.measurements

import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import java.time.LocalDate
import java.time.ZoneId

const val DefaultFakeLeanBodyWeightKg = 67.0
const val DefaultFakeMinFatBodyWeightKg = 8.0
const val DefaultFakeMaxFatBodyWeightKg = 20.0

fun defaultFakeProfile(): UserProfile {
    val dateOfBirthMillis = LocalDate.of(1990, 2, 14)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    return UserProfile(
        sex = Sex.Male,
        dateOfBirthEpochMillis = dateOfBirthMillis,
        heightCm = 180f,
    )
}
