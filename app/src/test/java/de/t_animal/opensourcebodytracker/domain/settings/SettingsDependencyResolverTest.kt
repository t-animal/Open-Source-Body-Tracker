package de.t_animal.opensourcebodytracker.domain.settings

import de.t_animal.opensourcebodytracker.core.model.AnalysisMethod
import de.t_animal.opensourcebodytracker.core.model.DisplayMetricType
import de.t_animal.opensourcebodytracker.core.model.MeasurementType
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

        assertEquals(emptySet<MeasurementType>(), result.requiredMeasurements)
    }

    @Test
    fun resolve_navyForMale_requiresNeckAndWaist() {
        val result = resolver.resolve(
            enabledAnalysisMethods = setOf(AnalysisMethod.NavyBodyFat),
            profile = profile(sex = Sex.Male),
        )

        assertEquals(
            setOf(
                MeasurementType.NeckCircumference,
                MeasurementType.WaistCircumference,
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
                MeasurementType.NeckCircumference,
                MeasurementType.WaistCircumference,
                MeasurementType.HipCircumference,
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
                MeasurementType.ChestSkinfold,
                MeasurementType.AbdomenSkinfold,
                MeasurementType.ThighSkinfold,
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
                MeasurementType.TricepsSkinfold,
                MeasurementType.SuprailiacSkinfold,
                MeasurementType.ThighSkinfold,
            ),
            result.requiredMeasurements,
        )
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
                MeasurementType.NeckCircumference,
                MeasurementType.WaistCircumference,
                MeasurementType.HipCircumference,
                MeasurementType.TricepsSkinfold,
                MeasurementType.SuprailiacSkinfold,
                MeasurementType.ThighSkinfold,
            ),
            result.requiredMeasurements,
        )
    }

    @Test
    fun enabledAnalysisMethods_mapsFromSettingsFlags() {
        val settings = SettingsState(
            navyBodyFatEnabled = true,
            skinfoldBodyFatEnabled = false,
            enabledMeasurements = emptySet(),
            visibleInAnalysis = setOf(DisplayMetricType.Weight),
            visibleInTable = setOf(DisplayMetricType.Weight),
        )

        val methods = settings.enabledAnalysisMethods()

        assertEquals(setOf(AnalysisMethod.NavyBodyFat), methods)
    }

    private fun profile(sex: Sex) = UserProfile(
        sex = sex,
        dateOfBirthEpochMillis = 0L,
        heightCm = 180f,
    )
}
