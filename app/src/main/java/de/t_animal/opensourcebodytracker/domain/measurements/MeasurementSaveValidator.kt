package de.t_animal.opensourcebodytracker.domain.measurements

import de.t_animal.opensourcebodytracker.core.model.BodyMetricType
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.photos.PersistedPhotoPath
import de.t_animal.opensourcebodytracker.core.photos.TemporaryCapturePhotoPath

class MeasurementSaveValidator {
    private val invalidSkinfoldMessage = "Skinfold values must be greater than 0"
    private val invalidBodyFatMessage = "Body fat must be between 0 and 100"
    private val missingInputOrNoPhotoMessage = "Enter at least one value or add a photo"

    fun validate(
        metricValues: Map<MeasuredBodyMetric, Double?>,
        newPhotoPath: TemporaryCapturePhotoPath?,
        oldPhotoPath: PersistedPhotoPath?,
        deleteOldPhoto: Boolean,
    ): String? {
        val invalidSkinfoldInput = MeasuredBodyMetric.entries
            .filter { it.metricType == BodyMetricType.SkinfoldThickness }
            .any { metric ->
                val value = metricValues[metric]
                value != null && value <= 0.0
            }
        if (invalidSkinfoldInput) {
            return invalidSkinfoldMessage
        }

        val bodyFat = metricValues[MeasuredBodyMetric.BodyFat]
        val invalidBodyFatInput = bodyFat != null && (bodyFat < 0.0 || bodyFat > 100.0)
        if (invalidBodyFatInput) {
            return invalidBodyFatMessage
        }

        val hasAnyValue = metricValues.values.any { it != null }
        val hasPhoto = newPhotoPath != null || (oldPhotoPath != null && !deleteOldPhoto)

        return if (!hasAnyValue && !hasPhoto) missingInputOrNoPhotoMessage else null
    }
}
