package de.t_animal.opensourcebodytracker.core.model

import java.time.DayOfWeek
import java.time.LocalTime

enum class AnalysisMethod {
    Bmi,
    NavyBodyFat,
    Skinfold3SiteBodyFat,
    WaistHipRatio,
    WaistHeightRatio,
}

data class SettingsState(
    val bmiEnabled: Boolean,
    val navyBodyFatEnabled: Boolean,
    val skinfoldBodyFatEnabled: Boolean,
    val waistHipRatioEnabled: Boolean,
    val waistHeightRatioEnabled: Boolean,
    val onboardingCompleted: Boolean,
    val isDemoMode: Boolean,
    val reminderEnabled: Boolean,
    val reminderWeekdays: Set<DayOfWeek>,
    val reminderTime: LocalTime,
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
    )

    return SettingsState(
        bmiEnabled = true,
        navyBodyFatEnabled = true,
        skinfoldBodyFatEnabled = true,
        waistHipRatioEnabled = true,
        waistHeightRatioEnabled = true,
        onboardingCompleted = false,
        isDemoMode = false,
        reminderEnabled = false,
        reminderWeekdays = setOf(DayOfWeek.SUNDAY),
        reminderTime = LocalTime.of(9, 0),
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
    DerivedBodyMetric.WaistHipRatio -> waistHipRatioEnabled
    DerivedBodyMetric.WaistHeightRatio -> waistHeightRatioEnabled
    else -> true
}
