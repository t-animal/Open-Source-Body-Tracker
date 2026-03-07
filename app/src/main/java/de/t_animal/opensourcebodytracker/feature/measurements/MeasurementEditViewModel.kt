package de.t_animal.opensourcebodytracker.feature.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.core.model.BodyMetricType
import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.metrics.DerivedMetricsDependencyResolver
import de.t_animal.opensourcebodytracker.domain.metrics.enabledAnalysisMethods
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.ZoneId
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
        val persistedPhotoFilePath: String? = null,
        val initialPersistedPhotoFilePath: String? = null,
        val pendingPhotoAbsolutePath: String? = null,
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

class MeasurementEditViewModel(
    private val repository: MeasurementRepository,
    private val photoStorage: InternalPhotoStorage,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val dependencyResolver: DerivedMetricsDependencyResolver,
    private val measurementId: Long?,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MeasurementEditUiState>(MeasurementEditUiState.Loading)
    val uiState: StateFlow<MeasurementEditUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MeasurementEditEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                profileRepository.requiredProfileFlow,
                settingsRepository.settingsFlow,
                observeExistingMeasurement(),
            ) { profile, settings, measurement ->
                val requiredMeasurements = dependencyResolver
                    .resolve(settings.enabledAnalysisMethods(), profile)
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
        update {
            it.copy(
                metricInputs = it.metricInputs + (metric to text),
                errorMessage = null,
            )
        }
    }

    fun onDateChanged(epochMillis: Long) {
        update {
            it.copy(
                dateEpochMillis = epochMillis,
                dateText = formatDate(epochMillis),
                errorMessage = null,
            )
        }
    }

    fun onPhotoCaptured(pendingPhotoAbsolutePath: String?) {
        val current = _uiState.value as? MeasurementEditUiState.Loaded ?: return
        val previousPhotoPath = current.pendingPhotoAbsolutePath
        if (!previousPhotoPath.isNullOrBlank() && previousPhotoPath != pendingPhotoAbsolutePath) {
            viewModelScope.launch {
                photoStorage.deletePhotoAtAbsolutePath(previousPhotoPath)
            }
        }

        update {
            it.copy(
                pendingPhotoAbsolutePath = pendingPhotoAbsolutePath,
                isPhotoMarkedForDeletion = false,
                isPhotoPreviewDialogVisible = false,
            )
        }
    }

    fun onDeletePhotoClicked() {
        update {
            it.copy(
                pendingPhotoAbsolutePath = null,
                isPhotoMarkedForDeletion = true,
                isPhotoPreviewDialogVisible = false,
            )
        }
    }

    fun onPhotoPreviewDialogVisibilityChanged(isVisible: Boolean) {
        update {
            it.copy(isPhotoPreviewDialogVisible = isVisible)
        }
    }

    fun onDeleteMeasurementClicked() {
        val current = _uiState.value as? MeasurementEditUiState.Loaded ?: return
        val currentMeasurementId = current.measurementId ?: return

        viewModelScope.launch {
            try {
                repository.deleteById(currentMeasurementId)
                current.pendingPhotoAbsolutePath?.let { photoStorage.deletePhotoAtAbsolutePath(it) }
                current.persistedPhotoFilePath?.let { photoStorage.deletePhoto(it) }
                _events.emit(MeasurementEditEvent.Deleted)
            } catch (_: Throwable) {
                update {
                    it.copy(errorMessage = "Unable to delete measurement")
                }
            }
        }
    }

    fun onSaveClicked() {
        val current = _uiState.value as? MeasurementEditUiState.Loaded ?: return
        val enabledMeasurements = current.enabledMeasurements
        val date = current.dateEpochMillis ?: System.currentTimeMillis()
        val currentMeasurementId = current.measurementId

        val metricValues = current.metricInputs
            .mapValues { (_, valueText) -> parseDoubleOrNull(valueText) }

        val invalidSkinfoldInput = MeasuredBodyMetric.entries
            .filter { it.metricType == BodyMetricType.SkinfoldThickness }
            .any { metric ->
                val value = metricValues[metric]
                value != null && value <= 0.0
            }
        if (invalidSkinfoldInput) {
            _uiState.value = current.copy(errorMessage = "Skinfold values must be greater than 0")
            return
        }

        val bodyFat = metricValues[MeasuredBodyMetric.BodyFat]
        val invalidBodyFatInput = bodyFat != null && (bodyFat < 0.0 || bodyFat > 100.0)
        if (invalidBodyFatInput) {
            _uiState.value = current.copy(errorMessage = "Body fat must be between 0 and 100")
            return
        }

        val hasAnyValue = metricValues.values.any { it != null }

        val hasPhoto = current.pendingPhotoAbsolutePath != null ||
            (current.persistedPhotoFilePath != null && !current.isPhotoMarkedForDeletion)

        if (!hasAnyValue && !hasPhoto) {
            _uiState.value = current.copy(errorMessage = "Enter at least one value or add a photo")
            return
        }

        viewModelScope.launch {
            val pendingPhotoAbsolutePath = current.pendingPhotoAbsolutePath

            try {
                if (currentMeasurementId == null) {
                    val insertedId = repository.insert(
                        buildBodyMeasurement(
                            id = 0,
                            dateEpochMillis = date,
                            photoFilePath = null,
                            values = metricValues,
                            enabledMeasurements = enabledMeasurements,
                        ),
                    )

                    if (pendingPhotoAbsolutePath != null) {
                        val savedPhotoPath = photoStorage.movePhotoForMeasurement(
                            measurementId = insertedId,
                            measurementDateEpochMillis = date,
                            sourceAbsolutePath = pendingPhotoAbsolutePath,
                        )
                        repository.update(
                            buildBodyMeasurement(
                                id = insertedId,
                                dateEpochMillis = date,
                                photoFilePath = savedPhotoPath,
                                values = metricValues,
                                enabledMeasurements = enabledMeasurements,
                            ),
                        )
                    }
                } else {
                    val shouldRemovePersistedPhoto =
                        current.isPhotoMarkedForDeletion && pendingPhotoAbsolutePath == null

                    val updatedPhotoPath = when {
                        pendingPhotoAbsolutePath != null -> photoStorage.movePhotoForMeasurement(
                            measurementId = currentMeasurementId,
                            measurementDateEpochMillis = date,
                            sourceAbsolutePath = pendingPhotoAbsolutePath,
                        )
                        shouldRemovePersistedPhoto -> null
                        else -> current.persistedPhotoFilePath
                    }

                    repository.update(
                        buildBodyMeasurement(
                            id = currentMeasurementId,
                            dateEpochMillis = date,
                            photoFilePath = updatedPhotoPath,
                            values = metricValues,
                            enabledMeasurements = enabledMeasurements,
                        ),
                    )

                    if (shouldRemovePersistedPhoto) {
                        current.persistedPhotoFilePath?.let { photoStorage.deletePhoto(it) }
                    } else if (pendingPhotoAbsolutePath != null) {
                        val previousPhotoPath = current.persistedPhotoFilePath
                        if (!previousPhotoPath.isNullOrBlank() && previousPhotoPath != updatedPhotoPath) {
                            photoStorage.deletePhoto(previousPhotoPath)
                        }
                    }
                }

                _events.emit(MeasurementEditEvent.Saved)
            } catch (_: Throwable) {
                update {
                    it.copy(errorMessage = "Unable to save measurement photo")
                }
            }
        }
    }

    private fun update(transform: (MeasurementEditUiState.Loaded) -> MeasurementEditUiState.Loaded) {
        val current = _uiState.value as? MeasurementEditUiState.Loaded ?: return
        val updated = transform(current)
        _uiState.value = updated.copy(
            hasUnsavedChanges = calculateHasUnsavedChanges(updated),
        )
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
                dateText = formatDate(now),
                initialDateEpochMillis = now,
            )
        } else {
            val metricInputs = toMetricInputMap(measurement)
            MeasurementEditUiState.Loaded(
                measurementId = measurement.id,
                sex = sex,
                enabledMeasurements = enabledMeasurements,
                dateEpochMillis = measurement.dateEpochMillis,
                dateText = formatDate(measurement.dateEpochMillis),
                initialDateEpochMillis = measurement.dateEpochMillis,
                metricInputs = metricInputs,
                initialMetricInputs = metricInputs,
                persistedPhotoFilePath = measurement.photoFilePath,
                initialPersistedPhotoFilePath = measurement.photoFilePath,
            )
        }
    }

    private fun buildBodyMeasurement(
        id: Long,
        dateEpochMillis: Long,
        photoFilePath: String?,
        values: Map<MeasuredBodyMetric, Double?>,
        enabledMeasurements: Set<MeasuredBodyMetric>,
    ): BodyMeasurement {
        fun ifEnabled(metric: MeasuredBodyMetric): Double? {
            return if (metric in enabledMeasurements) values[metric] else null
        }

        return BodyMeasurement(
            id = id,
            dateEpochMillis = dateEpochMillis,
            photoFilePath = photoFilePath,
            weightKg = ifEnabled(MeasuredBodyMetric.Weight),
            bodyFatPercent = ifEnabled(MeasuredBodyMetric.BodyFat),
            neckCircumferenceCm = ifEnabled(MeasuredBodyMetric.NeckCircumference),
            chestCircumferenceCm = ifEnabled(MeasuredBodyMetric.ChestCircumference),
            waistCircumferenceCm = ifEnabled(MeasuredBodyMetric.WaistCircumference),
            abdomenCircumferenceCm = ifEnabled(MeasuredBodyMetric.AbdomenCircumference),
            hipCircumferenceCm = ifEnabled(MeasuredBodyMetric.HipCircumference),
            chestSkinfoldMm = ifEnabled(MeasuredBodyMetric.ChestSkinfold),
            abdomenSkinfoldMm = ifEnabled(MeasuredBodyMetric.AbdomenSkinfold),
            thighSkinfoldMm = ifEnabled(MeasuredBodyMetric.ThighSkinfold),
            tricepsSkinfoldMm = ifEnabled(MeasuredBodyMetric.TricepsSkinfold),
            suprailiacSkinfoldMm = ifEnabled(MeasuredBodyMetric.SuprailiacSkinfold),
        )
    }
}

class MeasurementEditViewModelFactory(
    private val repository: MeasurementRepository,
    private val photoStorage: InternalPhotoStorage,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val dependencyResolver: DerivedMetricsDependencyResolver,
    private val measurementId: Long?,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MeasurementEditViewModel(
            repository = repository,
            photoStorage = photoStorage,
            profileRepository = profileRepository,
            settingsRepository = settingsRepository,
            dependencyResolver = dependencyResolver,
            measurementId = measurementId,
        ) as T
    }
}

private fun parseDoubleOrNull(text: String): Double? {
    val trimmed = text.trim()
    if (trimmed.isBlank()) return null
    val decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator
    return trimmed
        .replace(decimalSeparator, '.')
        .replace(',', '.')
        .toDoubleOrNull()
}

private fun formatDecimalForInput(value: Double): String {
    val decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator
    val text = value.toString()
    return if (decimalSeparator == '.') text else text.replace('.', decimalSeparator)
}

private fun formatDate(epochMillis: Long): String {
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .toString()
}

private fun defaultMetricInputs(): Map<MeasuredBodyMetric, String> {
    return MeasuredBodyMetric.entries.associateWith { "" }
}

private fun toMetricInputMap(measurement: BodyMeasurement): Map<MeasuredBodyMetric, String> {
    return mapOf(
        MeasuredBodyMetric.Weight to measurement.weightKg,
        MeasuredBodyMetric.BodyFat to measurement.bodyFatPercent,
        MeasuredBodyMetric.NeckCircumference to measurement.neckCircumferenceCm,
        MeasuredBodyMetric.ChestCircumference to measurement.chestCircumferenceCm,
        MeasuredBodyMetric.WaistCircumference to measurement.waistCircumferenceCm,
        MeasuredBodyMetric.AbdomenCircumference to measurement.abdomenCircumferenceCm,
        MeasuredBodyMetric.HipCircumference to measurement.hipCircumferenceCm,
        MeasuredBodyMetric.ChestSkinfold to measurement.chestSkinfoldMm,
        MeasuredBodyMetric.AbdomenSkinfold to measurement.abdomenSkinfoldMm,
        MeasuredBodyMetric.ThighSkinfold to measurement.thighSkinfoldMm,
        MeasuredBodyMetric.TricepsSkinfold to measurement.tricepsSkinfoldMm,
        MeasuredBodyMetric.SuprailiacSkinfold to measurement.suprailiacSkinfoldMm,
    ).mapValues { (_, value) ->
        value?.let(::formatDecimalForInput).orEmpty()
    }
}

private fun calculateHasUnsavedChanges(state: MeasurementEditUiState.Loaded): Boolean {
    if (state.measurementId == null) {
        return false
    }

    val hasDateChange = state.dateEpochMillis != state.initialDateEpochMillis
    val hasMetricInputChange = MeasuredBodyMetric.entries.any { metric ->
        val currentValue = parseDoubleOrNull(state.metricInputs[metric].orEmpty())
        val initialValue = parseDoubleOrNull(state.initialMetricInputs[metric].orEmpty())
        currentValue != initialValue
    }
    val hasPhotoChange =
        state.isPhotoMarkedForDeletion ||
            !state.pendingPhotoAbsolutePath.isNullOrBlank() ||
            state.persistedPhotoFilePath != state.initialPersistedPhotoFilePath

    return hasDateChange || hasMetricInputChange || hasPhotoChange
}
