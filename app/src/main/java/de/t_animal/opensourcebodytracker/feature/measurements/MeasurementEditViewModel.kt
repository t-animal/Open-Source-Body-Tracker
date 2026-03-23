package de.t_animal.opensourcebodytracker.feature.measurements

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UnitSystem
import de.t_animal.opensourcebodytracker.core.model.userInputToStorageValue
import de.t_animal.opensourcebodytracker.core.photos.PersistedPhotoPath
import de.t_animal.opensourcebodytracker.core.photos.TemporaryCapturePhotoPath
import de.t_animal.opensourcebodytracker.core.util.formatEpochMillisAsIsoDate
import de.t_animal.opensourcebodytracker.core.util.parseLocalizedDoubleOrNull
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.GeneralSettingsRepository
import de.t_animal.opensourcebodytracker.domain.measurements.DeleteMeasurementCommand
import de.t_animal.opensourcebodytracker.domain.measurements.DeleteMeasurementResult
import de.t_animal.opensourcebodytracker.domain.measurements.DeleteMeasurementUseCase
import de.t_animal.opensourcebodytracker.domain.measurements.MeasurementMetricMapper
import de.t_animal.opensourcebodytracker.domain.measurements.MeasurementValidationError
import de.t_animal.opensourcebodytracker.domain.measurements.SaveMeasurementCommand
import de.t_animal.opensourcebodytracker.domain.measurements.SaveMeasurementResult
import de.t_animal.opensourcebodytracker.domain.measurements.SaveMeasurementUseCase
import de.t_animal.opensourcebodytracker.domain.metrics.RequiredMeasurementsResolver
import de.t_animal.opensourcebodytracker.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

sealed interface MeasurementEditError {
    data class Validation(val error: MeasurementValidationError) : MeasurementEditError
    data object DeleteFailed : MeasurementEditError
    data object SavePhotoFailed : MeasurementEditError
}

sealed interface MeasurementEditUiState {
    data object Loading : MeasurementEditUiState

    data class Loaded(
        val measurementId: Long? = null,
        val sex: Sex,
        val unitSystem: UnitSystem = UnitSystem.Metric,
        val enabledMeasurements: Set<MeasuredBodyMetric>,
        val dateEpochMillis: Long? = null,
        val dateText: String = "",
        val initialDateEpochMillis: Long? = null,
        val bodyMetricInputs: Map<MeasuredBodyMetric, String> = defaultBodyMetricInputs(),
        val initialMetricInputs: Map<MeasuredBodyMetric, String> = defaultBodyMetricInputs(),
        val note: String = "",
        val initialNote: String = "",
        val persistedPhotoFilePath: PersistedPhotoPath? = null,
        val initialPersistedPhotoFilePath: PersistedPhotoPath? = null,
        val pendingPhotoAbsolutePath: TemporaryCapturePhotoPath? = null,
        val isPhotoMarkedForDeletion: Boolean = false,
        val hasUnsavedChanges: Boolean = false,
        val isPhotoPreviewDialogVisible: Boolean = false,
        val error: MeasurementEditError? = null,
    ) : MeasurementEditUiState
}

sealed interface MeasurementEditEvent {
    data object Saved : MeasurementEditEvent
    data object Deleted : MeasurementEditEvent
}

@HiltViewModel
class MeasurementEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MeasurementRepository,
    private val photoStorage: InternalPhotoStorage,
    private val profileRepository: ProfileRepository,
    private val generalSettingsRepository: GeneralSettingsRepository,
    private val deleteMeasurementUseCase: DeleteMeasurementUseCase,
    private val saveMeasurementUseCase: SaveMeasurementUseCase,
    private val requiredMeasurementsResolver: RequiredMeasurementsResolver,
) : ViewModel() {
    private val measurementId: Long? = Routes.parseMeasurementEditId(savedStateHandle)
    private val _uiState = MutableStateFlow<MeasurementEditUiState>(MeasurementEditUiState.Loading)
    val uiState: StateFlow<MeasurementEditUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MeasurementEditEvent>()
    val events = _events.asSharedFlow()

    fun createPhotoCaptureTarget() = photoStorage.createTemporaryNewPhotoCaptureTarget()

    suspend fun clearTemporaryCapturePhotos() = photoStorage.clearTemporaryCapturePhotos()

    fun resolveTemporaryCapturePhotoFile(path: TemporaryCapturePhotoPath) = photoStorage.resolveTemporaryCapturePhotoFile(path)

    fun resolvePhotoFile(path: PersistedPhotoPath) = photoStorage.resolvePhotoFile(path)

    init {
        viewModelScope.launch {
            combine(
                profileRepository.requiredProfileFlow,
                requiredMeasurementsResolver.effectiveMeasurementSettingsFlow,
                observeExistingMeasurement(),
                generalSettingsRepository.settingsFlow,
            ) { profile, effective, measurement, generalSettings ->
                val sex = profile.sex
                val enabledMeasurements = effective.settings.enabledMeasurements
                val unitSystem = generalSettings.unitSystem

                val baseMeasurementId = measurementId
                if (baseMeasurementId == null && measurement != null) return@combine

                val currentLoaded = _uiState.value as? MeasurementEditUiState.Loaded
                _uiState.value = if (currentLoaded == null) {
                    buildInitialLoadedState(
                        sex = sex,
                        unitSystem = unitSystem,
                        enabledMeasurements = enabledMeasurements,
                        measurementId = baseMeasurementId,
                        measurement = measurement,
                    )
                } else {
                    currentLoaded.copy(
                        sex = sex,
                        unitSystem = unitSystem,
                        enabledMeasurements = enabledMeasurements,
                    )
                }
            }.collect {}
        }
    }

    private fun observeExistingMeasurement() = flow {
        emit(
            if (measurementId == null) {
                null
            } else {
                repository.getById(measurementId)
            },
        )
    }

    fun onMetricChanged(metric: MeasuredBodyMetric, text: String) {
        updateUiState {
            it.copy(
                bodyMetricInputs = it.bodyMetricInputs + (metric to text),
                error = null,
            )
        }
    }

    fun onDateChanged(epochMillis: Long) {
        updateUiState {
            it.copy(
                dateEpochMillis = epochMillis,
                dateText = formatEpochMillisAsIsoDate(epochMillis),
                error = null,
            )
        }
    }

    fun onNoteChanged(text: String) {
        updateUiState {
            it.copy(
                note = text,
                error = null,
            )
        }
    }

    fun onPhotoCaptured(pendingPhotoAbsolutePath: TemporaryCapturePhotoPath?) {
        val current = _uiState.value as? MeasurementEditUiState.Loaded ?: return
        val previousPhotoPath = current.pendingPhotoAbsolutePath
        val incomingPhotoPath = pendingPhotoAbsolutePath
        if (previousPhotoPath != null && previousPhotoPath != incomingPhotoPath) {
            viewModelScope.launch {
                photoStorage.deleteTemporaryCapturePhoto(previousPhotoPath)
            }
        }

        updateUiState {
            it.copy(
                pendingPhotoAbsolutePath = incomingPhotoPath,
                isPhotoMarkedForDeletion = false,
                isPhotoPreviewDialogVisible = false,
            )
        }
    }

    fun onDeletePhotoClicked() {
        updateUiState {
            it.copy(
                pendingPhotoAbsolutePath = null,
                isPhotoMarkedForDeletion = true,
                isPhotoPreviewDialogVisible = false,
            )
        }
    }

    fun onPhotoPreviewDialogVisibilityChanged(isVisible: Boolean) {
        updateUiState {
            it.copy(isPhotoPreviewDialogVisible = isVisible)
        }
    }

    fun onDeleteMeasurementClicked() {
        val current = _uiState.value as? MeasurementEditUiState.Loaded ?: return
        val currentMeasurementId = current.measurementId ?: return

        viewModelScope.launch {
            try {
                when (
                    deleteMeasurementUseCase(
                        DeleteMeasurementCommand(
                            measurementId = currentMeasurementId,
                            pendingPhotoPath = current.pendingPhotoAbsolutePath,
                            persistedPhotoPath = current.persistedPhotoFilePath,
                        ),
                    )
                ) {
                    DeleteMeasurementResult.Success -> _events.emit(MeasurementEditEvent.Deleted)
                }
            } catch (_: Throwable) {
                updateUiState {
                    it.copy(error = MeasurementEditError.DeleteFailed)
                }
            }
        }
    }

    fun onSaveClicked() {
        val current = _uiState.value as? MeasurementEditUiState.Loaded ?: return
        val date = current.dateEpochMillis ?: System.currentTimeMillis()

        viewModelScope.launch {
            try {
                when (
                    val result = saveMeasurementUseCase(
                        SaveMeasurementCommand(
                            measurementId = current.measurementId,
                            dateEpochMillis = date,
                            enabledMeasurements = current.enabledMeasurements,
                            metricValues = parseBodyMetricValues(current.bodyMetricInputs, current.unitSystem),
                            existingPhotoPath = current.persistedPhotoFilePath,
                            newPhotoPath = current.pendingPhotoAbsolutePath,
                            deleteExistingPhoto = current.isPhotoMarkedForDeletion,
                            note = current.note,
                        ),
                    )
                ) {
                    is SaveMeasurementResult.Success -> _events.emit(MeasurementEditEvent.Saved)
                    is SaveMeasurementResult.ValidationError -> {
                        _uiState.value = current.copy(
                            error = MeasurementEditError.Validation(result.error),
                        )
                    }
                }
            } catch (_: Throwable) {
                updateUiState {
                    it.copy(error = MeasurementEditError.SavePhotoFailed)
                }
            }
        }
    }

    private fun updateUiState(
        transform: (MeasurementEditUiState.Loaded) -> MeasurementEditUiState.Loaded,
    ) {
        val current = _uiState.value as? MeasurementEditUiState.Loaded ?: return
        val updated = transform(current)
        _uiState.value = updated.copy(
            hasUnsavedChanges = calculateHasUnsavedChanges(updated),
        )
    }

    private fun parseBodyMetricValues(
        bodyMetricInputs: Map<MeasuredBodyMetric, String>,
        unitSystem: UnitSystem,
    ): Map<MeasuredBodyMetric, Double?> {
        return bodyMetricInputs.mapValues { (metric, valueText) ->
            parseLocalizedDoubleOrNull(valueText)
                ?.userInputToStorageValue(metric.unit, unitSystem)
        }
    }

    private fun buildInitialLoadedState(
        sex: Sex,
        unitSystem: UnitSystem,
        enabledMeasurements: Set<MeasuredBodyMetric>,
        measurementId: Long?,
        measurement: BodyMeasurement?,
    ): MeasurementEditUiState.Loaded {
        val now = System.currentTimeMillis()
        return if (measurement == null) {
            MeasurementEditUiState.Loaded(
                measurementId = measurementId,
                sex = sex,
                unitSystem = unitSystem,
                enabledMeasurements = enabledMeasurements,
                dateEpochMillis = now,
                dateText = formatEpochMillisAsIsoDate(now),
                initialDateEpochMillis = now,
            )
        } else {
            val bodyMetricInputs = MeasurementMetricMapper.toBodyMetricInputMap(measurement, unitSystem)
            MeasurementEditUiState.Loaded(
                measurementId = measurement.id,
                sex = sex,
                unitSystem = unitSystem,
                enabledMeasurements = enabledMeasurements,
                dateEpochMillis = measurement.dateEpochMillis,
                dateText = formatEpochMillisAsIsoDate(measurement.dateEpochMillis),
                initialDateEpochMillis = measurement.dateEpochMillis,
                bodyMetricInputs = bodyMetricInputs,
                initialMetricInputs = bodyMetricInputs,
                note = measurement.note.orEmpty(),
                initialNote = measurement.note.orEmpty(),
                persistedPhotoFilePath = measurement.photoFilePath,
                initialPersistedPhotoFilePath = measurement.photoFilePath,
            )
        }
    }
}

private fun defaultBodyMetricInputs(): Map<MeasuredBodyMetric, String> {
    return MeasuredBodyMetric.entries.associateWith { "" }
}


private fun calculateHasUnsavedChanges(state: MeasurementEditUiState.Loaded): Boolean {
    if (state.measurementId == null) {
        return false
    }

    val hasDateChange = state.dateEpochMillis != state.initialDateEpochMillis
    val hasMetricInputChange = MeasuredBodyMetric.entries.any { metric ->
        val currentValue = parseLocalizedDoubleOrNull(state.bodyMetricInputs[metric].orEmpty())
        val initialValue = parseLocalizedDoubleOrNull(state.initialMetricInputs[metric].orEmpty())
        currentValue != initialValue
    }
    val hasPhotoChange =
        state.isPhotoMarkedForDeletion ||
            state.pendingPhotoAbsolutePath != null ||
            state.persistedPhotoFilePath != state.initialPersistedPhotoFilePath
    val hasNoteChange = state.note != state.initialNote

    return hasDateChange || hasMetricInputChange || hasPhotoChange || hasNoteChange
}
