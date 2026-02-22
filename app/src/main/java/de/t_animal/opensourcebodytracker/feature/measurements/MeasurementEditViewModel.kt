package de.t_animal.opensourcebodytracker.feature.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MeasurementEditUiState(
    val measurementId: Long? = null,
    val dateEpochMillis: Long? = null,
    val dateText: String = "",
    val weightKgText: String = "",
    val neckCmText: String = "",
    val chestCmText: String = "",
    val waistCmText: String = "",
    val abdomenCmText: String = "",
    val hipCmText: String = "",
    val errorMessage: String? = null,
)

sealed interface MeasurementEditEvent {
    data object Saved : MeasurementEditEvent
}

class MeasurementEditViewModel(
    private val repository: MeasurementRepository,
    private val measurementId: Long?,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MeasurementEditUiState(measurementId = measurementId))
    val uiState: StateFlow<MeasurementEditUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MeasurementEditEvent>()
    val events = _events.asSharedFlow()

    init {
        if (measurementId != null) {
            viewModelScope.launch {
                val measurement = repository.getById(measurementId)
                if (measurement != null) {
                    _uiState.value = MeasurementEditUiState(
                        measurementId = measurement.id,
                        dateEpochMillis = measurement.dateEpochMillis,
                        dateText = formatDate(measurement.dateEpochMillis),
                        weightKgText = measurement.weightKg?.toString().orEmpty(),
                        neckCmText = measurement.neckCircumferenceCm?.toString().orEmpty(),
                        chestCmText = measurement.chestCircumferenceCm?.toString().orEmpty(),
                        waistCmText = measurement.waistCircumferenceCm?.toString().orEmpty(),
                        abdomenCmText = measurement.abdomenCircumferenceCm?.toString().orEmpty(),
                        hipCmText = measurement.hipCircumferenceCm?.toString().orEmpty(),
                    )
                }
            }
        } else {
            val now = System.currentTimeMillis()
            _uiState.value = _uiState.value.copy(
                dateEpochMillis = now,
                dateText = formatDate(now),
            )
        }
    }

    fun onWeightChanged(text: String) = update { it.copy(weightKgText = text, errorMessage = null) }

    fun onNeckChanged(text: String) = update { it.copy(neckCmText = text, errorMessage = null) }

    fun onChestChanged(text: String) = update { it.copy(chestCmText = text, errorMessage = null) }

    fun onWaistChanged(text: String) = update { it.copy(waistCmText = text, errorMessage = null) }

    fun onAbdomenChanged(text: String) = update { it.copy(abdomenCmText = text, errorMessage = null) }

    fun onHipChanged(text: String) = update { it.copy(hipCmText = text, errorMessage = null) }

    fun onSaveClicked() {
        val current = _uiState.value
        val date = current.dateEpochMillis ?: System.currentTimeMillis()

        val weight = parseDoubleOrNull(current.weightKgText)
        val neck = parseDoubleOrNull(current.neckCmText)
        val chest = parseDoubleOrNull(current.chestCmText)
        val waist = parseDoubleOrNull(current.waistCmText)
        val abdomen = parseDoubleOrNull(current.abdomenCmText)
        val hip = parseDoubleOrNull(current.hipCmText)

        val hasAnyValue = listOf(weight, neck, chest, waist, abdomen, hip).any { it != null }
        if (!hasAnyValue) {
            _uiState.value = current.copy(errorMessage = "Enter at least one value")
            return
        }

        viewModelScope.launch {
            if (measurementId == null) {
                repository.insert(
                    BodyMeasurement(
                        id = 0,
                        dateEpochMillis = date,
                        weightKg = weight,
                        neckCircumferenceCm = neck,
                        chestCircumferenceCm = chest,
                        waistCircumferenceCm = waist,
                        abdomenCircumferenceCm = abdomen,
                        hipCircumferenceCm = hip,
                    ),
                )
            } else {
                repository.update(
                    BodyMeasurement(
                        id = measurementId,
                        dateEpochMillis = date,
                        weightKg = weight,
                        neckCircumferenceCm = neck,
                        chestCircumferenceCm = chest,
                        waistCircumferenceCm = waist,
                        abdomenCircumferenceCm = abdomen,
                        hipCircumferenceCm = hip,
                    ),
                )
            }
            _events.emit(MeasurementEditEvent.Saved)
        }
    }

    private fun update(transform: (MeasurementEditUiState) -> MeasurementEditUiState) {
        _uiState.value = transform(_uiState.value)
    }
}

class MeasurementEditViewModelFactory(
    private val repository: MeasurementRepository,
    private val measurementId: Long?,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MeasurementEditViewModel(repository = repository, measurementId = measurementId) as T
    }
}

private fun parseDoubleOrNull(text: String): Double? {
    val trimmed = text.trim()
    if (trimmed.isBlank()) return null
    return trimmed.replace(',', '.').toDoubleOrNull()
}

private fun formatDate(epochMillis: Long): String {
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .toString()
}
