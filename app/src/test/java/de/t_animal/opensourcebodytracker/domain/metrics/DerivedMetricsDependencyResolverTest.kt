package de.t_animal.opensourcebodytracker.domain.metrics

import de.t_animal.opensourcebodytracker.core.model.AnalysisMethod
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.SettingsState
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert
import org.junit.Test

class DerivedMetricsDependencyResolverTest {

    private val resolver = DerivedMetricsDependencyResolver()

    @Test
    fun resolve_noAnalysisMethods_returnsNoRequiredMeasurements() {
        val result = resolver.resolve(
            enabledAnalysisMethods = emptySet(),
            profile = profile(sex = Sex.Male),
        )

        Assert.assertEquals(emptySet<MeasuredBodyMetric>(), result.requiredMeasurements)
    }

    @Test
    fun resolve_navyForMale_requiresNeckAndWaist() {
        val result = resolver.resolve(
            enabledAnalysisMethods = setOf(AnalysisMethod.NavyBodyFat),
            profile = profile(sex = Sex.Male),
        )

        Assert.assertEquals(
            setOf(
                MeasuredBodyMetric.NeckCircumference,
                MeasuredBodyMetric.WaistCircumference,
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

        Assert.assertEquals(
            setOf(
                MeasuredBodyMetric.NeckCircumference,
                MeasuredBodyMetric.WaistCircumference,
                MeasuredBodyMetric.HipCircumference,
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

        Assert.assertEquals(
            setOf(
                MeasuredBodyMetric.ChestSkinfold,
                MeasuredBodyMetric.AbdomenSkinfold,
                MeasuredBodyMetric.ThighSkinfold,
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

        Assert.assertEquals(
            setOf(
                MeasuredBodyMetric.TricepsSkinfold,
                MeasuredBodyMetric.SuprailiacSkinfold,
                MeasuredBodyMetric.ThighSkinfold,
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

        Assert.assertEquals(setOf(MeasuredBodyMetric.Weight), result.requiredMeasurements)
    }

    @Test
    fun resolve_waistHipRatio_requiresWaistAndHip() {
        val result = resolver.resolve(
            enabledAnalysisMethods = setOf(AnalysisMethod.WaistHipRatio),
            profile = profile(sex = Sex.Male),
        )

        Assert.assertEquals(
            setOf(
                MeasuredBodyMetric.WaistCircumference,
                MeasuredBodyMetric.HipCircumference,
            ),
            result.requiredMeasurements,
        )
    }

    @Test
    fun resolve_waistHeightRatio_requiresWaistOnly() {
        val result = resolver.resolve(
            enabledAnalysisMethods = setOf(AnalysisMethod.WaistHeightRatio),
            profile = profile(sex = Sex.Female),
        )

        Assert.assertEquals(
            setOf(MeasuredBodyMetric.WaistCircumference),
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

        Assert.assertEquals(
            setOf(
                MeasuredBodyMetric.NeckCircumference,
                MeasuredBodyMetric.WaistCircumference,
                MeasuredBodyMetric.HipCircumference,
                MeasuredBodyMetric.TricepsSkinfold,
                MeasuredBodyMetric.SuprailiacSkinfold,
                MeasuredBodyMetric.ThighSkinfold,
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
            waistHipRatioEnabled = true,
            waistHeightRatioEnabled = false,
            onboardingCompleted = false,
            isDemoMode = false,
            reminderEnabled = false,
            reminderWeekdays = setOf(DayOfWeek.SUNDAY),
            reminderTime = LocalTime.of(9, 0),
            exportToDeviceStorageEnabled = false,
            exportFolderUri = null,
            enabledMeasurements = emptySet(),
            visibleInAnalysis = setOf(MeasuredBodyMetric.Weight),
            visibleInTable = setOf(MeasuredBodyMetric.Weight),
        )

        val methods = settings.enabledAnalysisMethods()

        Assert.assertEquals(
            setOf(
                AnalysisMethod.Bmi,
                AnalysisMethod.NavyBodyFat,
                AnalysisMethod.WaistHipRatio,
            ),
            methods,
        )
    }

    private fun profile(sex: Sex) = UserProfile(
        sex = sex,
        dateOfBirth = LocalDate.of(1990, 1, 1),
        heightCm = 180f,
    )
}
