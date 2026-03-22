package de.t_animal.opensourcebodytracker.feature.settings.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.GeneralSettingsRepository
import de.t_animal.opensourcebodytracker.domain.demodata.DefaultDemoDataLeanBodyWeightKg
import de.t_animal.opensourcebodytracker.domain.demodata.DefaultDemoDataMaxFatBodyWeightKg
import de.t_animal.opensourcebodytracker.domain.demodata.DefaultDemoDataMinFatBodyWeightKg
import de.t_animal.opensourcebodytracker.domain.demodata.GenerateDemoDataUseCase
import de.t_animal.opensourcebodytracker.domain.demodata.defaultDemoDataProfile
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingStartViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val generalSettingsRepository: GeneralSettingsRepository,
    private val generateDemoDataUseCase: GenerateDemoDataUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingStartUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<OnboardingStartEvent>()
    val events = _events.asSharedFlow()

    fun onTryDemoDataClicked() {
        if (_uiState.value.isBusy) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, hasError = false)
            runCatching {
                seedDemoData(
                    profile = defaultDemoDataProfile(),
                )
                _events.emit(OnboardingStartEvent.DemoModeInitializationCompleted)
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(hasError = true)
            }
            _uiState.value = _uiState.value.copy(isBusy = false)
        }
    }

    private suspend fun seedDemoData(profile: UserProfile) {
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

data class OnboardingStartUiState(
    val isBusy: Boolean = false,
    val hasError: Boolean = false,
)

sealed interface OnboardingStartEvent {
    data object DemoModeInitializationCompleted : OnboardingStartEvent
}

