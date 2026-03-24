package de.t_animal.opensourcebodytracker.feature.settings.onboarding

import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.GeneralSettingsRepository
import de.t_animal.opensourcebodytracker.domain.demodata.DefaultDemoDataLeanBodyWeightKg
import de.t_animal.opensourcebodytracker.domain.demodata.DefaultDemoDataMaxFatBodyWeightKg
import de.t_animal.opensourcebodytracker.domain.demodata.DefaultDemoDataMinFatBodyWeightKg
import de.t_animal.opensourcebodytracker.domain.demodata.GenerateDemoDataUseCase
import de.t_animal.opensourcebodytracker.domain.demodata.defaultDemoDataProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class StartStepHandler(
    private val uiState: MutableStateFlow<OnboardingUiState>,
    private val events: MutableSharedFlow<OnboardingEvent>,
    private val coroutineScope: CoroutineScope,
    private val profileRepository: ProfileRepository,
    private val generalSettingsRepository: GeneralSettingsRepository,
    private val generateDemoDataUseCase: GenerateDemoDataUseCase,
) {

    fun onTryDemoDataClicked() {
        if (uiState.value.start.isDemoModeBusy) return

        coroutineScope.launch {
            uiState.value = uiState.value.copy(
                start = StartStepState(isDemoModeBusy = true, demoModeError = false),
            )
            runCatching {
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
                events.emit(OnboardingEvent.DemoModeCompleted)
            }.onFailure {
                uiState.value = uiState.value.copy(
                    start = uiState.value.start.copy(demoModeError = true),
                )
            }
            uiState.value = uiState.value.copy(
                start = uiState.value.start.copy(isDemoModeBusy = false),
            )
        }
    }
}
