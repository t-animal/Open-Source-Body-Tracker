package de.t_animal.opensourcebodytracker.domain.metrics

import de.t_animal.opensourcebodytracker.core.model.MeasurementSettings
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.MeasurementSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

data class EffectiveMeasurementSettings(
    val settings: MeasurementSettings,
    val dependencies: DerivedBodyMetricsDependencies,
)

class RequiredMeasurementsResolver @Inject constructor(
    private val measurementSettingsRepository: MeasurementSettingsRepository,
    private val profileRepository: ProfileRepository,
    private val dependencyResolver: DerivedMetricsDependencyResolver,
) {
    val effectiveMeasurementSettingsFlow: Flow<EffectiveMeasurementSettings> = combine(
        measurementSettingsRepository.settingsFlow,
        profileRepository.requiredProfileFlow,
    ) { settings, profile ->
        ensureRequired(settings, profile)
    }

    fun ensureRequired(
        settings: MeasurementSettings,
        profile: UserProfile,
    ): EffectiveMeasurementSettings {
        val dependencies = dependencyResolver.resolve(
            settings.enabledAnalysisMethods,
            profile,
        )
        val effectiveSettings = settings.copy(
            enabledMeasurements = settings.enabledMeasurements + dependencies.requiredMeasurements,
        )
        return EffectiveMeasurementSettings(
            settings = effectiveSettings,
            dependencies = dependencies,
        )
    }

    suspend fun ensureRequiredWithCurrentProfile(
        settings: MeasurementSettings,
    ): EffectiveMeasurementSettings {
        val profile = profileRepository.requiredProfileFlow.first()
        return ensureRequired(settings, profile)
    }
}
