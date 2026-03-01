package de.t_animal.opensourcebodytracker.feature.measurements.helpers

import de.t_animal.opensourcebodytracker.core.model.BodyMetricType
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.Sex


fun resolveVisibleMeasuredMetrics(
    enabledMeasurements: Set<MeasuredBodyMetric>,
    sex: Sex,
): List<MeasuredBodyMetric> {
    return MeasuredBodyMetric.entries.filter { metric ->
        metric in enabledMeasurements && isMetricVisible(metric = metric, sex = sex)
    }
}

private fun isMetricVisible(
    metric: MeasuredBodyMetric,
    sex: Sex,
): Boolean {
    if (metric.metricType != BodyMetricType.SkinfoldThickness) {
        return true
    }

    return when (metric) {
        MeasuredBodyMetric.ChestSkinfold,
        MeasuredBodyMetric.AbdomenSkinfold,
        -> sex == Sex.Male

        MeasuredBodyMetric.TricepsSkinfold,
        MeasuredBodyMetric.SuprailiacSkinfold,
        -> sex == Sex.Female

        MeasuredBodyMetric.ThighSkinfold -> true

        else -> false
    }
}
