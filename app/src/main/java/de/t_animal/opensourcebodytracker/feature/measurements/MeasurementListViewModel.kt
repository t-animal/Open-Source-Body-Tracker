package de.t_animal.opensourcebodytracker.feature.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class MeasurementListUiState(
    val measurements: List<BodyMeasurement> = emptyList(),
)

class MeasurementListViewModel(
    repository: MeasurementRepository,
) : ViewModel() {
    val uiState: StateFlow<MeasurementListUiState> = repository.observeAll()
        .map { list -> MeasurementListUiState(measurements = list) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MeasurementListUiState(),
        )
}

class MeasurementListViewModelFactory(
    private val repository: MeasurementRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MeasurementListViewModel(repository) as T
    }
}
