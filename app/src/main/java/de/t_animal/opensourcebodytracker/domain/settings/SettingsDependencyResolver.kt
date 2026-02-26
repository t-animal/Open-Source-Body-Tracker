package de.t_animal.opensourcebodytracker.domain.settings

import de.t_animal.opensourcebodytracker.core.model.AnalysisMethod
import de.t_animal.opensourcebodytracker.core.model.MeasurementType
import de.t_animal.opensourcebodytracker.core.model.SettingsState
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile

data class MeasurementDependencyState(
    val requiredMeasurements: Set<MeasurementType>,
)

class SettingsDependencyResolver {

    fun resolve(
        enabledAnalysisMethods: Set<AnalysisMethod>,
        profile: UserProfile,
    ): MeasurementDependencyState {
        val sex = profile.sex
        val requiredMeasurements = buildSet {
            if (AnalysisMethod.NavyBodyFat in enabledAnalysisMethods) {
                add(MeasurementType.NeckCircumference)
                add(MeasurementType.WaistCircumference)
                if (sex == Sex.Female) {
                    add(MeasurementType.HipCircumference)
                }
            }

            if (AnalysisMethod.Skinfold3SiteBodyFat in enabledAnalysisMethods) {
                add(MeasurementType.ThighSkinfold)
                if (sex == Sex.Male) {
                    add(MeasurementType.ChestSkinfold)
                    add(MeasurementType.AbdomenSkinfold)
                } else {
                    add(MeasurementType.TricepsSkinfold)
                    add(MeasurementType.SuprailiacSkinfold)
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
