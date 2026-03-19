package de.t_animal.opensourcebodytracker.feature.measurements.helpers

import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.BodyMetricUnit
import de.t_animal.opensourcebodytracker.core.model.DerivedBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MetricRating
import de.t_animal.opensourcebodytracker.core.model.forMetric
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementListItemUiModel
import java.text.NumberFormat

internal data class MetricDisplayItem(
    val label: String,
    val fullName: String,
    val value: String,
    val rating: MetricRating? = null,
)

internal fun buildLatestMeasurementMetrics(
    item: MeasurementListItemUiModel,
    visibleMetrics: List<BodyMetric>,
): List<MetricDisplayItem> {
    return visibleMetrics.map { metric ->
        MetricDisplayItem(
            label = metric.label(),
            fullName = metric.fullName(),
            value = metric.formattedValue(item),
            rating = (metric as? DerivedBodyMetric)?.let { item.derivedMetricRatings.forMetric(it) },
        )
    }
}

internal fun BodyMetric.formattedValue(item: MeasurementListItemUiModel): String {
    val value = valueSelector(item.measurement, item.derivedMetrics)
    return valueWithUnit(value, unit)
}

internal fun BodyMetric.label(): String = when (this) {
    is MeasuredBodyMetric -> when (this) {
        MeasuredBodyMetric.Weight -> "Weight"
        MeasuredBodyMetric.BodyFat -> "Body Fat"
        MeasuredBodyMetric.NeckCircumference -> "Neck"
        MeasuredBodyMetric.WaistCircumference -> "Waist"
        MeasuredBodyMetric.HipCircumference -> "Hip"
        MeasuredBodyMetric.ChestCircumference -> "Chest"
        MeasuredBodyMetric.AbdomenCircumference -> "Abdomen"
        MeasuredBodyMetric.ChestSkinfold -> "Chest Skinfold"
        MeasuredBodyMetric.AbdomenSkinfold -> "Abdomen Skinfold"
        MeasuredBodyMetric.ThighSkinfold -> "Thigh Skinfold"
        MeasuredBodyMetric.TricepsSkinfold -> "Triceps Skinfold"
        MeasuredBodyMetric.SuprailiacSkinfold -> "Suprailiac Skinfold"
    }

    is DerivedBodyMetric -> when (this) {
        DerivedBodyMetric.Bmi -> "BMI"
        DerivedBodyMetric.NavyBodyFatPercent -> "Body Fat Navy"
        DerivedBodyMetric.SkinfoldBodyFatPercent -> "Body Fat Skinfold"
        DerivedBodyMetric.WaistHipRatio -> "WHR"
        DerivedBodyMetric.WaistHeightRatio -> "WHtR"
    }

    else -> id
}

internal fun BodyMetric.fullName(): String = when (this) {
    is MeasuredBodyMetric -> when (this) {
        MeasuredBodyMetric.Weight -> "Weight"
        MeasuredBodyMetric.BodyFat -> "Body Fat"
        MeasuredBodyMetric.NeckCircumference -> "Neck Circumference"
        MeasuredBodyMetric.WaistCircumference -> "Waist Circumference"
        MeasuredBodyMetric.HipCircumference -> "Hip Circumference"
        MeasuredBodyMetric.ChestCircumference -> "Chest Circumference"
        MeasuredBodyMetric.AbdomenCircumference -> "Abdomen Circumference"
        MeasuredBodyMetric.ChestSkinfold -> "Chest Skinfold"
        MeasuredBodyMetric.AbdomenSkinfold -> "Abdomen Skinfold"
        MeasuredBodyMetric.ThighSkinfold -> "Thigh Skinfold"
        MeasuredBodyMetric.TricepsSkinfold -> "Triceps Skinfold"
        MeasuredBodyMetric.SuprailiacSkinfold -> "Suprailiac Skinfold"
    }

    is DerivedBodyMetric -> when (this) {
        DerivedBodyMetric.Bmi -> "Body Mass Index"
        DerivedBodyMetric.NavyBodyFatPercent -> "Navy Method Body Fat Percentage"
        DerivedBodyMetric.SkinfoldBodyFatPercent -> "3-Site Skinfold Body Fat Percentage"
        DerivedBodyMetric.WaistHipRatio -> "Waist-Hip Ratio"
        DerivedBodyMetric.WaistHeightRatio -> "Waist-Height Ratio"
    }

    else -> id
}

internal fun valueWithUnit(value: Double?, unit: BodyMetricUnit): String {
    if (value == null) return MISSING_VALUE_PLACEHOLDER
    val number = formatDecimal(value)
    return if (unit == BodyMetricUnit.Unitless) number else "$number ${unit.symbol}"
}

private const val MISSING_VALUE_PLACEHOLDER = "--"

private fun formatDecimal(value: Double): String {
    val nf = NumberFormat.getNumberInstance()
    nf.isGroupingUsed = false
    nf.maximumFractionDigits = 2
    return nf.format(value)
}
