package de.t_animal.opensourcebodytracker.domain.measurements

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.photos.PersistedPhotoPath
import de.t_animal.opensourcebodytracker.core.util.formatDecimalForInput

object MeasurementMetricMapper {
    fun toBodyMeasurement(
        id: Long,
        dateEpochMillis: Long,
        photoFilePath: PersistedPhotoPath?,
        values: Map<MeasuredBodyMetric, Double?>,
        enabledMeasurements: Set<MeasuredBodyMetric>,
    ): BodyMeasurement {
        fun getIfEnabledOrNull(metric: MeasuredBodyMetric): Double? {
            return if (metric in enabledMeasurements) values[metric] else null
        }

        return BodyMeasurement(
            id = id,
            dateEpochMillis = dateEpochMillis,
            photoFilePath = photoFilePath,
            weightKg = getIfEnabledOrNull(MeasuredBodyMetric.Weight),
            bodyFatPercent = getIfEnabledOrNull(MeasuredBodyMetric.BodyFat),
            neckCircumferenceCm = getIfEnabledOrNull(MeasuredBodyMetric.NeckCircumference),
            chestCircumferenceCm = getIfEnabledOrNull(MeasuredBodyMetric.ChestCircumference),
            waistCircumferenceCm = getIfEnabledOrNull(MeasuredBodyMetric.WaistCircumference),
            abdomenCircumferenceCm = getIfEnabledOrNull(MeasuredBodyMetric.AbdomenCircumference),
            hipCircumferenceCm = getIfEnabledOrNull(MeasuredBodyMetric.HipCircumference),
            chestSkinfoldMm = getIfEnabledOrNull(MeasuredBodyMetric.ChestSkinfold),
            abdomenSkinfoldMm = getIfEnabledOrNull(MeasuredBodyMetric.AbdomenSkinfold),
            thighSkinfoldMm = getIfEnabledOrNull(MeasuredBodyMetric.ThighSkinfold),
            tricepsSkinfoldMm = getIfEnabledOrNull(MeasuredBodyMetric.TricepsSkinfold),
            suprailiacSkinfoldMm = getIfEnabledOrNull(MeasuredBodyMetric.SuprailiacSkinfold),
        )
    }

    fun toMetricInputMap(measurement: BodyMeasurement): Map<MeasuredBodyMetric, String> {
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
}
