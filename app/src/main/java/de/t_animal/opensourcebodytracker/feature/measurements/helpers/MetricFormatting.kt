package de.t_animal.opensourcebodytracker.feature.measurements.helpers

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.t_animal.opensourcebodytracker.R
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

@Composable
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

@Composable
internal fun BodyMetric.label(): String = when (this) {
    is MeasuredBodyMetric -> when (this) {
        MeasuredBodyMetric.Weight -> stringResource(R.string.metric_label_weight)
        MeasuredBodyMetric.BodyFat -> stringResource(R.string.metric_label_body_fat)
        MeasuredBodyMetric.NeckCircumference -> stringResource(R.string.metric_label_neck)
        MeasuredBodyMetric.WaistCircumference -> stringResource(R.string.metric_label_waist)
        MeasuredBodyMetric.HipCircumference -> stringResource(R.string.metric_label_hip)
        MeasuredBodyMetric.ChestCircumference -> stringResource(R.string.metric_label_chest)
        MeasuredBodyMetric.AbdomenCircumference -> stringResource(R.string.metric_label_abdomen)
        MeasuredBodyMetric.ChestSkinfold -> stringResource(R.string.metric_label_chest_skinfold)
        MeasuredBodyMetric.AbdomenSkinfold -> stringResource(R.string.metric_label_abdomen_skinfold)
        MeasuredBodyMetric.ThighSkinfold -> stringResource(R.string.metric_label_thigh_skinfold)
        MeasuredBodyMetric.TricepsSkinfold -> stringResource(R.string.metric_label_triceps_skinfold)
        MeasuredBodyMetric.SuprailiacSkinfold -> stringResource(R.string.metric_label_suprailiac_skinfold)
    }

    is DerivedBodyMetric -> when (this) {
        DerivedBodyMetric.Bmi -> stringResource(R.string.metric_label_bmi)
        DerivedBodyMetric.NavyBodyFatPercent -> stringResource(R.string.metric_label_navy_body_fat)
        DerivedBodyMetric.SkinfoldBodyFatPercent -> stringResource(R.string.metric_label_skinfold_body_fat)
        DerivedBodyMetric.WaistHipRatio -> stringResource(R.string.metric_label_whr)
        DerivedBodyMetric.WaistHeightRatio -> stringResource(R.string.metric_label_whtr)
    }

    else -> id
}

@Composable
internal fun BodyMetric.fullName(): String = when (this) {
    is MeasuredBodyMetric -> when (this) {
        MeasuredBodyMetric.Weight -> stringResource(R.string.metric_fullname_weight)
        MeasuredBodyMetric.BodyFat -> stringResource(R.string.metric_fullname_body_fat)
        MeasuredBodyMetric.NeckCircumference -> stringResource(R.string.metric_fullname_neck)
        MeasuredBodyMetric.WaistCircumference -> stringResource(R.string.metric_fullname_waist)
        MeasuredBodyMetric.HipCircumference -> stringResource(R.string.metric_fullname_hip)
        MeasuredBodyMetric.ChestCircumference -> stringResource(R.string.metric_fullname_chest)
        MeasuredBodyMetric.AbdomenCircumference -> stringResource(R.string.metric_fullname_abdomen)
        MeasuredBodyMetric.ChestSkinfold -> stringResource(R.string.metric_fullname_chest_skinfold)
        MeasuredBodyMetric.AbdomenSkinfold -> stringResource(R.string.metric_fullname_abdomen_skinfold)
        MeasuredBodyMetric.ThighSkinfold -> stringResource(R.string.metric_fullname_thigh_skinfold)
        MeasuredBodyMetric.TricepsSkinfold -> stringResource(R.string.metric_fullname_triceps_skinfold)
        MeasuredBodyMetric.SuprailiacSkinfold -> stringResource(R.string.metric_fullname_suprailiac_skinfold)
    }

    is DerivedBodyMetric -> when (this) {
        DerivedBodyMetric.Bmi -> stringResource(R.string.metric_fullname_bmi)
        DerivedBodyMetric.NavyBodyFatPercent -> stringResource(R.string.metric_fullname_navy_body_fat)
        DerivedBodyMetric.SkinfoldBodyFatPercent -> stringResource(R.string.metric_fullname_skinfold_body_fat)
        DerivedBodyMetric.WaistHipRatio -> stringResource(R.string.metric_fullname_whr)
        DerivedBodyMetric.WaistHeightRatio -> stringResource(R.string.metric_fullname_whtr)
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
