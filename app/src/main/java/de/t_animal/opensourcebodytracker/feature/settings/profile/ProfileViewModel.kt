package de.t_animal.opensourcebodytracker.feature.settings.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UnitSystem
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import de.t_animal.opensourcebodytracker.core.util.formatDecimalForInput
import de.t_animal.opensourcebodytracker.core.util.parseLocalizedFloatOrNull
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.GeneralSettingsRepository
import de.t_animal.opensourcebodytracker.data.settings.MeasurementSettingsRepository
import de.t_animal.opensourcebodytracker.domain.metrics.RequiredMeasurementsResolver
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface ProfileValidationError {
    data object MissingSex : ProfileValidationError
    data object InvalidDateOfBirth : ProfileValidationError
    data object InvalidHeight : ProfileValidationError
}

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
    private val measurementSettingsRepository: MeasurementSettingsRepository,
    private val generalSettingsRepository: GeneralSettingsRepository,
    private val requiredMeasurementsResolver: RequiredMeasurementsResolver,
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
        val sex = current.sex ?: run {
            _uiState.value = current.copy(validationError = ProfileValidationError.MissingSex)
            return
        }

        val date = runCatching { LocalDate.parse(current.dateOfBirthText.trim()) }.getOrNull()
        if (date == null) {
            _uiState.value = current.copy(validationError = ProfileValidationError.InvalidDateOfBirth)
            return
        }

        val height = parseLocalizedFloatOrNull(current.heightCmText)
        if (height == null || height <= 0f) {
            _uiState.value = current.copy(validationError = ProfileValidationError.InvalidHeight)
            return
        }

        viewModelScope.launch {
            val profile = UserProfile(
                sex = sex,
                dateOfBirth = date,
                heightCm = height,
            )

            repository.saveProfile(
                profile,
            )

            val currentMeasurementSettings = measurementSettingsRepository.settingsFlow.first()
            val effective = requiredMeasurementsResolver.ensureRequired(currentMeasurementSettings, profile)
            if (effective.settings != currentMeasurementSettings) {
                measurementSettingsRepository.saveSettings(effective.settings)
            }

            _events.emit(ProfileEvent.Saved)
        }
    }
}
