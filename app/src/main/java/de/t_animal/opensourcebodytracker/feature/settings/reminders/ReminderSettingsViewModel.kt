package de.t_animal.opensourcebodytracker.feature.settings.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import de.t_animal.opensourcebodytracker.core.notifications.ReminderAlarmScheduler
import de.t_animal.opensourcebodytracker.core.model.ReminderSettings
import de.t_animal.opensourcebodytracker.data.settings.GeneralSettingsRepository
import de.t_animal.opensourcebodytracker.data.settings.ReminderSettingsRepository
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
    val mode: ReminderMode,
    val isLoading: Boolean = true,
    val enabled: Boolean = false,
    val weekdays: Set<DayOfWeek> = setOf(DayOfWeek.SUNDAY),
    val time: LocalTime = LocalTime.of(9, 0),
    val errorMessage: String? = null,
)

sealed interface ReminderSettingsEvent {
    data object Saved : ReminderSettingsEvent
}

@HiltViewModel(assistedFactory = ReminderSettingsViewModel.Factory::class)
class ReminderSettingsViewModel @AssistedInject constructor(
    @Assisted val mode: ReminderMode,
    private val reminderSettingsRepository: ReminderSettingsRepository,
    private val generalSettingsRepository: GeneralSettingsRepository,
    private val reminderAlarmScheduler: ReminderAlarmScheduler,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(mode: ReminderMode): ReminderSettingsViewModel
    }

    private val _uiState = MutableStateFlow(ReminderSettingsUiState(mode = mode))
    val uiState: StateFlow<ReminderSettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ReminderSettingsEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val reminderSettings = reminderSettingsRepository.settingsFlow.first()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                enabled = reminderSettings.reminderEnabled,
                weekdays = reminderSettings.reminderWeekdays,
                time = reminderSettings.reminderTime,
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

    fun onPermissionDeniedWhileSaving() {
        _uiState.value = _uiState.value.copy(
            enabled = false,
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
            val currentSettings = reminderSettingsRepository.settingsFlow.first()
            val updatedSettings = ReminderSettings(
                reminderEnabled = current.enabled,
                reminderWeekdays = current.weekdays,
                reminderTime = current.time,
            )

            if (updatedSettings != currentSettings) {
                reminderSettingsRepository.saveSettings(updatedSettings)
            }

            if (mode == ReminderMode.Onboarding) {
                generalSettingsRepository.updateSettings {
                    it.copy(onboardingCompleted = true, isDemoMode = false)
                }
            }

            reminderAlarmScheduler.syncWithSettings(updatedSettings)

            _events.emit(ReminderSettingsEvent.Saved)
        }
    }
}
