package de.t_animal.opensourcebodytracker.domain.settings

import de.t_animal.opensourcebodytracker.core.model.AnalysisMethod
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.SettingsState
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsDependencyResolverTest {

    private val resolver = SettingsDependencyResolver()

    @Test
    fun resolve_noAnalysisMethods_returnsNoRequiredMeasurements() {
        val result = resolver.resolve(
            enabledAnalysisMethods = emptySet(),
            profile = profile(sex = Sex.Male),
        )

        assertEquals(emptySet<BodyMetric>(), result.requiredMeasurements)
    }

    @Test
    fun resolve_navyForMale_requiresNeckAndWaist() {
        val result = resolver.resolve(
            enabledAnalysisMethods = setOf(AnalysisMethod.NavyBodyFat),
            profile = profile(sex = Sex.Male),
        )

        assertEquals(
            setOf(
                BodyMetric.NeckCircumference,
                BodyMetric.WaistCircumference,
            ),
            result.requiredMeasurements,
        )
    }

    @Test
    fun resolve_navyForFemale_requiresNeckWaistAndHip() {
        val result = resolver.resolve(
            enabledAnalysisMethods = setOf(AnalysisMethod.NavyBodyFat),
            profile = profile(sex = Sex.Female),
        )

        assertEquals(
            setOf(
                BodyMetric.NeckCircumference,
                BodyMetric.WaistCircumference,
                BodyMetric.HipCircumference,
            ),
            result.requiredMeasurements,
        )
    }

    @Test
    fun resolve_skinfoldForMale_requiresMaleThreeSiteSet() {
        val result = resolver.resolve(
            enabledAnalysisMethods = setOf(AnalysisMethod.Skinfold3SiteBodyFat),
            profile = profile(sex = Sex.Male),
        )

        assertEquals(
            setOf(
                BodyMetric.ChestSkinfold,
                BodyMetric.AbdomenSkinfold,
                BodyMetric.ThighSkinfold,
            ),
            result.requiredMeasurements,
        )
    }

    @Test
    fun resolve_skinfoldForFemale_requiresFemaleThreeSiteSet() {
        val result = resolver.resolve(
            enabledAnalysisMethods = setOf(AnalysisMethod.Skinfold3SiteBodyFat),
            profile = profile(sex = Sex.Female),
        )

        assertEquals(
            setOf(
                BodyMetric.TricepsSkinfold,
                BodyMetric.SuprailiacSkinfold,
                BodyMetric.ThighSkinfold,
            ),
            result.requiredMeasurements,
        )
    }

    @Test
    fun resolve_bmi_requiresWeight() {
        val result = resolver.resolve(
            enabledAnalysisMethods = setOf(AnalysisMethod.Bmi),
            profile = profile(sex = Sex.Male),
        )

        assertEquals(setOf(BodyMetric.Weight), result.requiredMeasurements)
    }

    @Test
    fun resolve_bothMethodsForFemale_returnsUnionWithoutDuplicates() {
        val result = resolver.resolve(
            enabledAnalysisMethods = setOf(
                AnalysisMethod.NavyBodyFat,
                AnalysisMethod.Skinfold3SiteBodyFat,
            ),
            profile = profile(sex = Sex.Female),
        )

        assertEquals(
            setOf(
                BodyMetric.NeckCircumference,
                BodyMetric.WaistCircumference,
                BodyMetric.HipCircumference,
                BodyMetric.TricepsSkinfold,
                BodyMetric.SuprailiacSkinfold,
                BodyMetric.ThighSkinfold,
            ),
            result.requiredMeasurements,
        )
    }

    @Test
    fun enabledAnalysisMethods_mapsFromSettingsFlags() {
        val settings = SettingsState(
            bmiEnabled = true,
            navyBodyFatEnabled = true,
            skinfoldBodyFatEnabled = false,
            enabledMeasurements = emptySet(),
            visibleInAnalysis = setOf(BodyMetric.Weight),
            visibleInTable = setOf(BodyMetric.Weight),
        )

        val methods = settings.enabledAnalysisMethods()

        assertEquals(
            setOf(
                AnalysisMethod.Bmi,
                AnalysisMethod.NavyBodyFat,
            ),
            methods,
        )
    }

    private fun profile(sex: Sex) = UserProfile(
        sex = sex,
        dateOfBirthEpochMillis = 0L,
        heightCm = 180f,
    )
}
