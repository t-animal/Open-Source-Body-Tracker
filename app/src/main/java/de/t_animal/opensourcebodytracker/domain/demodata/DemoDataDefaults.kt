package de.t_animal.opensourcebodytracker.domain.demodata

import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import java.time.LocalDate

const val DefaultDemoDataLeanBodyWeightKg = 67.0
const val DefaultDemoDataMinFatBodyWeightKg = 8.0
const val DefaultDemoDataMaxFatBodyWeightKg = 20.0

fun defaultDemoDataProfile(): UserProfile {
    return UserProfile(
        sex = Sex.Male,
        dateOfBirth = LocalDate.of(1990, 2, 14),
        heightCm = 180f,
    )
}
