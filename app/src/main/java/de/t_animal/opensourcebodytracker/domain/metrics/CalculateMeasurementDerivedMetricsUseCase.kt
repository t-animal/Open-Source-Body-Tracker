package de.t_animal.opensourcebodytracker.domain.metrics

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.DerivedMetrics
import de.t_animal.opensourcebodytracker.core.model.UserProfile

class CalculateMeasurementDerivedMetricsUseCase(
    private val calculator: DerivedMetricsCalculator,
) {
    operator fun invoke(
        profile: UserProfile?,
        measurement: BodyMeasurement,
    ): DerivedMetrics {
        if (profile == null) {
            return DerivedMetrics()
        }

        return calculator.calculate(profile = profile, measurement = measurement)
    }
}
