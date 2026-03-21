package de.t_animal.opensourcebodytracker

import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import de.t_animal.opensourcebodytracker.core.notifications.ReminderNotificationContract
import de.t_animal.opensourcebodytracker.core.notifications.ReminderNotificationPoster
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.export.AutomaticExportScheduler
import de.t_animal.opensourcebodytracker.ui.navigation.BodyTrackerNavHost
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val openMeasurementAddSignal = MutableStateFlow(0L)

    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var automaticExportScheduler: AutomaticExportScheduler
    @Inject lateinit var reminderNotificationPoster: ReminderNotificationPoster

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleNotificationIntent(intent)

        setContent {
            BodyTrackerTheme {
                Surface(modifier = Modifier) {
                    BodyTrackerNavHost(
                        settingsRepository = settingsRepository,
                        automaticExportScheduler = automaticExportScheduler,
                        reminderNotificationPoster = reminderNotificationPoster,
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
