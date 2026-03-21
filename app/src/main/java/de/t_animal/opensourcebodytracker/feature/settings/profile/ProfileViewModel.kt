package de.t_animal.opensourcebodytracker.feature.settings.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import de.t_animal.opensourcebodytracker.core.model.MeasurementSettings
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import de.t_animal.opensourcebodytracker.core.util.formatDecimalForInput
import de.t_animal.opensourcebodytracker.core.util.parseLocalizedFloatOrNull
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.GeneralSettingsRepository
import de.t_animal.opensourcebodytracker.data.settings.MeasurementSettingsRepository
import de.t_animal.opensourcebodytracker.domain.metrics.DerivedMetricsDependencyResolver
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ProfileUiState(
    val mode: ProfileMode,
    val sex: Sex? = null,
    val dateOfBirthText: String = "",
    val heightCmText: String = "",
    val errorMessage: String? = null,
)

sealed interface ProfileEvent {
    data object Saved : ProfileEvent
}

@HiltViewModel(assistedFactory = ProfileViewModel.Factory::class)
class ProfileViewModel @AssistedInject constructor(
    @Assisted val mode: ProfileMode,
    private val repository: ProfileRepository,
    private val measurementSettingsRepository: MeasurementSettingsRepository,
    private val generalSettingsRepository: GeneralSettingsRepository,
    private val dependencyResolver: DerivedMetricsDependencyResolver,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(mode: ProfileMode): ProfileViewModel
    }

    private val _uiState = MutableStateFlow(ProfileUiState(mode = mode))
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
                        _uiState.value = _uiState.value.copy(
                            sex = profile.sex,
                            dateOfBirthText = profile.dateOfBirth.toString(),
                            heightCmText = formatDecimalForInput(profile.heightCm.toDouble()),
                            errorMessage = null,
                        )
                    }
                }
            }
        }
    }

    fun onSexChanged(sex: Sex) {
        _uiState.value = _uiState.value.copy(sex = sex, errorMessage = null)
    }

    fun onDateOfBirthChanged(text: String) {
        _uiState.value = _uiState.value.copy(dateOfBirthText = text, errorMessage = null)
    }

    fun onHeightChanged(text: String) {
        _uiState.value = _uiState.value.copy(heightCmText = text, errorMessage = null)
    }

    fun onSaveClicked() {
        val current = _uiState.value
        val sex = current.sex ?: run {
            _uiState.value = current.copy(errorMessage = "Please select a sex")
            return
        }

        val date = runCatching { LocalDate.parse(current.dateOfBirthText.trim()) }.getOrNull()
        if (date == null) {
            _uiState.value = current.copy(errorMessage = "Date of birth must be YYYY-MM-DD")
            return
        }

        val height = parseLocalizedFloatOrNull(current.heightCmText)
        if (height == null || height <= 0f) {
            _uiState.value = current.copy(errorMessage = "Height must be a positive number")
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
            val requiredAwareMeasurementSettings = ensureRequiredMeasurementsEnabled(currentMeasurementSettings, profile)
            if (requiredAwareMeasurementSettings != currentMeasurementSettings) {
                measurementSettingsRepository.saveSettings(requiredAwareMeasurementSettings)
            }

            if (mode == ProfileMode.Onboarding) {
                generalSettingsRepository.updateSettings {
                    it.copy(
                        onboardingCompleted = false,
                        isDemoMode = false,
                    )
                }
            }

            _events.emit(ProfileEvent.Saved)
        }
    }

    private fun ensureRequiredMeasurementsEnabled(
        settings: MeasurementSettings,
        profile: UserProfile,
    ): MeasurementSettings {
        val requiredMeasurements = dependencyResolver
            .resolve(settings.enabledAnalysisMethods, profile)
            .requiredMeasurements

        val missingRequiredMeasurements = requiredMeasurements - settings.enabledMeasurements
        if (missingRequiredMeasurements.isEmpty()) {
            return settings
        }

        return settings.copy(
            enabledMeasurements = settings.enabledMeasurements + missingRequiredMeasurements,
        )
    }
}
