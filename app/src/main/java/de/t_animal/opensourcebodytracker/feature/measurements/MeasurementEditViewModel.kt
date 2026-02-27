package de.t_animal.opensourcebodytracker.feature.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.metrics.DerivedMetricsDependencyResolver
import de.t_animal.opensourcebodytracker.domain.metrics.enabledAnalysisMethods
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MeasurementEditUiState(
    val measurementId: Long? = null,
    val sex: Sex? = null,
    val enabledMeasurements: Set<MeasuredBodyMetric> = MeasuredBodyMetric.entries.toSet(),
    val dateEpochMillis: Long? = null,
    val dateText: String = "",
    val weightKgText: String = "",
    val neckCmText: String = "",
    val chestCmText: String = "",
    val waistCmText: String = "",
    val abdomenCmText: String = "",
    val hipCmText: String = "",
    val chestSkinfoldMmText: String = "",
    val abdomenSkinfoldMmText: String = "",
    val thighSkinfoldMmText: String = "",
    val tricepsSkinfoldMmText: String = "",
    val suprailiacSkinfoldMmText: String = "",
    val errorMessage: String? = null,
)

sealed interface MeasurementEditEvent {
    data object Saved : MeasurementEditEvent
}

class MeasurementEditViewModel(
    private val repository: MeasurementRepository,
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
                        dateEpochMillis = measurement.dateEpochMillis,
                        dateText = formatDate(measurement.dateEpochMillis),
                        weightKgText = measurement.weightKg?.let(::formatDecimalForInput).orEmpty(),
                        neckCmText = measurement.neckCircumferenceCm?.let(::formatDecimalForInput).orEmpty(),
                        chestCmText = measurement.chestCircumferenceCm?.let(::formatDecimalForInput).orEmpty(),
                        waistCmText = measurement.waistCircumferenceCm?.let(::formatDecimalForInput).orEmpty(),
                        abdomenCmText = measurement.abdomenCircumferenceCm?.let(::formatDecimalForInput).orEmpty(),
                        hipCmText = measurement.hipCircumferenceCm?.let(::formatDecimalForInput).orEmpty(),
                        chestSkinfoldMmText = measurement.chestSkinfoldMm?.let(::formatDecimalForInput).orEmpty(),
                        abdomenSkinfoldMmText = measurement.abdomenSkinfoldMm?.let(::formatDecimalForInput).orEmpty(),
                        thighSkinfoldMmText = measurement.thighSkinfoldMm?.let(::formatDecimalForInput).orEmpty(),
                        tricepsSkinfoldMmText = measurement.tricepsSkinfoldMm?.let(::formatDecimalForInput).orEmpty(),
                        suprailiacSkinfoldMmText = measurement.suprailiacSkinfoldMm?.let(::formatDecimalForInput).orEmpty(),
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

    fun onChestSkinfoldChanged(text: String) = update {
        it.copy(chestSkinfoldMmText = text, errorMessage = null)
    }

    fun onAbdomenSkinfoldChanged(text: String) = update {
        it.copy(abdomenSkinfoldMmText = text, errorMessage = null)
    }

    fun onThighSkinfoldChanged(text: String) = update {
        it.copy(thighSkinfoldMmText = text, errorMessage = null)
    }

    fun onTricepsSkinfoldChanged(text: String) = update {
        it.copy(tricepsSkinfoldMmText = text, errorMessage = null)
    }

    fun onSuprailiacSkinfoldChanged(text: String) = update {
        it.copy(suprailiacSkinfoldMmText = text, errorMessage = null)
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

    fun onSaveClicked() {
        val current = _uiState.value
        val enabledMeasurements = current.enabledMeasurements
        val date = current.dateEpochMillis ?: System.currentTimeMillis()

        val weight = parseDoubleOrNull(current.weightKgText)
        val neck = parseDoubleOrNull(current.neckCmText)
        val chest = parseDoubleOrNull(current.chestCmText)
        val waist = parseDoubleOrNull(current.waistCmText)
        val abdomen = parseDoubleOrNull(current.abdomenCmText)
        val hip = parseDoubleOrNull(current.hipCmText)
        val chestSkinfold = parseDoubleOrNull(current.chestSkinfoldMmText)
        val abdomenSkinfold = parseDoubleOrNull(current.abdomenSkinfoldMmText)
        val thighSkinfold = parseDoubleOrNull(current.thighSkinfoldMmText)
        val tricepsSkinfold = parseDoubleOrNull(current.tricepsSkinfoldMmText)
        val suprailiacSkinfold = parseDoubleOrNull(current.suprailiacSkinfoldMmText)

        val invalidSkinfoldInput = listOf(
            chestSkinfold,
            abdomenSkinfold,
            thighSkinfold,
            tricepsSkinfold,
            suprailiacSkinfold,
        ).any { it != null && it <= 0.0 }
        if (invalidSkinfoldInput) {
            _uiState.value = current.copy(errorMessage = "Skinfold values must be greater than 0")
            return
        }

        val hasAnyValue = listOf(
            weight,
            neck,
            chest,
            waist,
            abdomen,
            hip,
            chestSkinfold,
            abdomenSkinfold,
            thighSkinfold,
            tricepsSkinfold,
            suprailiacSkinfold,
        ).any { it != null }
        if (!hasAnyValue) {
            _uiState.value = current.copy(errorMessage = "Enter at least one value")
            return
        }

        viewModelScope.launch {
            fun ifEnabled(metric: MeasuredBodyMetric, value: Double?): Double? {
                return if (metric in enabledMeasurements) value else null
            }

            if (measurementId == null) {
                repository.insert(
                    BodyMeasurement(
                        id = 0,
                        dateEpochMillis = date,
                        weightKg = ifEnabled(MeasuredBodyMetric.Weight, weight),
                        neckCircumferenceCm = ifEnabled(MeasuredBodyMetric.NeckCircumference, neck),
                        chestCircumferenceCm = ifEnabled(MeasuredBodyMetric.ChestCircumference, chest),
                        waistCircumferenceCm = ifEnabled(MeasuredBodyMetric.WaistCircumference, waist),
                        abdomenCircumferenceCm = ifEnabled(MeasuredBodyMetric.AbdomenCircumference, abdomen),
                        hipCircumferenceCm = ifEnabled(MeasuredBodyMetric.HipCircumference, hip),
                        chestSkinfoldMm = ifEnabled(MeasuredBodyMetric.ChestSkinfold, chestSkinfold),
                        abdomenSkinfoldMm = ifEnabled(MeasuredBodyMetric.AbdomenSkinfold, abdomenSkinfold),
                        thighSkinfoldMm = ifEnabled(MeasuredBodyMetric.ThighSkinfold, thighSkinfold),
                        tricepsSkinfoldMm = ifEnabled(MeasuredBodyMetric.TricepsSkinfold, tricepsSkinfold),
                        suprailiacSkinfoldMm = ifEnabled(MeasuredBodyMetric.SuprailiacSkinfold, suprailiacSkinfold),
                    ),
                )
            } else {
                repository.update(
                    BodyMeasurement(
                        id = measurementId,
                        dateEpochMillis = date,
                        weightKg = ifEnabled(MeasuredBodyMetric.Weight, weight),
                        neckCircumferenceCm = ifEnabled(MeasuredBodyMetric.NeckCircumference, neck),
                        chestCircumferenceCm = ifEnabled(MeasuredBodyMetric.ChestCircumference, chest),
                        waistCircumferenceCm = ifEnabled(MeasuredBodyMetric.WaistCircumference, waist),
                        abdomenCircumferenceCm = ifEnabled(MeasuredBodyMetric.AbdomenCircumference, abdomen),
                        hipCircumferenceCm = ifEnabled(MeasuredBodyMetric.HipCircumference, hip),
                        chestSkinfoldMm = ifEnabled(MeasuredBodyMetric.ChestSkinfold, chestSkinfold),
                        abdomenSkinfoldMm = ifEnabled(MeasuredBodyMetric.AbdomenSkinfold, abdomenSkinfold),
                        thighSkinfoldMm = ifEnabled(MeasuredBodyMetric.ThighSkinfold, thighSkinfold),
                        tricepsSkinfoldMm = ifEnabled(MeasuredBodyMetric.TricepsSkinfold, tricepsSkinfold),
                        suprailiacSkinfoldMm = ifEnabled(MeasuredBodyMetric.SuprailiacSkinfold, suprailiacSkinfold),
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
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val dependencyResolver: DerivedMetricsDependencyResolver,
    private val measurementId: Long?,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MeasurementEditViewModel(
            repository = repository,
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
