package de.t_animal.opensourcebodytracker.feature.settings.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.SettingsState
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.metrics.DerivedMetricsDependencyResolver
import de.t_animal.opensourcebodytracker.domain.metrics.enabledAnalysisMethods
import java.text.DecimalFormatSymbols
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

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val dependencyResolver: DerivedMetricsDependencyResolver,
    private val mode: ProfileMode,
) : ViewModel() {
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

        val height = parseFloatOrNull(current.heightCmText)
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

            val currentSettings = settingsRepository.settingsFlow.first()
            val requiredAwareSettings = ensureRequiredMeasurementsEnabled(currentSettings, profile)
            val settingsToSave = if (mode == ProfileMode.Onboarding) {
                requiredAwareSettings.copy(
                    onboardingCompleted = false,
                    isDemoMode = false,
                )
            } else {
                requiredAwareSettings
            }

            if (settingsToSave != currentSettings) {
                settingsRepository.saveSettings(settingsToSave)
            }

            _events.emit(ProfileEvent.Saved)
        }
    }

    private fun ensureRequiredMeasurementsEnabled(
        settings: SettingsState,
        profile: UserProfile,
    ): SettingsState {
        val requiredMeasurements = dependencyResolver
            .resolve(settings.enabledAnalysisMethods(), profile)
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

class ProfileViewModelFactory(
    private val repository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val dependencyResolver: DerivedMetricsDependencyResolver,
    private val mode: ProfileMode,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(
            repository = repository,
            settingsRepository = settingsRepository,
            dependencyResolver = dependencyResolver,
            mode = mode,
        ) as T
    }
}

private fun parseFloatOrNull(text: String): Float? {
    val trimmed = text.trim()
    if (trimmed.isBlank()) return null
    val decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator
    return trimmed
        .replace(decimalSeparator, '.')
        .replace(',', '.')
        .toFloatOrNull()
}

private fun formatDecimalForInput(value: Double): String {
    val decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator
    val text = value.toString()
    return if (decimalSeparator == '.') text else text.replace('.', decimalSeparator)
}