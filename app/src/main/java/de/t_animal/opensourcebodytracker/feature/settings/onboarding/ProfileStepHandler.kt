package de.t_animal.opensourcebodytracker.feature.settings.onboarding

import de.t_animal.opensourcebodytracker.core.model.ProfileParseResult
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UnitSystem
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class ProfileStepHandler(
    private val uiState: MutableStateFlow<OnboardingUiState>,
    private val events: MutableSharedFlow<OnboardingEvent>,
    private val coroutineScope: CoroutineScope,
    private val analysisStepHandler: AnalysisStepHandler,
) {

    var validatedProfile: UserProfile? = null
        private set

    fun onSexChanged(sex: Sex) {
        uiState.value = uiState.value.copy(
            profile = uiState.value.profile.copy(sex = sex, validationError = null),
        )
    }

    fun onDateOfBirthChanged(text: String) {
        uiState.value = uiState.value.copy(
            profile = uiState.value.profile.copy(dateOfBirthText = text, validationError = null),
        )
    }

    fun onHeightCmChanged(text: String) {
        uiState.value = uiState.value.copy(
            profile = uiState.value.profile.copy(heightCmText = text, validationError = null),
        )
    }

    fun onUnitSystemChanged(unitSystem: UnitSystem) {
        uiState.value = uiState.value.copy(
            profile = uiState.value.profile.copy(unitSystem = unitSystem),
        )
    }

    fun validateProfile() {
        val currentProfile = uiState.value.profile
        when (val result = UserProfile.parse(currentProfile.sex, currentProfile.dateOfBirthText, currentProfile.heightCmText)) {
            is ProfileParseResult.Error -> {
                uiState.value = uiState.value.copy(
                    profile = currentProfile.copy(validationError = result.error),
                )
            }
            is ProfileParseResult.Success -> {
                val profile = result.profile
                validatedProfile = profile
                analysisStepHandler.recomputeDependencies(profile)
                coroutineScope.launch {
                    events.emit(OnboardingEvent.ProfileValid)
                }
            }
        }
    }
}
