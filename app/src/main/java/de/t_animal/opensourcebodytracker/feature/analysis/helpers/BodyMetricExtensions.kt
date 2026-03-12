package de.t_animal.opensourcebodytracker.feature.analysis.helpers

import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.BodyMetricUnit
import de.t_animal.opensourcebodytracker.core.model.DerivedBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric

internal fun BodyMetric.analysisTitle(): String = when (this) {
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
        DerivedBodyMetric.NavyBodyFatPercent -> "Navy Body Fat %"
        DerivedBodyMetric.SkinfoldBodyFatPercent -> "Skinfold Body Fat %"
        DerivedBodyMetric.WaistHipRatio -> "Waist–Hip Ratio"
        DerivedBodyMetric.WaistHeightRatio -> "Waist–Height Ratio"
    }

    else -> id
}

internal fun BodyMetricUnit.suffixWithLeadingSpace(): String = when (this) {
    BodyMetricUnit.Unitless -> ""
    else -> " $symbol"
}
