package de.t_animal.opensourcebodytracker

import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import de.t_animal.opensourcebodytracker.infra.notifications.ReminderNotificationContract
import de.t_animal.opensourcebodytracker.infra.notifications.ReminderNotificationPoster
import de.t_animal.opensourcebodytracker.data.settings.GeneralSettingsRepository
import de.t_animal.opensourcebodytracker.domain.export.AutomaticExportScheduler
import de.t_animal.opensourcebodytracker.ui.navigation.BodyTrackerNavHost
import de.t_animal.opensourcebodytracker.ui.navigation.Routes
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val openMeasurementAddSignal = MutableStateFlow(0L)
    private var startDestination by mutableStateOf<String?>(null)

    @Inject lateinit var generalSettingsRepository: GeneralSettingsRepository
    @Inject lateinit var automaticExportScheduler: AutomaticExportScheduler
    @Inject lateinit var reminderNotificationPoster: ReminderNotificationPoster

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { startDestination == null }

        lifecycleScope.launch {
            val settings = generalSettingsRepository.settingsFlow.first()
            startDestination = if (settings.onboardingCompleted) {
                Routes.MeasurementList
            } else {
                Routes.OnboardingGraph
            }
        }

        handleNotificationIntent(intent)

        setContent {
            BodyTrackerTheme {
                val destination = startDestination
                if (destination != null) {
                    Surface(modifier = Modifier) {
                        BodyTrackerNavHost(
                            startDestination = destination,
                            generalSettingsRepository = generalSettingsRepository,
                            automaticExportScheduler = automaticExportScheduler,
                            reminderNotificationPoster = reminderNotificationPoster,
                            openMeasurementAddSignal = openMeasurementAddSignal,
                            onResetApp = ::resetApp,
                        )
                    }
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
            getString(R.string.app_reset_cleaning),
            Toast.LENGTH_LONG,
        ).show()

        val clearAccepted = runCatching {
            val activityManager = getSystemService(ActivityManager::class.java)
            activityManager?.clearApplicationUserData() ?: false
        }.getOrDefault(false)

        if (!clearAccepted) {
            Toast.makeText(
                this,
                getString(R.string.app_reset_failed),
                Toast.LENGTH_LONG,
            ).show()
        }
    }
}
