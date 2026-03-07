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

interface BodyMetric {
    val name: String // implemented implicitly by enum
    val id: String
    val metricType: BodyMetricType
    val unit: BodyMetricUnit
    val valueSelector: (BodyMeasurement, DerivedMetrics) -> Double?

    companion object {
        val entries: List<BodyMetric>
            get() = MeasuredBodyMetric.entries + DerivedBodyMetric.entries
    }
}

enum class MeasuredBodyMetric(
    override val id: String,
    override val metricType: BodyMetricType,
    override val unit: BodyMetricUnit,
    override val valueSelector: (BodyMeasurement, DerivedMetrics) -> Double?,
) : BodyMetric {
    Weight(
        id = "weight_kg",
        metricType = BodyMetricType.Weight,
        unit = BodyMetricUnit.Kilogram,
        valueSelector = { measurement, _ -> measurement.weightKg },
    ),
    BodyFat(
        id = "body_fat_percent",
        metricType = BodyMetricType.Weight,
        unit = BodyMetricUnit.Percent,
        valueSelector = { measurement, _ -> measurement.bodyFatPercent },
    ),
    NeckCircumference(
        id = "neck_cm",
        metricType = BodyMetricType.Circumference,
        unit = BodyMetricUnit.Centimeter,
        valueSelector = { measurement, _ -> measurement.neckCircumferenceCm },
    ),
    ChestCircumference(
        id = "chest_cm",
        metricType = BodyMetricType.Circumference,
        unit = BodyMetricUnit.Centimeter,
        valueSelector = { measurement, _ -> measurement.chestCircumferenceCm },
    ),
    WaistCircumference(
        id = "waist_cm",
        metricType = BodyMetricType.Circumference,
        unit = BodyMetricUnit.Centimeter,
        valueSelector = { measurement, _ -> measurement.waistCircumferenceCm },
    ),
    AbdomenCircumference(
        id = "abdomen_cm",
        metricType = BodyMetricType.Circumference,
        unit = BodyMetricUnit.Centimeter,
        valueSelector = { measurement, _ -> measurement.abdomenCircumferenceCm },
    ),
    HipCircumference(
        id = "hip_cm",
        metricType = BodyMetricType.Circumference,
        unit = BodyMetricUnit.Centimeter,
        valueSelector = { measurement, _ -> measurement.hipCircumferenceCm },
    ),
    ChestSkinfold(
        id = "chest_skinfold_mm",
        metricType = BodyMetricType.SkinfoldThickness,
        unit = BodyMetricUnit.Millimeter,
        valueSelector = { measurement, _ -> measurement.chestSkinfoldMm },
    ),
    AbdomenSkinfold(
        id = "abdomen_skinfold_mm",
        metricType = BodyMetricType.SkinfoldThickness,
        unit = BodyMetricUnit.Millimeter,
        valueSelector = { measurement, _ -> measurement.abdomenSkinfoldMm },
    ),
    ThighSkinfold(
        id = "thigh_skinfold_mm",
        metricType = BodyMetricType.SkinfoldThickness,
        unit = BodyMetricUnit.Millimeter,
        valueSelector = { measurement, _ -> measurement.thighSkinfoldMm },
    ),
    TricepsSkinfold(
        id = "triceps_skinfold_mm",
        metricType = BodyMetricType.SkinfoldThickness,
        unit = BodyMetricUnit.Millimeter,
        valueSelector = { measurement, _ -> measurement.tricepsSkinfoldMm },
    ),
    SuprailiacSkinfold(
        id = "suprailiac_skinfold_mm",
        metricType = BodyMetricType.SkinfoldThickness,
        unit = BodyMetricUnit.Millimeter,
        valueSelector = { measurement, _ -> measurement.suprailiacSkinfoldMm },
    ),
}

enum class DerivedBodyMetric(
    override val id: String,
    override val metricType: BodyMetricType,
    override val unit: BodyMetricUnit,
    override val valueSelector: (BodyMeasurement, DerivedMetrics) -> Double?,
) : BodyMetric {
    Bmi(
        id = "bmi",
        metricType = BodyMetricType.AnalysisResult,
        unit = BodyMetricUnit.Unitless,
        valueSelector = { _, derived -> derived.bmi },
    ),
    NavyBodyFatPercent(
        id = "body_fat_navy_percent",
        metricType = BodyMetricType.AnalysisResult,
        unit = BodyMetricUnit.Percent,
        valueSelector = { _, derived -> derived.navyBodyFatPercent },
    ),
    SkinfoldBodyFatPercent(
        id = "body_fat_skinfold_percent",
        metricType = BodyMetricType.AnalysisResult,
        unit = BodyMetricUnit.Percent,
        valueSelector = { _, derived -> derived.skinfold3SiteBodyFatPercent },
    ),
    WaistHipRatio(
        id = "waist_hip_ratio",
        metricType = BodyMetricType.AnalysisResult,
        unit = BodyMetricUnit.Unitless,
        valueSelector = { _, derived -> derived.waistHipRatio },
    ),
    WaistHeightRatio(
        id = "waist_height_ratio",
        metricType = BodyMetricType.AnalysisResult,
        unit = BodyMetricUnit.Unitless,
        valueSelector = { _, derived -> derived.waistHeightRatio },
    ),
}
