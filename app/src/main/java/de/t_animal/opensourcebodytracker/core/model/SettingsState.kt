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
    val enabledMeasurements: Set<BodyMetric>,
    val visibleInAnalysis: Set<BodyMetric>,
    val visibleInTable: Set<BodyMetric>,
)

fun defaultSettingsState(): SettingsState {
    val visibleByDefault = BodyMetric.entries.toSet() - setOf(
        BodyMetric.ChestSkinfold,
        BodyMetric.AbdomenSkinfold,
        BodyMetric.ThighSkinfold,
        BodyMetric.TricepsSkinfold,
        BodyMetric.SuprailiacSkinfold,
        BodyMetric.HipHeightRatio,
    )

    return SettingsState(
        bmiEnabled = true,
        navyBodyFatEnabled = true,
        skinfoldBodyFatEnabled = true,
        enabledMeasurements = BodyMetric.entries.filterTo(mutableSetOf()) { it.isMeasured },
        visibleInAnalysis = visibleByDefault,
        visibleInTable = visibleByDefault,
    )
}
