package de.t_animal.opensourcebodytracker.feature.measurements

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.photos.PersistedPhotoPath
import de.t_animal.opensourcebodytracker.core.photos.TemporaryCapturePhotoPath
import de.t_animal.opensourcebodytracker.data.photos.NewPhotoCaptureTarget
import java.io.File
import de.t_animal.opensourcebodytracker.core.util.formatEpochMillisAsIsoDate
import de.t_animal.opensourcebodytracker.core.util.parseLocalizedDoubleOrNull
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.MeasurementSettingsRepository
import de.t_animal.opensourcebodytracker.domain.measurements.DeleteMeasurementCommand
import de.t_animal.opensourcebodytracker.domain.measurements.DeleteMeasurementResult
import de.t_animal.opensourcebodytracker.domain.measurements.DeleteMeasurementUseCase
import de.t_animal.opensourcebodytracker.domain.measurements.MeasurementMetricMapper
import de.t_animal.opensourcebodytracker.domain.measurements.SaveMeasurementCommand
import de.t_animal.opensourcebodytracker.domain.measurements.SaveMeasurementResult
import de.t_animal.opensourcebodytracker.domain.measurements.SaveMeasurementUseCase
import de.t_animal.opensourcebodytracker.domain.metrics.DerivedMetricsDependencyResolver
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

sealed interface MeasurementEditUiState {
    data object Loading : MeasurementEditUiState

    data class Loaded(
        val measurementId: Long? = null,
        val sex: Sex,
        val enabledMeasurements: Set<MeasuredBodyMetric>,
        val dateEpochMillis: Long? = null,
        val dateText: String = "",
        val initialDateEpochMillis: Long? = null,
        val metricInputs: Map<MeasuredBodyMetric, String> = defaultMetricInputs(),
        val initialMetricInputs: Map<MeasuredBodyMetric, String> = defaultMetricInputs(),
        val note: String = "",
        val initialNote: String = "",
        val persistedPhotoFilePath: PersistedPhotoPath? = null,
        val initialPersistedPhotoFilePath: PersistedPhotoPath? = null,
        val pendingPhotoAbsolutePath: TemporaryCapturePhotoPath? = null,
        val isPhotoMarkedForDeletion: Boolean = false,
        val hasUnsavedChanges: Boolean = false,
        val isPhotoPreviewDialogVisible: Boolean = false,
        val errorMessage: String? = null,
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
    private val measurementSettingsRepository: MeasurementSettingsRepository,
    private val deleteMeasurementUseCase: DeleteMeasurementUseCase,
    private val saveMeasurementUseCase: SaveMeasurementUseCase,
    private val dependencyResolver: DerivedMetricsDependencyResolver,
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
                measurementSettingsRepository.settingsFlow,
                observeExistingMeasurement(),
            ) { profile, settings, measurement ->
                val requiredMeasurements = dependencyResolver
                    .resolve(settings.enabledAnalysisMethods, profile)
                    .requiredMeasurements
                val effectiveEnabledMeasurements = settings.enabledMeasurements + requiredMeasurements

                Triple(profile.sex, effectiveEnabledMeasurements, measurement)
            }.collect { (sex, enabledMeasurements, measurement) ->

                val baseMeasurementId = measurementId
                if (baseMeasurementId == null && measurement != null) return@collect

                val currentLoaded = _uiState.value as? MeasurementEditUiState.Loaded
                _uiState.value = if (currentLoaded == null) {
                    buildInitialLoadedState(
                        sex = sex,
                        enabledMeasurements = enabledMeasurements,
                        measurementId = baseMeasurementId,
                        measurement = measurement,
                    )
                } else {
                    currentLoaded.copy(
                        sex = sex,
                        enabledMeasurements = enabledMeasurements,
                    )
                }
            }
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
                metricInputs = it.metricInputs + (metric to text),
                errorMessage = null,
            )
        }
    }

    fun onDateChanged(epochMillis: Long) {
        updateUiState {
            it.copy(
                dateEpochMillis = epochMillis,
                dateText = formatEpochMillisAsIsoDate(epochMillis),
                errorMessage = null,
            )
        }
    }

    fun onNoteChanged(text: String) {
        updateUiState {
            it.copy(
                note = text,
                errorMessage = null,
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
                    it.copy(errorMessage = "Unable to delete measurement")
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
                            metricValues = parseMetricValues(current.metricInputs),
                            existingPhotoPath = current.persistedPhotoFilePath,
                            newPhotoPath = current.pendingPhotoAbsolutePath,
                            deleteExistingPhoto = current.isPhotoMarkedForDeletion,
                            note = current.note,
                        ),
                    )
                ) {
                    is SaveMeasurementResult.Success -> _events.emit(MeasurementEditEvent.Saved)
                    is SaveMeasurementResult.ValidationError -> {
                        _uiState.value = current.copy(errorMessage = result.message)
                    }
                }
            } catch (_: Throwable) {
                updateUiState {
                    it.copy(errorMessage = "Unable to save measurement photo")
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

    private fun parseMetricValues(metricInputs: Map<MeasuredBodyMetric, String>): Map<MeasuredBodyMetric, Double?> {
        return metricInputs.mapValues { (_, valueText) -> parseLocalizedDoubleOrNull(valueText) }
    }

    private fun buildInitialLoadedState(
        sex: Sex,
        enabledMeasurements: Set<MeasuredBodyMetric>,
        measurementId: Long?,
        measurement: BodyMeasurement?,
    ): MeasurementEditUiState.Loaded {
        val now = System.currentTimeMillis()
        return if (measurement == null) {
            MeasurementEditUiState.Loaded(
                measurementId = measurementId,
                sex = sex,
                enabledMeasurements = enabledMeasurements,
                dateEpochMillis = now,
                dateText = formatEpochMillisAsIsoDate(now),
                initialDateEpochMillis = now,
            )
        } else {
            val metricInputs = MeasurementMetricMapper.toMetricInputMap(measurement)
            MeasurementEditUiState.Loaded(
                measurementId = measurement.id,
                sex = sex,
                enabledMeasurements = enabledMeasurements,
                dateEpochMillis = measurement.dateEpochMillis,
                dateText = formatEpochMillisAsIsoDate(measurement.dateEpochMillis),
                initialDateEpochMillis = measurement.dateEpochMillis,
                metricInputs = metricInputs,
                initialMetricInputs = metricInputs,
                note = measurement.note.orEmpty(),
                initialNote = measurement.note.orEmpty(),
                persistedPhotoFilePath = measurement.photoFilePath,
                initialPersistedPhotoFilePath = measurement.photoFilePath,
            )
        }
    }
}

private fun defaultMetricInputs(): Map<MeasuredBodyMetric, String> {
    return MeasuredBodyMetric.entries.associateWith { "" }
}


private fun calculateHasUnsavedChanges(state: MeasurementEditUiState.Loaded): Boolean {
    if (state.measurementId == null) {
        return false
    }

    val hasDateChange = state.dateEpochMillis != state.initialDateEpochMillis
    val hasMetricInputChange = MeasuredBodyMetric.entries.any { metric ->
        val currentValue = parseLocalizedDoubleOrNull(state.metricInputs[metric].orEmpty())
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
