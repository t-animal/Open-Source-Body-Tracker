package de.t_animal.opensourcebodytracker.feature.settings.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.demodata.DefaultDemoDataLeanBodyWeightKg
import de.t_animal.opensourcebodytracker.domain.demodata.DefaultDemoDataMaxFatBodyWeightKg
import de.t_animal.opensourcebodytracker.domain.demodata.DefaultDemoDataMinFatBodyWeightKg
import de.t_animal.opensourcebodytracker.domain.demodata.GenerateDemoDataUseCase
import de.t_animal.opensourcebodytracker.domain.demodata.defaultDemoDataProfile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class OnboardingStartViewModel(
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val generateDemoDataUseCase: GenerateDemoDataUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingStartUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<OnboardingStartEvent>()
    val events = _events.asSharedFlow()

    fun onTryDemoDataClicked() {
        if (_uiState.value.isBusy) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, errorMessage = null)
            runCatching {
                seedDemoData(
                    profile = defaultDemoDataProfile(),
                )
                _events.emit(OnboardingStartEvent.DemoModeInitializationCompleted)
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = throwable.message ?: "Could not generate demo data",
                )
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

        val refreshedSettings = settingsRepository.settingsFlow.first()
        settingsRepository.saveSettings(
            refreshedSettings.copy(
                onboardingCompleted = true,
                isDemoMode = true,
            ),
        )
    }
}

data class OnboardingStartUiState(
    val isBusy: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface OnboardingStartEvent {
    data object DemoModeInitializationCompleted : OnboardingStartEvent
}

class OnboardingStartViewModelFactory(
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val generateDemoDataUseCase: GenerateDemoDataUseCase,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return OnboardingStartViewModel(
            profileRepository = profileRepository,
            settingsRepository = settingsRepository,
            generateDemoDataUseCase = generateDemoDataUseCase,
        ) as T
    }
}
