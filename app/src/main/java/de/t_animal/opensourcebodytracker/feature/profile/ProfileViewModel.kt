package de.t_animal.opensourcebodytracker.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
                repository.profileFlow.collect { profile ->
                    if (profile != null && !didInitializeFromRepo) {
                        didInitializeFromRepo = true
                        _uiState.value = _uiState.value.copy(
                            sex = profile.sex,
                            dateOfBirthText = epochMillisToLocalDate(profile.dateOfBirthEpochMillis).toString(),
                            heightCmText = profile.heightCm.toString(),
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

        val height = current.heightCmText.trim().replace(',', '.').toFloatOrNull()
        if (height == null || height <= 0f) {
            _uiState.value = current.copy(errorMessage = "Height must be a positive number")
            return
        }

        viewModelScope.launch {
            repository.saveProfile(
                UserProfile(
                    sex = sex,
                    dateOfBirthEpochMillis = localDateToEpochMillis(date),
                    heightCm = height,
                ),
            )
            _events.emit(ProfileEvent.Saved)
        }
    }
}

class ProfileViewModelFactory(
    private val repository: ProfileRepository,
    private val mode: ProfileMode,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(repository = repository, mode = mode) as T
    }
}

private fun localDateToEpochMillis(localDate: LocalDate): Long {
    return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun epochMillisToLocalDate(epochMillis: Long): LocalDate {
    return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
}
