package de.t_animal.opensourcebodytracker

import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.t_animal.opensourcebodytracker.core.notifications.ReminderNotificationContract
import de.t_animal.opensourcebodytracker.data.export.ExportPasswordRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import de.t_animal.opensourcebodytracker.core.notifications.ReminderAlarmScheduler
import de.t_animal.opensourcebodytracker.core.notifications.ReminderNotificationPoster
import de.t_animal.opensourcebodytracker.domain.demodata.GenerateDemoDataUseCase
import de.t_animal.opensourcebodytracker.domain.export.CreateLocalExportTestFileUseCase
import de.t_animal.opensourcebodytracker.domain.measurements.DeleteMeasurementUseCase
import de.t_animal.opensourcebodytracker.domain.measurements.SaveMeasurementUseCase
import de.t_animal.opensourcebodytracker.ui.navigation.BodyTrackerNavHost
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity() {
    private val openMeasurementAddSignal = MutableStateFlow(0L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = (application as BodyTrackerApplication).container
        handleNotificationIntent(intent)

        setContent {
            BodyTrackerTheme {
                Surface(modifier = Modifier) {
                    BodyTrackerApp(
                        profileRepository = container.profileRepository,
                        settingsRepository = container.settingsRepository,
                        exportPasswordRepository = container.exportPasswordRepository,
                        createLocalExportTestFileUseCase = container.createLocalExportTestFileUseCase,
                        measurementRepository = container.measurementRepository,
                        internalPhotoStorage = container.internalPhotoStorage,
                        calculateMeasurementDerivedMetrics = container.calculateMeasurementDerivedMetricsUseCase,
                        generateDemoDataUseCase = container.generateDemoDataUseCase,
                        deleteMeasurementUseCase = container.deleteMeasurementUseCase,
                        saveMeasurementUseCase = container.saveMeasurementUseCase,
                        reminderNotificationPoster = container.reminderNotificationPoster,
                        reminderAlarmScheduler = container.reminderAlarmScheduler,
                        openMeasurementAddSignal = openMeasurementAddSignal,
                        onResetApp = ::resetApp,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val shouldOpenAddMeasurement = intent?.action == ReminderNotificationContract.OpenAddMeasurementScreenAction ||
            intent?.getBooleanExtra(ReminderNotificationContract.OpenAddMeasurementScreen, false) == true

        if (!shouldOpenAddMeasurement) {
            return
        }

        intent?.removeExtra(ReminderNotificationContract.OpenAddMeasurementScreen)
        if (intent?.action == ReminderNotificationContract.OpenAddMeasurementScreenAction) {
            intent.action = null
        }

        openMeasurementAddSignal.value += 1L
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
    exportPasswordRepository: ExportPasswordRepository,
    createLocalExportTestFileUseCase: CreateLocalExportTestFileUseCase,
    measurementRepository: de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository,
    internalPhotoStorage: InternalPhotoStorage,
    calculateMeasurementDerivedMetrics: de.t_animal.opensourcebodytracker.domain.metrics.CalculateMeasurementDerivedMetricsUseCase,
    generateDemoDataUseCase: GenerateDemoDataUseCase,
    deleteMeasurementUseCase: DeleteMeasurementUseCase,
    saveMeasurementUseCase: SaveMeasurementUseCase,
    reminderNotificationPoster: ReminderNotificationPoster,
    reminderAlarmScheduler: ReminderAlarmScheduler,
    openMeasurementAddSignal: StateFlow<Long>,
    onResetApp: () -> Unit,
) {
    BodyTrackerNavHost(
        profileRepository = profileRepository,
        settingsRepository = settingsRepository,
        exportPasswordRepository = exportPasswordRepository,
        createLocalExportTestFileUseCase = createLocalExportTestFileUseCase,
        measurementRepository = measurementRepository,
        internalPhotoStorage = internalPhotoStorage,
        calculateMeasurementDerivedMetrics = calculateMeasurementDerivedMetrics,
        generateDemoDataUseCase = generateDemoDataUseCase,
        deleteMeasurementUseCase = deleteMeasurementUseCase,
        saveMeasurementUseCase = saveMeasurementUseCase,
        reminderNotificationPoster = reminderNotificationPoster,
        reminderAlarmScheduler = reminderAlarmScheduler,
        openMeasurementAddSignal = openMeasurementAddSignal,
        onResetApp = onResetApp,
    )
}
