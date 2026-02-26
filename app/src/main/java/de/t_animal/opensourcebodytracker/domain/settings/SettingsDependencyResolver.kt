package de.t_animal.opensourcebodytracker.domain.settings

import de.t_animal.opensourcebodytracker.core.model.AnalysisMethod
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.SettingsState
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile

data class MeasurementDependencyState(
    val requiredMeasurements: Set<BodyMetric>,
)

class SettingsDependencyResolver {

    fun resolve(
        enabledAnalysisMethods: Set<AnalysisMethod>,
        profile: UserProfile,
    ): MeasurementDependencyState {
        val sex = profile.sex
        val requiredMeasurements = buildSet {
            if (AnalysisMethod.NavyBodyFat in enabledAnalysisMethods) {
                add(BodyMetric.NeckCircumference)
                add(BodyMetric.WaistCircumference)
                if (sex == Sex.Female) {
                    add(BodyMetric.HipCircumference)
                }
            }

            if (AnalysisMethod.Skinfold3SiteBodyFat in enabledAnalysisMethods) {
                add(BodyMetric.ThighSkinfold)
                if (sex == Sex.Male) {
                    add(BodyMetric.ChestSkinfold)
                    add(BodyMetric.AbdomenSkinfold)
                } else {
                    add(BodyMetric.TricepsSkinfold)
                    add(BodyMetric.SuprailiacSkinfold)
                }
            }
        }

        return MeasurementDependencyState(
            requiredMeasurements = requiredMeasurements,
        )
    }
}

fun SettingsState.enabledAnalysisMethods(): Set<AnalysisMethod> = buildSet {
    if (navyBodyFatEnabled) {
        add(AnalysisMethod.NavyBodyFat)
    }
    if (skinfoldBodyFatEnabled) {
        add(AnalysisMethod.Skinfold3SiteBodyFat)
    }
}
