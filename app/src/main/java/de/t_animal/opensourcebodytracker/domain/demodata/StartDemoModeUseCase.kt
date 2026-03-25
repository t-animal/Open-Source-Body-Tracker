package de.t_animal.opensourcebodytracker.domain.demodata

import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.GeneralSettingsRepository
import javax.inject.Inject

class StartDemoModeUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val generalSettingsRepository: GeneralSettingsRepository,
    private val generateDemoDataUseCase: GenerateDemoDataUseCase,
) {
    suspend operator fun invoke() {
        val profile = defaultDemoDataProfile()
        profileRepository.saveProfile(profile)

        generateDemoDataUseCase(
            profile = profile,
            leanBodyWeightKg = DefaultDemoDataLeanBodyWeightKg,
            minFatBodyWeightKg = DefaultDemoDataMinFatBodyWeightKg,
            maxFatBodyWeightKg = DefaultDemoDataMaxFatBodyWeightKg,
        )

        generalSettingsRepository.updateSettings {
            it.copy(onboardingCompleted = true, isDemoMode = true)
        }
    }
}
