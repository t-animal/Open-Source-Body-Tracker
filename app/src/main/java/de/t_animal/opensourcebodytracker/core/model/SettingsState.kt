package de.t_animal.opensourcebodytracker.core.model

enum class AnalysisMethod {
    NavyBodyFat,
    Skinfold3SiteBodyFat,
}

enum class MeasurementType {
    NeckCircumference,
    WaistCircumference,
    HipCircumference,
    ChestCircumference,
    AbdomenCircumference,
    ChestSkinfold,
    AbdomenSkinfold,
    ThighSkinfold,
    TricepsSkinfold,
    SuprailiacSkinfold,
}

enum class DisplayMetricType {
    Weight,
    NeckCircumference,
    WaistCircumference,
    HipCircumference,
    ChestCircumference,
    AbdomenCircumference,
    ChestSkinfold,
    AbdomenSkinfold,
    ThighSkinfold,
    TricepsSkinfold,
    SuprailiacSkinfold,
    Bmi,
    NavyBodyFatPercent,
    SkinfoldBodyFatPercent,
    WaistHipRatio,
    WaistHeightRatio,
    HipHeightRatio,
}

data class SettingsState(
    val navyBodyFatEnabled: Boolean,
    val skinfoldBodyFatEnabled: Boolean,
    val enabledMeasurements: Set<MeasurementType>,
    val visibleInAnalysis: Set<DisplayMetricType>,
    val visibleInTable: Set<DisplayMetricType>,
)

fun defaultSettingsState(): SettingsState {
    val visibleByDefault = DisplayMetricType.entries.toSet() - setOf(
        DisplayMetricType.ChestSkinfold,
        DisplayMetricType.AbdomenSkinfold,
        DisplayMetricType.ThighSkinfold,
        DisplayMetricType.TricepsSkinfold,
        DisplayMetricType.SuprailiacSkinfold,
        DisplayMetricType.HipHeightRatio,
    )

    return SettingsState(
        navyBodyFatEnabled = true,
        skinfoldBodyFatEnabled = true,
        enabledMeasurements = MeasurementType.entries.toSet(),
        visibleInAnalysis = visibleByDefault,
        visibleInTable = visibleByDefault,
    )
}
