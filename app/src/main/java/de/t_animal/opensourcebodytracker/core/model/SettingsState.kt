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
