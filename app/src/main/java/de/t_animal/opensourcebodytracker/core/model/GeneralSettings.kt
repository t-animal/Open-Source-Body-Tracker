package de.t_animal.opensourcebodytracker.core.model

data class GeneralSettings(
    val onboardingCompleted: Boolean = false,
    val isDemoMode: Boolean = false,
    val unitSystem: UnitSystem = UnitSystem.Metric,
    val photoQuality: PhotoQuality = PhotoQuality.High,
)
