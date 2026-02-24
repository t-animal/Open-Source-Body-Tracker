package de.t_animal.opensourcebodytracker.feature.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.domain.measurements.GenerateFakeMeasurementsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class FakeDataGeneratorUiState(
    val isGenerating: Boolean = false,
)

class FakeDataGeneratorViewModel(
    private val profileRepository: ProfileRepository,
    private val generateFakeMeasurementsUseCase: GenerateFakeMeasurementsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FakeDataGeneratorUiState())
    val uiState: StateFlow<FakeDataGeneratorUiState> = _uiState.asStateFlow()

    fun onGenerateClicked() {
        if (_uiState.value.isGenerating) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGenerating = true)
            try {
                val profile = profileRepository.profileFlow.first()
                generateFakeMeasurementsUseCase(profile?.sex)
            } finally {
                _uiState.value = _uiState.value.copy(isGenerating = false)
            }
        }
    }
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