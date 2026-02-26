package de.t_animal.opensourcebodytracker.core.model

enum class BodyMetricType {
    Weight,
    Circumference,
    SkinfoldThickness,
    AnalysisResult,
}

enum class BodyMetricUnit(
    val symbol: String,
) {
    Kilogram("kg"),
    Centimeter("cm"),
    Millimeter("mm"),
    Percent("%"),
    Unitless(""),
}

enum class BodyMetric(
    val id: String,
    val metricType: BodyMetricType,
    val unit: BodyMetricUnit,
    val isMeasured: Boolean,
    val valueSelector: (BodyMeasurement, DerivedMetrics) -> Double?,
) {
    Weight(
        id = "weight_kg",
        metricType = BodyMetricType.Weight,
        unit = BodyMetricUnit.Kilogram,
        isMeasured = true,
        valueSelector = { measurement, _ -> measurement.weightKg },
    ),
    NeckCircumference(
        id = "neck_cm",
        metricType = BodyMetricType.Circumference,
        unit = BodyMetricUnit.Centimeter,
        isMeasured = true,
        valueSelector = { measurement, _ -> measurement.neckCircumferenceCm },
    ),
    ChestCircumference(
        id = "chest_cm",
        metricType = BodyMetricType.Circumference,
        unit = BodyMetricUnit.Centimeter,
        isMeasured = true,
        valueSelector = { measurement, _ -> measurement.chestCircumferenceCm },
    ),
    WaistCircumference(
        id = "waist_cm",
        metricType = BodyMetricType.Circumference,
        unit = BodyMetricUnit.Centimeter,
        isMeasured = true,
        valueSelector = { measurement, _ -> measurement.waistCircumferenceCm },
    ),
    AbdomenCircumference(
        id = "abdomen_cm",
        metricType = BodyMetricType.Circumference,
        unit = BodyMetricUnit.Centimeter,
        isMeasured = true,
        valueSelector = { measurement, _ -> measurement.abdomenCircumferenceCm },
    ),
    HipCircumference(
        id = "hip_cm",
        metricType = BodyMetricType.Circumference,
        unit = BodyMetricUnit.Centimeter,
        isMeasured = true,
        valueSelector = { measurement, _ -> measurement.hipCircumferenceCm },
    ),
    ChestSkinfold(
        id = "chest_skinfold_mm",
        metricType = BodyMetricType.SkinfoldThickness,
        unit = BodyMetricUnit.Millimeter,
        isMeasured = true,
        valueSelector = { measurement, _ -> measurement.chestSkinfoldMm },
    ),
    AbdomenSkinfold(
        id = "abdomen_skinfold_mm",
        metricType = BodyMetricType.SkinfoldThickness,
        unit = BodyMetricUnit.Millimeter,
        isMeasured = true,
        valueSelector = { measurement, _ -> measurement.abdomenSkinfoldMm },
    ),
    ThighSkinfold(
        id = "thigh_skinfold_mm",
        metricType = BodyMetricType.SkinfoldThickness,
        unit = BodyMetricUnit.Millimeter,
        isMeasured = true,
        valueSelector = { measurement, _ -> measurement.thighSkinfoldMm },
    ),
    TricepsSkinfold(
        id = "triceps_skinfold_mm",
        metricType = BodyMetricType.SkinfoldThickness,
        unit = BodyMetricUnit.Millimeter,
        isMeasured = true,
        valueSelector = { measurement, _ -> measurement.tricepsSkinfoldMm },
    ),
    SuprailiacSkinfold(
        id = "suprailiac_skinfold_mm",
        metricType = BodyMetricType.SkinfoldThickness,
        unit = BodyMetricUnit.Millimeter,
        isMeasured = true,
        valueSelector = { measurement, _ -> measurement.suprailiacSkinfoldMm },
    ),
    Bmi(
        id = "bmi",
        metricType = BodyMetricType.AnalysisResult,
        unit = BodyMetricUnit.Unitless,
        isMeasured = false,
        valueSelector = { _, derived -> derived.bmi },
    ),
    NavyBodyFatPercent(
        id = "body_fat_navy_percent",
        metricType = BodyMetricType.AnalysisResult,
        unit = BodyMetricUnit.Percent,
        isMeasured = false,
        valueSelector = { _, derived -> derived.navyBodyFatPercent },
    ),
    SkinfoldBodyFatPercent(
        id = "body_fat_skinfold_percent",
        metricType = BodyMetricType.AnalysisResult,
        unit = BodyMetricUnit.Percent,
        isMeasured = false,
        valueSelector = { _, derived -> derived.skinfold3SiteBodyFatPercent },
    ),
    WaistHipRatio(
        id = "waist_hip_ratio",
        metricType = BodyMetricType.AnalysisResult,
        unit = BodyMetricUnit.Unitless,
        isMeasured = false,
        valueSelector = { _, derived -> derived.waistHipRatio },
    ),
    WaistHeightRatio(
        id = "waist_height_ratio",
        metricType = BodyMetricType.AnalysisResult,
        unit = BodyMetricUnit.Unitless,
        isMeasured = false,
        valueSelector = { _, derived -> derived.waistHeightRatio },
    ),
    HipHeightRatio(
        id = "hip_height_ratio",
        metricType = BodyMetricType.AnalysisResult,
        unit = BodyMetricUnit.Unitless,
        isMeasured = false,
        valueSelector = { _, derived -> derived.hipHeightRatio },
    ),
}
