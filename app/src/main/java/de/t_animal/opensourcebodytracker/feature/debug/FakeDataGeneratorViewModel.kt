package de.t_animal.opensourcebodytracker.feature.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.domain.demodata.DefaultDemoDataLeanBodyWeightKg
import de.t_animal.opensourcebodytracker.domain.demodata.DefaultDemoDataMaxFatBodyWeightKg
import de.t_animal.opensourcebodytracker.domain.demodata.DefaultDemoDataMinFatBodyWeightKg
import de.t_animal.opensourcebodytracker.domain.demodata.GenerateDemoDataUseCase
import de.t_animal.opensourcebodytracker.domain.demodata.defaultDemoDataProfile
import java.text.DecimalFormatSymbols
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FakeDataGeneratorUiState(
    val isGenerating: Boolean = false,
    val leanBodyWeightKgText: String = DefaultDemoDataLeanBodyWeightKg.toString(),
    val minFatBodyWeightKgText: String = DefaultDemoDataMinFatBodyWeightKg.toString(),
    val maxFatBodyWeightKgText: String = DefaultDemoDataMaxFatBodyWeightKg.toString(),
    val inputError: String? = null,
)

class FakeDataGeneratorViewModel(
    private val profileRepository: ProfileRepository,
    private val generateDemoDataUseCase: GenerateDemoDataUseCase,
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
                val profile = defaultDemoDataProfile()
                profileRepository.saveProfile(profile)
                generateDemoDataUseCase(
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

class FakeDataGeneratorViewModelFactory(
    private val profileRepository: ProfileRepository,
    private val generateDemoDataUseCase: GenerateDemoDataUseCase,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FakeDataGeneratorViewModel(
            profileRepository = profileRepository,
            generateDemoDataUseCase = generateDemoDataUseCase,
        ) as T
    }
}
