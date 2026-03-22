package de.t_animal.opensourcebodytracker.domain.measurements

import de.t_animal.opensourcebodytracker.core.model.BodyMetricType
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.photos.PersistedPhotoPath
import de.t_animal.opensourcebodytracker.core.photos.TemporaryCapturePhotoPath
import javax.inject.Inject

sealed interface MeasurementValidationError {
    data object InvalidSkinfoldValues : MeasurementValidationError
    data object InvalidBodyFat : MeasurementValidationError
    data object MissingInput : MeasurementValidationError
}

class MeasurementSaveValidator @Inject constructor() {
    fun validate(
        metricValues: Map<MeasuredBodyMetric, Double?>,
        newPhotoPath: TemporaryCapturePhotoPath?,
        oldPhotoPath: PersistedPhotoPath?,
        deleteOldPhoto: Boolean,
        note: String,
    ): MeasurementValidationError? {
        val invalidSkinfoldInput = MeasuredBodyMetric.entries
            .filter { it.metricType == BodyMetricType.SkinfoldThickness }
            .any { metric ->
                val value = metricValues[metric]
                value != null && value <= 0.0
            }
        if (invalidSkinfoldInput) {
            return MeasurementValidationError.InvalidSkinfoldValues
        }

        val bodyFat = metricValues[MeasuredBodyMetric.BodyFat]
        val invalidBodyFatInput = bodyFat != null && (bodyFat < 0.0 || bodyFat > 100.0)
        if (invalidBodyFatInput) {
            return MeasurementValidationError.InvalidBodyFat
        }

        val hasAnyValue = metricValues.values.any { it != null }
        val hasPhoto = newPhotoPath != null || (oldPhotoPath != null && !deleteOldPhoto)
        val hasNote = note.isNotBlank()

        return if (!hasAnyValue && !hasPhoto && !hasNote) MeasurementValidationError.MissingInput else null
    }
}
