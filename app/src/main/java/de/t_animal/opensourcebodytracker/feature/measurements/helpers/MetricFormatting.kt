package de.t_animal.opensourcebodytracker.feature.measurements.helpers

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.BodyMetricUnit
import de.t_animal.opensourcebodytracker.core.model.DerivedBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MetricRating
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UnitSystem
import de.t_animal.opensourcebodytracker.feature.measurements.components.GuidanceImageSide
import de.t_animal.opensourcebodytracker.ui.helpers.displaySymbol
import de.t_animal.opensourcebodytracker.core.model.forMetric
import de.t_animal.opensourcebodytracker.core.model.toDisplayValue
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
    unitSystem: UnitSystem,
): List<MetricDisplayItem> {
    return visibleMetrics.map { metric ->
        MetricDisplayItem(
            label = metric.label(),
            fullName = metric.fullName(),
            value = metric.formattedValue(item, unitSystem),
            rating = (metric as? DerivedBodyMetric)?.let { item.derivedMetricRatings.forMetric(it) },
        )
    }
}

internal fun BodyMetric.formattedValue(
    item: MeasurementListItemUiModel,
    unitSystem: UnitSystem,
): String {
    val value = valueSelector(item.measurement, item.derivedMetrics)
    return valueWithUnit(value, unit, unitSystem)
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

internal fun valueWithUnit(
    value: Double?,
    unit: BodyMetricUnit,
    unitSystem: UnitSystem,
): String {
    if (value == null) return MISSING_VALUE_PLACEHOLDER
    val displayValue = value.toDisplayValue(unit, unitSystem)
    val number = formatDecimal(displayValue)
    val symbol = unit.displaySymbol(unitSystem)
    return if (symbol.isEmpty()) number else "$number $symbol"
}

@androidx.annotation.StringRes
internal fun MeasuredBodyMetric.guidanceResId(): Int = when (this) {
    MeasuredBodyMetric.Weight -> R.string.measurement_guidance_weight
    MeasuredBodyMetric.BodyFat -> R.string.measurement_guidance_body_fat
    MeasuredBodyMetric.NeckCircumference -> R.string.measurement_guidance_neck_circumference
    MeasuredBodyMetric.ChestCircumference -> R.string.measurement_guidance_chest_circumference
    MeasuredBodyMetric.WaistCircumference -> R.string.measurement_guidance_waist_circumference
    MeasuredBodyMetric.AbdomenCircumference -> R.string.measurement_guidance_abdomen_circumference
    MeasuredBodyMetric.HipCircumference -> R.string.measurement_guidance_hip_circumference
    MeasuredBodyMetric.ChestSkinfold -> R.string.measurement_guidance_chest_skinfold
    MeasuredBodyMetric.AbdomenSkinfold -> R.string.measurement_guidance_abdomen_skinfold
    MeasuredBodyMetric.ThighSkinfold -> R.string.measurement_guidance_thigh_skinfold
    MeasuredBodyMetric.TricepsSkinfold -> R.string.measurement_guidance_triceps_skinfold
    MeasuredBodyMetric.SuprailiacSkinfold -> R.string.measurement_guidance_suprailiac_skinfold
}

internal fun MeasuredBodyMetric.initialGuidanceOrientation(): GuidanceImageSide =
    if (this == MeasuredBodyMetric.TricepsSkinfold) GuidanceImageSide.Back
    else GuidanceImageSide.Front

@androidx.annotation.DrawableRes
internal fun MeasuredBodyMetric.guidanceImageResId(sex: Sex, side: GuidanceImageSide): Int =
    when (sex) {
        Sex.Male -> when (side) {
            GuidanceImageSide.Front -> when (this) {
                MeasuredBodyMetric.Weight -> R.drawable.measurement_guidance_weight_male_front
                MeasuredBodyMetric.BodyFat -> R.drawable.measurement_guidance_body_fat_male_front
                MeasuredBodyMetric.NeckCircumference -> R.drawable.measurement_guidance_neck_circumference_male_front
                MeasuredBodyMetric.ChestCircumference -> R.drawable.measurement_guidance_chest_circumference_male_front
                MeasuredBodyMetric.WaistCircumference -> R.drawable.measurement_guidance_waist_circumference_male_front
                MeasuredBodyMetric.AbdomenCircumference -> R.drawable.measurement_guidance_abdomen_circumference_male_front
                MeasuredBodyMetric.HipCircumference -> R.drawable.measurement_guidance_hip_circumference_male_front
                MeasuredBodyMetric.ChestSkinfold -> R.drawable.measurement_guidance_chest_skinfold_male_front
                MeasuredBodyMetric.AbdomenSkinfold -> R.drawable.measurement_guidance_abdomen_skinfold_male_front
                MeasuredBodyMetric.ThighSkinfold -> R.drawable.measurement_guidance_thigh_skinfold_male_front
                MeasuredBodyMetric.TricepsSkinfold -> R.drawable.measurement_guidance_triceps_skinfold_male_front
                MeasuredBodyMetric.SuprailiacSkinfold -> R.drawable.measurement_guidance_suprailiac_skinfold_male_front
            }
            GuidanceImageSide.Back -> when (this) {
                MeasuredBodyMetric.Weight -> R.drawable.measurement_guidance_weight_male_back
                MeasuredBodyMetric.BodyFat -> R.drawable.measurement_guidance_body_fat_male_back
                MeasuredBodyMetric.NeckCircumference -> R.drawable.measurement_guidance_neck_circumference_male_back
                MeasuredBodyMetric.ChestCircumference -> R.drawable.measurement_guidance_chest_circumference_male_back
                MeasuredBodyMetric.WaistCircumference -> R.drawable.measurement_guidance_waist_circumference_male_back
                MeasuredBodyMetric.AbdomenCircumference -> R.drawable.measurement_guidance_abdomen_circumference_male_back
                MeasuredBodyMetric.HipCircumference -> R.drawable.measurement_guidance_hip_circumference_male_back
                MeasuredBodyMetric.ChestSkinfold -> R.drawable.measurement_guidance_chest_skinfold_male_back
                MeasuredBodyMetric.AbdomenSkinfold -> R.drawable.measurement_guidance_abdomen_skinfold_male_back
                MeasuredBodyMetric.ThighSkinfold -> R.drawable.measurement_guidance_thigh_skinfold_male_back
                MeasuredBodyMetric.TricepsSkinfold -> R.drawable.measurement_guidance_triceps_skinfold_male_back
                MeasuredBodyMetric.SuprailiacSkinfold -> R.drawable.measurement_guidance_suprailiac_skinfold_male_back
            }
        }
        Sex.Female -> when (side) {
            GuidanceImageSide.Front -> when (this) {
                MeasuredBodyMetric.Weight -> R.drawable.measurement_guidance_weight_female_front
                MeasuredBodyMetric.BodyFat -> R.drawable.measurement_guidance_body_fat_female_front
                MeasuredBodyMetric.NeckCircumference -> R.drawable.measurement_guidance_neck_circumference_female_front
                MeasuredBodyMetric.ChestCircumference -> R.drawable.measurement_guidance_chest_circumference_female_front
                MeasuredBodyMetric.WaistCircumference -> R.drawable.measurement_guidance_waist_circumference_female_front
                MeasuredBodyMetric.AbdomenCircumference -> R.drawable.measurement_guidance_abdomen_circumference_female_front
                MeasuredBodyMetric.HipCircumference -> R.drawable.measurement_guidance_hip_circumference_female_front
                MeasuredBodyMetric.ChestSkinfold -> R.drawable.measurement_guidance_chest_skinfold_female_front
                MeasuredBodyMetric.AbdomenSkinfold -> R.drawable.measurement_guidance_abdomen_skinfold_female_front
                MeasuredBodyMetric.ThighSkinfold -> R.drawable.measurement_guidance_thigh_skinfold_female_front
                MeasuredBodyMetric.TricepsSkinfold -> R.drawable.measurement_guidance_triceps_skinfold_female_front
                MeasuredBodyMetric.SuprailiacSkinfold -> R.drawable.measurement_guidance_suprailiac_skinfold_female_front
            }
            GuidanceImageSide.Back -> when (this) {
                MeasuredBodyMetric.Weight -> R.drawable.measurement_guidance_weight_female_back
                MeasuredBodyMetric.BodyFat -> R.drawable.measurement_guidance_body_fat_female_back
                MeasuredBodyMetric.NeckCircumference -> R.drawable.measurement_guidance_neck_circumference_female_back
                MeasuredBodyMetric.ChestCircumference -> R.drawable.measurement_guidance_chest_circumference_female_back
                MeasuredBodyMetric.WaistCircumference -> R.drawable.measurement_guidance_waist_circumference_female_back
                MeasuredBodyMetric.AbdomenCircumference -> R.drawable.measurement_guidance_abdomen_circumference_female_back
                MeasuredBodyMetric.HipCircumference -> R.drawable.measurement_guidance_hip_circumference_female_back
                MeasuredBodyMetric.ChestSkinfold -> R.drawable.measurement_guidance_chest_skinfold_female_back
                MeasuredBodyMetric.AbdomenSkinfold -> R.drawable.measurement_guidance_abdomen_skinfold_female_back
                MeasuredBodyMetric.ThighSkinfold -> R.drawable.measurement_guidance_thigh_skinfold_female_back
                MeasuredBodyMetric.TricepsSkinfold -> R.drawable.measurement_guidance_triceps_skinfold_female_back
                MeasuredBodyMetric.SuprailiacSkinfold -> R.drawable.measurement_guidance_suprailiac_skinfold_female_back
            }
        }
    }

private const val MISSING_VALUE_PLACEHOLDER = "--"

private fun formatDecimal(value: Double): String {
    val nf = NumberFormat.getNumberInstance()
    nf.isGroupingUsed = false
    nf.maximumFractionDigits = 2
    return nf.format(value)
}
