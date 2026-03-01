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
import kotlinx.coroutines.launch

data class MeasurementEditUiState(
    val measurementId: Long? = null,
    val sex: Sex? = null,
    val enabledMeasurements: Set<MeasuredBodyMetric> = MeasuredBodyMetric.entries.toSet(),
    val dateEpochMillis: Long? = null,
    val dateText: String = "",
    val metricInputs: Map<MeasuredBodyMetric, String> = defaultMetricInputs(),
    val persistedPhotoFilePath: String? = null,
    val pendingPhotoAbsolutePath: String? = null,
    val isPhotoMarkedForDeletion: Boolean = false,
    val isPhotoPreviewDialogVisible: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface MeasurementEditEvent {
    data object Saved : MeasurementEditEvent
}

class MeasurementEditViewModel(
    private val repository: MeasurementRepository,
    private val photoStorage: InternalPhotoStorage,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val dependencyResolver: DerivedMetricsDependencyResolver,
    private val measurementId: Long?,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MeasurementEditUiState(measurementId = measurementId))
    val uiState: StateFlow<MeasurementEditUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MeasurementEditEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(profileRepository.profileFlow, settingsRepository.settingsFlow) { profile, settings ->
                val requiredMeasurements = profile
                    ?.let { dependencyResolver.resolve(settings.enabledAnalysisMethods(), it).requiredMeasurements }
                    .orEmpty()
                val effectiveEnabledMeasurements = settings.enabledMeasurements + requiredMeasurements

                profile?.sex to effectiveEnabledMeasurements
            }.collect { (sex, enabledMeasurements) ->
                update {
                    it.copy(
                        sex = sex,
                        enabledMeasurements = enabledMeasurements,
                    )
                }
            }
        }

        if (measurementId != null) {
            viewModelScope.launch {
                val measurement = repository.getById(measurementId)
                if (measurement != null) {
                    _uiState.value = MeasurementEditUiState(
                        measurementId = measurement.id,
                        sex = _uiState.value.sex,
                        enabledMeasurements = _uiState.value.enabledMeasurements,
                        dateEpochMillis = measurement.dateEpochMillis,
                        dateText = formatDate(measurement.dateEpochMillis),
                        metricInputs = toMetricInputMap(measurement),
                        persistedPhotoFilePath = measurement.photoFilePath,
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
        val previousPhotoPath = _uiState.value.pendingPhotoAbsolutePath
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

    fun onSaveClicked() {
        val current = _uiState.value
        val enabledMeasurements = current.enabledMeasurements
        val date = current.dateEpochMillis ?: System.currentTimeMillis()

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
                if (measurementId == null) {
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
                            measurementId = measurementId,
                            measurementDateEpochMillis = date,
                            sourceAbsolutePath = pendingPhotoAbsolutePath,
                        )
                        shouldRemovePersistedPhoto -> null
                        else -> current.persistedPhotoFilePath
                    }

                    repository.update(
                        buildBodyMeasurement(
                            id = measurementId,
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
                _uiState.value = _uiState.value.copy(errorMessage = "Unable to save measurement photo")
            }
        }
    }

    private fun update(transform: (MeasurementEditUiState) -> MeasurementEditUiState) {
        _uiState.value = transform(_uiState.value)
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
