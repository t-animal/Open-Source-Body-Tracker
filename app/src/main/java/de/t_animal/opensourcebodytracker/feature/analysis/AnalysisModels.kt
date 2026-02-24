package de.t_animal.opensourcebodytracker.feature.analysis

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.DerivedMetrics

enum class AnalysisDuration(
    val label: String,
) {
    OneMonth("1M"),
    ThreeMonths("3M"),
    SixMonths("6M"),
    OneYear("1Y"),
    All("All"),
}

data class AnalysisMetricDefinition(
    val id: String,
    val title: String,
    val unit: String?,
    val valueSelector: (BodyMeasurement, DerivedMetrics) -> Double?,
)

data class AnalysisChartPoint(
    val epochMillis: Long,
    val value: Double,
)

data class AnalysisYAxisRange(
    val min: Double,
    val max: Double,
)

data class AnalysisMetricChartUiModel(
    val definition: AnalysisMetricDefinition,
    val points: List<AnalysisChartPoint>,
    val yAxisRange: AnalysisYAxisRange?,
)

data class AnalysisUiState(
    val selectedDuration: AnalysisDuration = AnalysisDuration.ThreeMonths,
    val metricCharts: List<AnalysisMetricChartUiModel> = emptyList(),
    val isLoading: Boolean = true,
)

internal val analysisMetricDefinitions: List<AnalysisMetricDefinition> = listOf(
    AnalysisMetricDefinition(
        id = "weight_kg",
        title = "Weight",
        unit = "kg",
        valueSelector = { measurement, _ -> measurement.weightKg },
    ),
    AnalysisMetricDefinition(
        id = "neck_cm",
        title = "Neck",
        unit = "cm",
        valueSelector = { measurement, _ -> measurement.neckCircumferenceCm },
    ),
    AnalysisMetricDefinition(
        id = "chest_cm",
        title = "Chest",
        unit = "cm",
        valueSelector = { measurement, _ -> measurement.chestCircumferenceCm },
    ),
    AnalysisMetricDefinition(
        id = "waist_cm",
        title = "Waist",
        unit = "cm",
        valueSelector = { measurement, _ -> measurement.waistCircumferenceCm },
    ),
    AnalysisMetricDefinition(
        id = "abdomen_cm",
        title = "Abdomen",
        unit = "cm",
        valueSelector = { measurement, _ -> measurement.abdomenCircumferenceCm },
    ),
    AnalysisMetricDefinition(
        id = "hip_cm",
        title = "Hip",
        unit = "cm",
        valueSelector = { measurement, _ -> measurement.hipCircumferenceCm },
    ),
    AnalysisMetricDefinition(
        id = "chest_skinfold_mm",
        title = "Chest Skinfold",
        unit = "mm",
        valueSelector = { measurement, _ -> measurement.chestSkinfoldMm },
    ),
    AnalysisMetricDefinition(
        id = "abdomen_skinfold_mm",
        title = "Abdomen Skinfold",
        unit = "mm",
        valueSelector = { measurement, _ -> measurement.abdomenSkinfoldMm },
    ),
    AnalysisMetricDefinition(
        id = "thigh_skinfold_mm",
        title = "Thigh Skinfold",
        unit = "mm",
        valueSelector = { measurement, _ -> measurement.thighSkinfoldMm },
    ),
    AnalysisMetricDefinition(
        id = "triceps_skinfold_mm",
        title = "Triceps Skinfold",
        unit = "mm",
        valueSelector = { measurement, _ -> measurement.tricepsSkinfoldMm },
    ),
    AnalysisMetricDefinition(
        id = "suprailiac_skinfold_mm",
        title = "Suprailiac Skinfold",
        unit = "mm",
        valueSelector = { measurement, _ -> measurement.suprailiacSkinfoldMm },
    ),
    AnalysisMetricDefinition(
        id = "bmi",
        title = "BMI",
        unit = null,
        valueSelector = { _, derived -> derived.bmi },
    ),
    AnalysisMetricDefinition(
        id = "body_fat_navy_percent",
        title = "Navy Body Fat %",
        unit = "%",
        valueSelector = { _, derived -> derived.navyBodyFatPercent },
    ),
    AnalysisMetricDefinition(
        id = "body_fat_skinfold_percent",
        title = "Skinfold Body Fat %",
        unit = "%",
        valueSelector = { _, derived -> derived.skinfold3SiteBodyFatPercent },
    ),
    AnalysisMetricDefinition(
        id = "waist_hip_ratio",
        title = "Waist–Hip Ratio",
        unit = null,
        valueSelector = { _, derived -> derived.waistHipRatio },
    ),
    AnalysisMetricDefinition(
        id = "waist_height_ratio",
        title = "Waist–Height Ratio",
        unit = null,
        valueSelector = { _, derived -> derived.waistHeightRatio },
    ),
    AnalysisMetricDefinition(
        id = "hip_height_ratio",
        title = "Hip–Height Ratio",
        unit = null,
        valueSelector = { _, derived -> derived.hipHeightRatio },
    ),
)
