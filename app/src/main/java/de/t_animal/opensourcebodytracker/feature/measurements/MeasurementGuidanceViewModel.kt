package de.t_animal.opensourcebodytracker.feature.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class MeasurementGuidanceUiState(val sex: Sex?)

@HiltViewModel
class MeasurementGuidanceViewModel @Inject constructor(
    profileRepository: ProfileRepository,
) : ViewModel() {

    val uiState: StateFlow<MeasurementGuidanceUiState> =
        profileRepository.profileFlow
            .map { profile -> MeasurementGuidanceUiState(sex = profile?.sex) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = MeasurementGuidanceUiState(sex = null),
            )
}
