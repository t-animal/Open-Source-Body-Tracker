package de.t_animal.opensourcebodytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.t_animal.opensourcebodytracker.domain.measurements.GenerateFakeMeasurementsUseCase
import de.t_animal.opensourcebodytracker.ui.navigation.BodyTrackerNavHost
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = (application as BodyTrackerApplication).container

        setContent {
            BodyTrackerTheme {
                Surface(modifier = Modifier) {
                    BodyTrackerApp(
                        profileRepository = container.profileRepository,
                        settingsRepository = container.settingsRepository,
                        measurementRepository = container.measurementRepository,
                        calculateMeasurementDerivedMetrics = container.calculateMeasurementDerivedMetricsUseCase,
                        generateFakeMeasurementsUseCase = container.generateFakeMeasurementsUseCase,
                    )
                }
            }
        }
    }
}

@Composable
private fun BodyTrackerApp(
    profileRepository: de.t_animal.opensourcebodytracker.data.profile.ProfileRepository,
    settingsRepository: de.t_animal.opensourcebodytracker.data.settings.SettingsRepository,
    measurementRepository: de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository,
    calculateMeasurementDerivedMetrics: de.t_animal.opensourcebodytracker.domain.metrics.CalculateMeasurementDerivedMetricsUseCase,
    generateFakeMeasurementsUseCase: GenerateFakeMeasurementsUseCase,
) {
    BodyTrackerNavHost(
        profileRepository = profileRepository,
        settingsRepository = settingsRepository,
        measurementRepository = measurementRepository,
        calculateMeasurementDerivedMetrics = calculateMeasurementDerivedMetrics,
        generateFakeMeasurementsUseCase = generateFakeMeasurementsUseCase,
    )
}
