package de.t_animal.opensourcebodytracker.domain

import de.t_animal.opensourcebodytracker.core.model.MeasurementSettings
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.MeasurementSettingsRepository
import de.t_animal.opensourcebodytracker.domain.metrics.RequiredMeasurementsResolver
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class SaveProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val measurementSettingsRepository: MeasurementSettingsRepository,
    private val requiredMeasurementsResolver: RequiredMeasurementsResolver,
) {
    suspend operator fun invoke(profile: UserProfile, measurementSettings: MeasurementSettings? = null) {
        profileRepository.saveProfile(profile)

        val settings = measurementSettings ?: measurementSettingsRepository.settingsFlow.first()
        val effective = requiredMeasurementsResolver.ensureRequired(settings, profile)

        if (effective.settings != settings) {
            measurementSettingsRepository.saveSettings(effective.settings)
        }
    }
}
