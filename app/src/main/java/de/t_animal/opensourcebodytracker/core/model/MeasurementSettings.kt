package de.t_animal.opensourcebodytracker.core.model

enum class AnalysisMethod {
    Bmi,
    NavyBodyFat,
    Skinfold3SiteBodyFat,
    WaistHipRatio,
    WaistHeightRatio,
}

data class MeasurementSettings(
    val bmiEnabled: Boolean = true,
    val navyBodyFatEnabled: Boolean = true,
    val skinfoldBodyFatEnabled: Boolean = true,
    val waistHipRatioEnabled: Boolean = true,
    val waistHeightRatioEnabled: Boolean = true,
    val enabledMeasurements: Set<MeasuredBodyMetric> = MeasuredBodyMetric.entries.toSet(),
    val visibleInAnalysis: Set<BodyMetric> = defaultVisibleMetrics(),
    val visibleInTable: Set<BodyMetric> = defaultVisibleMetrics(),
) {
    val enabledDerivedMetrics: Set<DerivedBodyMetric>
        get() = buildSet {
            if (bmiEnabled) add(DerivedBodyMetric.Bmi)
            if (navyBodyFatEnabled) add(DerivedBodyMetric.NavyBodyFatPercent)
            if (skinfoldBodyFatEnabled) add(DerivedBodyMetric.SkinfoldBodyFatPercent)
            if (waistHipRatioEnabled) add(DerivedBodyMetric.WaistHipRatio)
            if (waistHeightRatioEnabled) add(DerivedBodyMetric.WaistHeightRatio)
        }

    val enabledAnalysisMethods: Set<AnalysisMethod>
        get() = buildSet {
            if (bmiEnabled) add(AnalysisMethod.Bmi)
            if (navyBodyFatEnabled) add(AnalysisMethod.NavyBodyFat)
            if (skinfoldBodyFatEnabled) add(AnalysisMethod.Skinfold3SiteBodyFat)
            if (waistHipRatioEnabled) add(AnalysisMethod.WaistHipRatio)
            if (waistHeightRatioEnabled) add(AnalysisMethod.WaistHeightRatio)
        }

    val visibleInAnalysisOrdered: List<BodyMetric>
        get() = visibleMetricSetOrdered(visibleInAnalysis)

    val visibleInTableOrdered: List<BodyMetric>
        get() = visibleMetricSetOrdered(visibleInTable)

    private fun visibleMetricSetOrdered(
        configuredVisibleMetrics: Set<BodyMetric>,
    ): List<BodyMetric> {
        val activeVisibleMetrics = configuredVisibleMetrics
            .asSequence()
            .filter(::isActiveMetric)
            .toSet()

        return BodyMetric.entries.filter { it in activeVisibleMetrics }
    }

    private fun isActiveMetric(metric: BodyMetric): Boolean = when (metric) {
        DerivedBodyMetric.Bmi -> bmiEnabled
        DerivedBodyMetric.NavyBodyFatPercent -> navyBodyFatEnabled
        DerivedBodyMetric.SkinfoldBodyFatPercent -> skinfoldBodyFatEnabled
        DerivedBodyMetric.WaistHipRatio -> waistHipRatioEnabled
        DerivedBodyMetric.WaistHeightRatio -> waistHeightRatioEnabled
        else -> true
    }
}

private fun defaultVisibleMetrics(): Set<BodyMetric> =
    BodyMetric.entries.toSet() - setOf(
        MeasuredBodyMetric.ChestSkinfold,
        MeasuredBodyMetric.AbdomenSkinfold,
        MeasuredBodyMetric.ThighSkinfold,
        MeasuredBodyMetric.TricepsSkinfold,
        MeasuredBodyMetric.SuprailiacSkinfold,
    )
