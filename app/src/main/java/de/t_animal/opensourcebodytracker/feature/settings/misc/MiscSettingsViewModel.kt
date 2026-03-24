package de.t_animal.opensourcebodytracker.feature.settings.misc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.t_animal.opensourcebodytracker.core.model.PhotoQuality
import de.t_animal.opensourcebodytracker.core.model.UnitSystem
import de.t_animal.opensourcebodytracker.data.settings.GeneralSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface MiscSettingsUiState {
    data object Loading : MiscSettingsUiState

    data class Loaded(
        val unitSystem: UnitSystem,
        val photoQuality: PhotoQuality,
    ) : MiscSettingsUiState
}

@HiltViewModel
class MiscSettingsViewModel @Inject constructor(
    private val generalSettingsRepository: GeneralSettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<MiscSettingsUiState> = generalSettingsRepository.settingsFlow
        .map<_, MiscSettingsUiState> { settings ->
            MiscSettingsUiState.Loaded(
                unitSystem = settings.unitSystem,
                photoQuality = settings.photoQuality,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MiscSettingsUiState.Loading,
        )

    fun onUnitSystemChanged(unitSystem: UnitSystem) {
        viewModelScope.launch {
            generalSettingsRepository.updateSettings { it.copy(unitSystem = unitSystem) }
        }
    }

    fun onPhotoQualityChanged(photoQuality: PhotoQuality) {
        viewModelScope.launch {
            generalSettingsRepository.updateSettings { it.copy(photoQuality = photoQuality) }
        }
    }
}
