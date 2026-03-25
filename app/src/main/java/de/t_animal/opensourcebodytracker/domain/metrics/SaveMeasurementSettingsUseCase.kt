package de.t_animal.opensourcebodytracker.domain.metrics

import de.t_animal.opensourcebodytracker.core.model.MeasurementSettings
import de.t_animal.opensourcebodytracker.data.settings.MeasurementSettingsRepository
import javax.inject.Inject

class SaveMeasurementSettingsUseCase @Inject constructor(
    private val measurementSettingsRepository: MeasurementSettingsRepository,
    private val requiredMeasurementsResolver: RequiredMeasurementsResolver,
) {
    suspend operator fun invoke(settings: MeasurementSettings) {
        val effective = requiredMeasurementsResolver.ensureRequiredWithCurrentProfile(settings)
        measurementSettingsRepository.saveSettings(effective.settings)
    }
}
