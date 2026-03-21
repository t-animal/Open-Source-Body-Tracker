package de.t_animal.opensourcebodytracker.domain.metrics

import de.t_animal.opensourcebodytracker.core.model.AnalysisMethod
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MeasurementSettings
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.MeasurementSettingsRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RequiredMeasurementsResolverTest {

    private val resolver = RequiredMeasurementsResolver(
        measurementSettingsRepository = stubMeasurementSettingsRepository(),
        profileRepository = stubProfileRepository(),
        dependencyResolver = DerivedMetricsDependencyResolver(),
    )

    @Test
    fun resolve_mergesRequiredMeasurementsIntoSettings() {
        val settings = MeasurementSettings(
            bmiEnabled = true,
            navyBodyFatEnabled = false,
            skinfoldBodyFatEnabled = false,
            waistHipRatioEnabled = false,
            waistHeightRatioEnabled = false,
            enabledMeasurements = setOf(MeasuredBodyMetric.HipCircumference),
        )

        val result = resolver.ensureRequired(settings, profile(Sex.Male))

        assertEquals(
            setOf(MeasuredBodyMetric.HipCircumference, MeasuredBodyMetric.Weight),
            result.settings.enabledMeasurements,
        )
    }

    @Test
    fun resolve_preservesAlreadyEnabledMeasurements() {
        val settings = MeasurementSettings(
            bmiEnabled = true,
            navyBodyFatEnabled = false,
            skinfoldBodyFatEnabled = false,
            waistHipRatioEnabled = false,
            waistHeightRatioEnabled = false,
            enabledMeasurements = setOf(
                MeasuredBodyMetric.Weight,
                MeasuredBodyMetric.HipCircumference,
            ),
        )

        val result = resolver.ensureRequired(settings, profile(Sex.Male))

        assertEquals(
            setOf(MeasuredBodyMetric.Weight, MeasuredBodyMetric.HipCircumference),
            result.settings.enabledMeasurements,
        )
    }

    @Test
    fun resolve_noAnalysisMethods_returnsSettingsUnchanged() {
        val settings = MeasurementSettings(
            bmiEnabled = false,
            navyBodyFatEnabled = false,
            skinfoldBodyFatEnabled = false,
            waistHipRatioEnabled = false,
            waistHeightRatioEnabled = false,
            enabledMeasurements = setOf(MeasuredBodyMetric.Weight),
        )

        val result = resolver.ensureRequired(settings, profile(Sex.Male))

        assertEquals(setOf(MeasuredBodyMetric.Weight), result.settings.enabledMeasurements)
        assertTrue(result.dependencies.requiredMeasurements.isEmpty())
    }

    @Test
    fun resolve_passesDependenciesThrough() {
        val settings = MeasurementSettings(
            bmiEnabled = true,
            navyBodyFatEnabled = false,
            skinfoldBodyFatEnabled = false,
            waistHipRatioEnabled = false,
            waistHeightRatioEnabled = false,
            enabledMeasurements = emptySet(),
        )

        val result = resolver.ensureRequired(settings, profile(Sex.Male))

        assertEquals(setOf(MeasuredBodyMetric.Weight), result.dependencies.requiredMeasurements)
        assertEquals(
            mapOf(MeasuredBodyMetric.Weight to setOf(AnalysisMethod.Bmi)),
            result.dependencies.measurementToAnalysisMethods,
        )
    }

    @Test
    fun resolve_preservesNonMeasurementSettingsFields() {
        val visibleInAnalysis = setOf(MeasuredBodyMetric.Weight)
        val visibleInTable = setOf(MeasuredBodyMetric.HipCircumference)
        val settings = MeasurementSettings(
            bmiEnabled = true,
            navyBodyFatEnabled = false,
            skinfoldBodyFatEnabled = false,
            waistHipRatioEnabled = false,
            waistHeightRatioEnabled = false,
            enabledMeasurements = emptySet(),
            visibleInAnalysis = visibleInAnalysis,
            visibleInTable = visibleInTable,
        )

        val result = resolver.ensureRequired(settings, profile(Sex.Male))

        assertEquals(visibleInAnalysis, result.settings.visibleInAnalysis)
        assertEquals(visibleInTable, result.settings.visibleInTable)
    }

    private fun profile(sex: Sex) = UserProfile(
        sex = sex,
        dateOfBirth = LocalDate.of(1990, 1, 1),
        heightCm = 180f,
    )

    private fun stubMeasurementSettingsRepository() = object : MeasurementSettingsRepository {
        override val settingsFlow: Flow<MeasurementSettings> = emptyFlow()
        override suspend fun saveSettings(settings: MeasurementSettings) = Unit
        override suspend fun updateSettings(transform: (MeasurementSettings) -> MeasurementSettings) = Unit
    }

    private fun stubProfileRepository() = object : ProfileRepository {
        override val profileFlow: Flow<UserProfile?> = emptyFlow()
        override suspend fun saveProfile(profile: UserProfile) = Unit
    }
}
