package de.t_animal.opensourcebodytracker.domain.metrics

import de.t_animal.opensourcebodytracker.core.model.AnalysisMethod
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.SettingsState
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile

data class MeasurementDependencyState(
    val requiredMeasurements: Set<MeasuredBodyMetric>,
)

class DerivedMetricsDependencyResolver {

    fun resolve(
        enabledAnalysisMethods: Set<AnalysisMethod>,
        profile: UserProfile,
    ): MeasurementDependencyState {
        val sex = profile.sex
        val requiredMeasurements = buildSet {
            if (AnalysisMethod.Bmi in enabledAnalysisMethods) {
                add(MeasuredBodyMetric.Weight)
            }

            if (AnalysisMethod.NavyBodyFat in enabledAnalysisMethods) {
                add(MeasuredBodyMetric.NeckCircumference)
                add(MeasuredBodyMetric.WaistCircumference)
                if (sex == Sex.Female) {
                    add(MeasuredBodyMetric.HipCircumference)
                }
            }

            if (AnalysisMethod.Skinfold3SiteBodyFat in enabledAnalysisMethods) {
                add(MeasuredBodyMetric.ThighSkinfold)
                if (sex == Sex.Male) {
                    add(MeasuredBodyMetric.ChestSkinfold)
                    add(MeasuredBodyMetric.AbdomenSkinfold)
                } else {
                    add(MeasuredBodyMetric.TricepsSkinfold)
                    add(MeasuredBodyMetric.SuprailiacSkinfold)
                }
            }

            if (AnalysisMethod.WaistHipRatio in enabledAnalysisMethods) {
                add(MeasuredBodyMetric.WaistCircumference)
                add(MeasuredBodyMetric.HipCircumference)
            }

            if (AnalysisMethod.WaistHeightRatio in enabledAnalysisMethods) {
                add(MeasuredBodyMetric.WaistCircumference)
            }
        }

        return MeasurementDependencyState(
            requiredMeasurements = requiredMeasurements,
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
