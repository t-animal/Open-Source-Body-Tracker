package de.t_animal.opensourcebodytracker.core.model

data class DerivedMetricRatings(
    val bmi: MetricRating? = null,
    val navyBodyFatPercent: MetricRating? = null,
    val skinfold3SiteBodyFatPercent: MetricRating? = null,
    val waistHipRatio: MetricRating? = null,
    val waistHeightRatio: MetricRating? = null,
)

fun DerivedMetricRatings.forMetric(metric: DerivedBodyMetric): MetricRating? = when (metric) {
    DerivedBodyMetric.Bmi -> bmi
    DerivedBodyMetric.NavyBodyFatPercent -> navyBodyFatPercent
    DerivedBodyMetric.SkinfoldBodyFatPercent -> skinfold3SiteBodyFatPercent
    DerivedBodyMetric.WaistHipRatio -> waistHipRatio
    DerivedBodyMetric.WaistHeightRatio -> waistHeightRatio
}
