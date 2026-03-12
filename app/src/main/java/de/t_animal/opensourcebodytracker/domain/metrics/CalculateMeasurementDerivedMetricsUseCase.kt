package de.t_animal.opensourcebodytracker.domain.metrics

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.DerivedMetricRatings
import de.t_animal.opensourcebodytracker.core.model.DerivedMetrics
import de.t_animal.opensourcebodytracker.core.model.UserProfile

data class DerivedMeasurementAnalysis(
    val metrics: DerivedMetrics,
    val ratings: DerivedMetricRatings,
)

class CalculateMeasurementDerivedMetricsUseCase(
    private val calculator: DerivedMetricsCalculator,
    private val rater: DerivedMetricsRater,
) {
    operator fun invoke(
        profile: UserProfile?,
        measurement: BodyMeasurement,
    ): DerivedMeasurementAnalysis {
        if (profile == null) {
            return DerivedMeasurementAnalysis(
                metrics = DerivedMetrics(),
                ratings = DerivedMetricRatings(),
            )
        }

        val metrics = calculator.calculate(profile = profile, measurement = measurement)
        return DerivedMeasurementAnalysis(
            metrics = metrics,
            ratings = rater.rate(sex = profile.sex, metrics = metrics),
        )
    }
}
