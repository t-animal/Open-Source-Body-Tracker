package de.t_animal.opensourcebodytracker.feature.settings.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import de.t_animal.opensourcebodytracker.core.model.ReminderSettings
import de.t_animal.opensourcebodytracker.core.model.ReminderValidationError
import de.t_animal.opensourcebodytracker.data.settings.GeneralSettingsRepository
import de.t_animal.opensourcebodytracker.data.settings.ReminderSettingsRepository
import de.t_animal.opensourcebodytracker.domain.reminders.SaveReminderSettingsUseCase
import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface ReminderSettingsUiState {
    val mode: ReminderMode

    data class Loading(override val mode: ReminderMode) : ReminderSettingsUiState

    data class Loaded(
        override val mode: ReminderMode,
        val enabled: Boolean,
        val weekdays: Set<DayOfWeek>,
        val time: LocalTime,
        val validationError: ReminderValidationError?,
    ) : ReminderSettingsUiState
}

sealed interface ReminderSettingsEvent {
    data object Saved : ReminderSettingsEvent
}

@HiltViewModel(assistedFactory = ReminderSettingsViewModel.Factory::class)
class ReminderSettingsViewModel @AssistedInject constructor(
    @Assisted val mode: ReminderMode,
    private val reminderSettingsRepository: ReminderSettingsRepository,
    private val generalSettingsRepository: GeneralSettingsRepository,
    private val saveReminderSettingsUseCase: SaveReminderSettingsUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(mode: ReminderMode): ReminderSettingsViewModel
    }

    private val _uiState = MutableStateFlow<ReminderSettingsUiState>(ReminderSettingsUiState.Loading(mode))
    val uiState: StateFlow<ReminderSettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ReminderSettingsEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val reminderSettings = reminderSettingsRepository.settingsFlow.first()
            _uiState.value = ReminderSettingsUiState.Loaded(
                mode = mode,
                enabled = reminderSettings.reminderEnabled,
                weekdays = reminderSettings.reminderWeekdays,
                time = reminderSettings.reminderTime,
                validationError = null,
            )
        }
    }

    fun onEnabledChanged(enabled: Boolean) {
        updateLoadedState { it.copy(enabled = enabled, validationError = null) }
    }

    fun onWeekdayToggled(dayOfWeek: DayOfWeek) {
        updateLoadedState {
            val updatedWeekdays = if (dayOfWeek in it.weekdays) {
                it.weekdays - dayOfWeek
            } else {
                it.weekdays + dayOfWeek
            }
            it.copy(weekdays = updatedWeekdays, validationError = null)
        }
    }

    fun onTimeChanged(time: LocalTime) {
        updateLoadedState { it.copy(time = time, validationError = null) }
    }

    fun onPermissionDeniedWhileSaving() {
        updateLoadedState { it.copy(enabled = false, validationError = null) }
    }

    fun onSaveClicked() {
        val current = _uiState.value as? ReminderSettingsUiState.Loaded ?: return

        val settings = ReminderSettings(
            reminderEnabled = current.enabled,
            reminderWeekdays = current.weekdays,
            reminderTime = current.time,
        )

        val validationError = settings.validate()
        if (validationError != null) {
            _uiState.value = current.copy(validationError = validationError)
            return
        }

        viewModelScope.launch {
            saveReminderSettingsUseCase(settings)
            _events.emit(ReminderSettingsEvent.Saved)
        }
    }

    private fun updateLoadedState(transform: (ReminderSettingsUiState.Loaded) -> ReminderSettingsUiState.Loaded) {
        val current = _uiState.value as? ReminderSettingsUiState.Loaded ?: return
        _uiState.value = transform(current)
    }
}
