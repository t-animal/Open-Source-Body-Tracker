package de.t_animal.opensourcebodytracker.feature.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.domain.measurements.GenerateFakeMeasurementsUseCase
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FakeDataGeneratorUiState(
    val isGenerating: Boolean = false,
    val leanBodyWeightKgText: String = "67.0",
    val minFatBodyWeightKgText: String = "8.0",
    val maxFatBodyWeightKgText: String = "20.0",
    val inputError: String? = null,
)

class FakeDataGeneratorViewModel(
    private val profileRepository: ProfileRepository,
    private val generateFakeMeasurementsUseCase: GenerateFakeMeasurementsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FakeDataGeneratorUiState())
    val uiState: StateFlow<FakeDataGeneratorUiState> = _uiState.asStateFlow()

    fun onLeanBodyWeightChanged(value: String) {
        _uiState.value = _uiState.value.copy(leanBodyWeightKgText = value, inputError = null)
    }

    fun onMinFatBodyWeightChanged(value: String) {
        _uiState.value = _uiState.value.copy(minFatBodyWeightKgText = value, inputError = null)
    }

    fun onMaxFatBodyWeightChanged(value: String) {
        _uiState.value = _uiState.value.copy(maxFatBodyWeightKgText = value, inputError = null)
    }

    fun onGenerateClicked() {
        if (_uiState.value.isGenerating) return

        val leanBodyWeightKg = parseDoubleOrNull(_uiState.value.leanBodyWeightKgText)
        val minFatBodyWeightKg = parseDoubleOrNull(_uiState.value.minFatBodyWeightKgText)
        val maxFatBodyWeightKg = parseDoubleOrNull(_uiState.value.maxFatBodyWeightKgText)

        if (leanBodyWeightKg == null || minFatBodyWeightKg == null || maxFatBodyWeightKg == null) {
            _uiState.value = _uiState.value.copy(inputError = "Please enter valid numeric values")
            return
        }
        if (leanBodyWeightKg <= 0.0 || minFatBodyWeightKg < 0.0 || maxFatBodyWeightKg < 0.0) {
            _uiState.value = _uiState.value.copy(inputError = "Lean must be > 0, fat values must be >= 0")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGenerating = true, inputError = null)
            try {
                val profile = defaultFakeProfile()
                profileRepository.saveProfile(profile)
                generateFakeMeasurementsUseCase(
                    profile = profile,
                    leanBodyWeightKg = leanBodyWeightKg,
                    minFatBodyWeightKg = minFatBodyWeightKg,
                    maxFatBodyWeightKg = maxFatBodyWeightKg,
                )
            } finally {
                _uiState.value = _uiState.value.copy(isGenerating = false)
            }
        }
    }
}

private fun parseDoubleOrNull(text: String): Double? {
    val trimmed = text.trim()
    if (trimmed.isBlank()) return null
    val decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator
    return trimmed
        .replace(decimalSeparator, '.')
        .replace(',', '.')
        .toDoubleOrNull()
}

private fun defaultFakeProfile(): UserProfile {
    val dateOfBirthMillis = LocalDate.of(1990, 2, 14)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    return UserProfile(
        sex = Sex.Male,
        dateOfBirthEpochMillis = dateOfBirthMillis,
        heightCm = 180f,
    )
}

class FakeDataGeneratorViewModelFactory(
    private val profileRepository: ProfileRepository,
    private val generateFakeMeasurementsUseCase: GenerateFakeMeasurementsUseCase,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FakeDataGeneratorViewModel(
            profileRepository = profileRepository,
            generateFakeMeasurementsUseCase = generateFakeMeasurementsUseCase,
        ) as T
    }
}