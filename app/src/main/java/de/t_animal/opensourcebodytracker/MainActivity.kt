package de.t_animal.opensourcebodytracker

import android.app.ActivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import de.t_animal.opensourcebodytracker.domain.demodata.GenerateDemoDataUseCase
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
                        internalPhotoStorage = container.internalPhotoStorage,
                        calculateMeasurementDerivedMetrics = container.calculateMeasurementDerivedMetricsUseCase,
                        generateDemoDataUseCase = container.generateDemoDataUseCase,
                        onResetApp = ::resetApp,
                    )
                }
            }
        }
    }

    private fun resetApp() {
        Toast.makeText(
            this,
            "Cleaning all app data. Please restart app manually.",
            Toast.LENGTH_LONG,
        ).show()

        val clearAccepted = runCatching {
            val activityManager = getSystemService(ActivityManager::class.java)
            activityManager?.clearApplicationUserData() ?: false
        }.getOrDefault(false)

        if (!clearAccepted) {
            Toast.makeText(
                this,
                "Could not reset app automatically. Please clear app storage in system settings.",
                Toast.LENGTH_LONG,
            ).show()
        }
    }
}

@Composable
private fun BodyTrackerApp(
    profileRepository: de.t_animal.opensourcebodytracker.data.profile.ProfileRepository,
    settingsRepository: de.t_animal.opensourcebodytracker.data.settings.SettingsRepository,
    measurementRepository: de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository,
    internalPhotoStorage: InternalPhotoStorage,
    calculateMeasurementDerivedMetrics: de.t_animal.opensourcebodytracker.domain.metrics.CalculateMeasurementDerivedMetricsUseCase,
    generateDemoDataUseCase: GenerateDemoDataUseCase,
    onResetApp: () -> Unit,
) {
    BodyTrackerNavHost(
        profileRepository = profileRepository,
        settingsRepository = settingsRepository,
        measurementRepository = measurementRepository,
        internalPhotoStorage = internalPhotoStorage,
        calculateMeasurementDerivedMetrics = calculateMeasurementDerivedMetrics,
        generateDemoDataUseCase = generateDemoDataUseCase,
        onResetApp = onResetApp,
    )
}
