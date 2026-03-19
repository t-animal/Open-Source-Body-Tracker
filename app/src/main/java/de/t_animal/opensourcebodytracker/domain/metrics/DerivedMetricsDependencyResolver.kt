package de.t_animal.opensourcebodytracker.domain.metrics

import de.t_animal.opensourcebodytracker.core.model.AnalysisMethod
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.SettingsState
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile

data class DerivedBodyMetricsDependencies(
    val requiredMeasurements: Set<MeasuredBodyMetric> = emptySet(),
    val measurementToAnalysisMethods: Map<MeasuredBodyMetric, Set<AnalysisMethod>> = emptyMap(),
)

class DerivedMetricsDependencyResolver {

    fun resolve(
        enabledAnalysisMethods: Set<AnalysisMethod>,
        profile: UserProfile,
    ): DerivedBodyMetricsDependencies {
        val sex = profile.sex
        val requiredMeasurements = mutableSetOf<MeasuredBodyMetric>()
        val measurementToAnalysisMethods = mutableMapOf<MeasuredBodyMetric, MutableSet<AnalysisMethod>>()

        fun requireMeasurementFor(
            measurement: MeasuredBodyMetric,
            method: AnalysisMethod,
        ) {
            requiredMeasurements += measurement
            measurementToAnalysisMethods.getOrPut(measurement) { mutableSetOf() } += method
        }

        if (AnalysisMethod.Bmi in enabledAnalysisMethods) {
            requireMeasurementFor(
                measurement = MeasuredBodyMetric.Weight,
                method = AnalysisMethod.Bmi,
            )
        }

        if (AnalysisMethod.NavyBodyFat in enabledAnalysisMethods) {
            requireMeasurementFor(
                measurement = MeasuredBodyMetric.NeckCircumference,
                method = AnalysisMethod.NavyBodyFat,
            )
            requireMeasurementFor(
                measurement = MeasuredBodyMetric.WaistCircumference,
                method = AnalysisMethod.NavyBodyFat,
            )
            if (sex == Sex.Female) {
                requireMeasurementFor(
                    measurement = MeasuredBodyMetric.HipCircumference,
                    method = AnalysisMethod.NavyBodyFat,
                )
            }
        }

        if (AnalysisMethod.Skinfold3SiteBodyFat in enabledAnalysisMethods) {
            requireMeasurementFor(
                measurement = MeasuredBodyMetric.ThighSkinfold,
                method = AnalysisMethod.Skinfold3SiteBodyFat,
            )
            if (sex == Sex.Male) {
                requireMeasurementFor(
                    measurement = MeasuredBodyMetric.ChestSkinfold,
                    method = AnalysisMethod.Skinfold3SiteBodyFat,
                )
                requireMeasurementFor(
                    measurement = MeasuredBodyMetric.AbdomenSkinfold,
                    method = AnalysisMethod.Skinfold3SiteBodyFat,
                )
            } else {
                requireMeasurementFor(
                    measurement = MeasuredBodyMetric.TricepsSkinfold,
                    method = AnalysisMethod.Skinfold3SiteBodyFat,
                )
                requireMeasurementFor(
                    measurement = MeasuredBodyMetric.SuprailiacSkinfold,
                    method = AnalysisMethod.Skinfold3SiteBodyFat,
                )
            }
        }

        if (AnalysisMethod.WaistHipRatio in enabledAnalysisMethods) {
            requireMeasurementFor(
                measurement = MeasuredBodyMetric.WaistCircumference,
                method = AnalysisMethod.WaistHipRatio,
            )
            requireMeasurementFor(
                measurement = MeasuredBodyMetric.HipCircumference,
                method = AnalysisMethod.WaistHipRatio,
            )
        }

        if (AnalysisMethod.WaistHeightRatio in enabledAnalysisMethods) {
            requireMeasurementFor(
                measurement = MeasuredBodyMetric.WaistCircumference,
                method = AnalysisMethod.WaistHeightRatio,
            )
        }

        return DerivedBodyMetricsDependencies(
            requiredMeasurements = requiredMeasurements.toSet(),
            measurementToAnalysisMethods = measurementToAnalysisMethods
                .mapValues { (_, methods) -> methods.toSet() },
        )
    }
}

fun SettingsState.enabledAnalysisMethods(): Set<AnalysisMethod> = buildSet {
    if (bmiEnabled) {
        add(AnalysisMethod.Bmi)
    }
    if (navyBodyFatEnabled) {
        add(AnalysisMethod.NavyBodyFat)
    }
    if (skinfoldBodyFatEnabled) {
        add(AnalysisMethod.Skinfold3SiteBodyFat)
    }
    if (waistHipRatioEnabled) {
        add(AnalysisMethod.WaistHipRatio)
    }
    if (waistHeightRatioEnabled) {
        add(AnalysisMethod.WaistHeightRatio)
    }
}
