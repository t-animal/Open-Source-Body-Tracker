package de.t_animal.opensourcebodytracker.feature.settings.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import de.t_animal.opensourcebodytracker.core.model.ProfileParseResult
import de.t_animal.opensourcebodytracker.core.model.ProfileValidationError
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UnitSystem
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import de.t_animal.opensourcebodytracker.core.util.formatDecimalForInput
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.GeneralSettingsRepository
import de.t_animal.opensourcebodytracker.domain.SaveProfileUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    val mode: ProfileMode

    data class Loading(override val mode: ProfileMode) : ProfileUiState

    data class Loaded(
        override val mode: ProfileMode,
        val sex: Sex?,
        val dateOfBirthText: String,
        val heightCmText: String,
        val unitSystem: UnitSystem,
        val validationError: ProfileValidationError?,
    ) : ProfileUiState
}

sealed interface ProfileEvent {
    data object Saved : ProfileEvent
}

@HiltViewModel(assistedFactory = ProfileViewModel.Factory::class)
class ProfileViewModel @AssistedInject constructor(
    @Assisted val mode: ProfileMode,
    private val repository: ProfileRepository,
    private val generalSettingsRepository: GeneralSettingsRepository,
    private val saveProfileUseCase: SaveProfileUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(mode: ProfileMode): ProfileViewModel
    }

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading(mode))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events = _events.asSharedFlow()

    private var didInitializeFromRepo = false

    init {
        if (mode == ProfileMode.Settings) {
            viewModelScope.launch {
                repository.requiredProfileFlow.collect { profile ->
                    if (!didInitializeFromRepo) {
                        didInitializeFromRepo = true
                        val unitSystem = generalSettingsRepository.settingsFlow.first().unitSystem
                        _uiState.value = ProfileUiState.Loaded(
                            mode = mode,
                            sex = profile.sex,
                            dateOfBirthText = profile.dateOfBirth.toString(),
                            heightCmText = formatDecimalForInput(profile.heightCm.toDouble()),
                            unitSystem = unitSystem,
                            validationError = null,
                        )
                    }
                }
            }
        } else {
            viewModelScope.launch {
                val unitSystem = generalSettingsRepository.settingsFlow.first().unitSystem
                _uiState.value = ProfileUiState.Loaded(
                    mode = mode,
                    sex = null,
                    dateOfBirthText = "",
                    heightCmText = "",
                    unitSystem = unitSystem,
                    validationError = null,
                )
            }
        }

        viewModelScope.launch {
            generalSettingsRepository.settingsFlow.collect { settings ->
                updateLoadedState { it.copy(unitSystem = settings.unitSystem) }
            }
        }
    }

    fun onSexChanged(sex: Sex) {
        updateLoadedState { it.copy(sex = sex, validationError = null) }
    }

    fun onDateOfBirthChanged(text: String) {
        updateLoadedState { it.copy(dateOfBirthText = text, validationError = null) }
    }

    fun onHeightCmChanged(text: String) {
        updateLoadedState { it.copy(heightCmText = text, validationError = null) }
    }

    private fun updateLoadedState(transform: (ProfileUiState.Loaded) -> ProfileUiState.Loaded) {
        val current = _uiState.value as? ProfileUiState.Loaded ?: return
        _uiState.value = transform(current)
    }

    fun onUnitSystemChanged(unitSystem: UnitSystem) {
        viewModelScope.launch {
            generalSettingsRepository.updateSettings { it.copy(unitSystem = unitSystem) }
        }
    }

    fun onSaveClicked() {
        val current = _uiState.value as? ProfileUiState.Loaded ?: return

        when (val result = UserProfile.parse(current.sex, current.dateOfBirthText, current.heightCmText)) {
            is ProfileParseResult.Error -> {
                _uiState.value = current.copy(validationError = result.error)
            }
            is ProfileParseResult.Success -> {
                viewModelScope.launch {
                    saveProfileUseCase(result.profile)
                    _events.emit(ProfileEvent.Saved)
                }
            }
        }
    }
}
