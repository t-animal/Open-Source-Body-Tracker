package de.t_animal.opensourcebodytracker.screenshot

import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import de.t_animal.opensourcebodytracker.ScreenshotModeOrchestrator
import de.t_animal.opensourcebodytracker.ScreenshotModeResult
import de.t_animal.opensourcebodytracker.data.settings.GeneralSettingsRepository
import de.t_animal.opensourcebodytracker.domain.demodata.StartDemoModeUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class ScreenshotModeOrchestratorImpl @Inject constructor(
    private val startDemoModeUseCase: StartDemoModeUseCase,
    private val generalSettingsRepository: GeneralSettingsRepository,
    private val screenshotStartDestinationResolver: ScreenshotStartDestinationResolver,
) : ScreenshotModeOrchestrator {

    override suspend fun tryInitialize(intent: Intent?): ScreenshotModeResult? {
        val request = ScreenshotCaptureRequest.fromIntent(intent) ?: return null

        Log.i(TAG, "Screenshot mode: target=${request.target}, theme=${request.theme}")
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))

        val alreadySeeded = generalSettingsRepository.settingsFlow.first().isDemoMode
        if (!alreadySeeded) {
            Log.i(TAG, "Starting demo mode...")
            startDemoModeUseCase()
            Log.i(TAG, "Demo mode started")
        } else {
            Log.i(TAG, "Demo data already seeded, skipping")
        }

        Log.i(TAG, "Resolving destination...")
        val destination = screenshotStartDestinationResolver.resolve(request.target)
        Log.i(TAG, "Destination resolved: $destination")

        return ScreenshotModeResult(
            startDestination = destination,
            darkTheme = request.theme.isDarkTheme,
        )
    }

    private companion object {
        const val TAG = "ScreenshotMode"
    }
}
