package de.t_animal.opensourcebodytracker.feature.analysis.helpers

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.BodyMetricUnit
import de.t_animal.opensourcebodytracker.core.model.DerivedBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.label

@Composable
internal fun BodyMetric.analysisTitle(): String = when (this) {
    is MeasuredBodyMetric -> label()

    is DerivedBodyMetric -> when (this) {
        DerivedBodyMetric.Bmi -> stringResource(R.string.analysis_chart_title_bmi)
        DerivedBodyMetric.NavyBodyFatPercent -> stringResource(R.string.analysis_chart_title_navy_body_fat)
        DerivedBodyMetric.SkinfoldBodyFatPercent -> stringResource(R.string.analysis_chart_title_skinfold_body_fat)
        DerivedBodyMetric.WaistHipRatio -> stringResource(R.string.analysis_chart_title_whr)
        DerivedBodyMetric.WaistHeightRatio -> stringResource(R.string.analysis_chart_title_whtr)
    }

    else -> id
}

internal fun BodyMetricUnit.suffixWithLeadingSpace(): String = when (this) {
    BodyMetricUnit.Unitless -> ""
    else -> " $symbol"
}
