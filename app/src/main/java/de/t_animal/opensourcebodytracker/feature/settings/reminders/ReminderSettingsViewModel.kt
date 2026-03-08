package de.t_animal.opensourcebodytracker.feature.settings.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ReminderSettingsUiState(
    val isLoading: Boolean = true,
    val enabled: Boolean = false,
    val weekdays: Set<DayOfWeek> = setOf(DayOfWeek.SUNDAY),
    val time: LocalTime = LocalTime.of(9, 0),
    val errorMessage: String? = null,
)

sealed interface ReminderSettingsEvent {
    data object Saved : ReminderSettingsEvent
}

class ReminderSettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReminderSettingsUiState())
    val uiState: StateFlow<ReminderSettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ReminderSettingsEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settingsFlow.first()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                enabled = settings.reminderEnabled,
                weekdays = settings.reminderWeekdays,
                time = settings.reminderTime,
                errorMessage = null,
            )
        }
    }

    fun onEnabledChanged(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            enabled = enabled,
            errorMessage = null,
        )
    }

    fun onWeekdayToggled(dayOfWeek: DayOfWeek) {
        val currentWeekdays = _uiState.value.weekdays
        val updatedWeekdays = if (dayOfWeek in currentWeekdays) {
            currentWeekdays - dayOfWeek
        } else {
            currentWeekdays + dayOfWeek
        }

        _uiState.value = _uiState.value.copy(
            weekdays = updatedWeekdays,
            errorMessage = null,
        )
    }

    fun onTimeChanged(time: LocalTime) {
        _uiState.value = _uiState.value.copy(
            time = time,
            errorMessage = null,
        )
    }

    fun onSaveClicked() {
        val current = _uiState.value
        if (current.isLoading) {
            return
        }

        if (current.enabled && current.weekdays.isEmpty()) {
            _uiState.value = current.copy(errorMessage = "Select at least one weekday")
            return
        }

        viewModelScope.launch {
            val settings = settingsRepository.settingsFlow.first()
            val updatedSettings = settings.copy(
                reminderEnabled = current.enabled,
                reminderWeekdays = current.weekdays,
                reminderTime = current.time,
            )

            if (updatedSettings != settings) {
                settingsRepository.saveSettings(updatedSettings)
            }

            _events.emit(ReminderSettingsEvent.Saved)
        }
    }
}

class ReminderSettingsViewModelFactory(
    private val settingsRepository: SettingsRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReminderSettingsViewModel(
            settingsRepository = settingsRepository,
        ) as T
    }
}
