package de.t_animal.opensourcebodytracker.domain.metrics

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.DerivedMetricRatings
import de.t_animal.opensourcebodytracker.core.model.DerivedMetrics
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import javax.inject.Inject

data class DerivedMetricsAnalysis(
    val metrics: DerivedMetrics,
    val ratings: DerivedMetricRatings,
)

class CalculateAndRateDerivedMetricsUseCase @Inject constructor(
    private val calculator: DerivedMetricsCalculator,
    private val rater: DerivedMetricsRater,
) {
    operator fun invoke(
        profile: UserProfile?,
        measurement: BodyMeasurement,
    ): DerivedMetricsAnalysis {
        if (profile == null) {
            return DerivedMetricsAnalysis(
                metrics = DerivedMetrics(),
                ratings = DerivedMetricRatings(),
            )
        }

        val metrics = calculator.calculate(profile = profile, measurement = measurement)
        return DerivedMetricsAnalysis(
            metrics = metrics,
            ratings = rater.rate(sex = profile.sex, metrics = metrics),
        )
    }
}
