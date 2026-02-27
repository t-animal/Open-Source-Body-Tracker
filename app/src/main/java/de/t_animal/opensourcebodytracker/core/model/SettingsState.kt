package de.t_animal.opensourcebodytracker.core.model

enum class AnalysisMethod {
    Bmi,
    NavyBodyFat,
    Skinfold3SiteBodyFat,
}

data class SettingsState(
    val bmiEnabled: Boolean,
    val navyBodyFatEnabled: Boolean,
    val skinfoldBodyFatEnabled: Boolean,
    val enabledMeasurements: Set<MeasuredBodyMetric>,
    val visibleInAnalysis: Set<BodyMetric>,
    val visibleInTable: Set<BodyMetric>,
)

fun defaultSettingsState(): SettingsState {
    val visibleByDefault = BodyMetric.entries.toSet() - setOf(
        MeasuredBodyMetric.ChestSkinfold,
        MeasuredBodyMetric.AbdomenSkinfold,
        MeasuredBodyMetric.ThighSkinfold,
        MeasuredBodyMetric.TricepsSkinfold,
        MeasuredBodyMetric.SuprailiacSkinfold,
        DerivedBodyMetric.HipHeightRatio,
    )

    return SettingsState(
        bmiEnabled = true,
        navyBodyFatEnabled = true,
        skinfoldBodyFatEnabled = true,
        enabledMeasurements = MeasuredBodyMetric.entries.toSet(),
        visibleInAnalysis = visibleByDefault,
        visibleInTable = visibleByDefault,
    )
}

fun SettingsState.visibleInAnalysisOrdered(
    allMetrics: List<BodyMetric> = BodyMetric.entries,
): List<BodyMetric> = visibleMetricSetOrdered(visibleInAnalysis, allMetrics)

fun SettingsState.visibleInTableOrdered(
    allMetrics: List<BodyMetric> = BodyMetric.entries,
): List<BodyMetric> = visibleMetricSetOrdered(visibleInTable, allMetrics)

private fun SettingsState.visibleMetricSetOrdered(
    configuredVisibleMetrics: Set<BodyMetric>,
    allMetrics: List<BodyMetric>,
): List<BodyMetric> {
    val activeVisibleMetrics = configuredVisibleMetrics
        .asSequence()
        .filter(::isActiveMetric)
        .toSet()

    return allMetrics.filter { it in activeVisibleMetrics }
}

private fun SettingsState.isActiveMetric(metric: BodyMetric): Boolean = when (metric) {
    DerivedBodyMetric.Bmi -> bmiEnabled
    DerivedBodyMetric.NavyBodyFatPercent -> navyBodyFatEnabled
    DerivedBodyMetric.SkinfoldBodyFatPercent -> skinfoldBodyFatEnabled
    else -> true
}
