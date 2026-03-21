package de.t_animal.opensourcebodytracker.domain.metrics

import de.t_animal.opensourcebodytracker.core.model.AnalysisMethod
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MeasurementSettings
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import java.time.LocalDate
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
        Assert.assertEquals(emptyMap<MeasuredBodyMetric, Set<AnalysisMethod>>(), result.measurementToAnalysisMethods)
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
        Assert.assertEquals(
            mapOf(
                MeasuredBodyMetric.Weight to setOf(AnalysisMethod.Bmi),
            ),
            result.measurementToAnalysisMethods,
        )
    }

    @Test
    fun resolve_waistRequiredByMultipleMethods_mapsAllMethods() {
        val result = resolver.resolve(
            enabledAnalysisMethods = setOf(
                AnalysisMethod.NavyBodyFat,
                AnalysisMethod.WaistHipRatio,
                AnalysisMethod.WaistHeightRatio,
            ),
            profile = profile(sex = Sex.Female),
        )

        Assert.assertEquals(
            setOf(
                AnalysisMethod.NavyBodyFat,
                AnalysisMethod.WaistHipRatio,
                AnalysisMethod.WaistHeightRatio,
            ),
            result.measurementToAnalysisMethods[MeasuredBodyMetric.WaistCircumference],
        )
    }

    @Test
    fun resolve_femaleHipRequiredByNavyAndWaistHipRatio_mapsBothMethods() {
        val result = resolver.resolve(
            enabledAnalysisMethods = setOf(
                AnalysisMethod.NavyBodyFat,
                AnalysisMethod.WaistHipRatio,
            ),
            profile = profile(sex = Sex.Female),
        )

        Assert.assertEquals(
            setOf(
                AnalysisMethod.NavyBodyFat,
                AnalysisMethod.WaistHipRatio,
            ),
            result.measurementToAnalysisMethods[MeasuredBodyMetric.HipCircumference],
        )
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
        val settings = MeasurementSettings(
            bmiEnabled = true,
            navyBodyFatEnabled = true,
            skinfoldBodyFatEnabled = false,
            waistHipRatioEnabled = true,
            waistHeightRatioEnabled = false,
            enabledMeasurements = emptySet(),
            visibleInAnalysis = setOf(MeasuredBodyMetric.Weight),
            visibleInTable = setOf(MeasuredBodyMetric.Weight),
        )

        val methods = settings.enabledAnalysisMethods

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
