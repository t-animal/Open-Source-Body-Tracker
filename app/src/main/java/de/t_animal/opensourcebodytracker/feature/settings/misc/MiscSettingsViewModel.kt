package de.t_animal.opensourcebodytracker.feature.settings.misc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.t_animal.opensourcebodytracker.core.model.UnitSystem
import de.t_animal.opensourcebodytracker.data.settings.GeneralSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MiscSettingsViewModel @Inject constructor(
    private val generalSettingsRepository: GeneralSettingsRepository,
) : ViewModel() {

    val unitSystem: StateFlow<UnitSystem> = generalSettingsRepository.settingsFlow
        .map { it.unitSystem }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UnitSystem.Metric,
        )

    fun onUnitSystemChanged(unitSystem: UnitSystem) {
        viewModelScope.launch {
            generalSettingsRepository.updateSettings { it.copy(unitSystem = unitSystem) }
        }
    }
}
