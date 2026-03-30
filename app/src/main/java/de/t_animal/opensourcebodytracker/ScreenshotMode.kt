package de.t_animal.opensourcebodytracker

import android.content.Intent
import javax.inject.Inject

interface ScreenshotModeOrchestrator {
    suspend fun tryInitialize(intent: Intent?): ScreenshotModeResult?
}

data class ScreenshotModeResult(
    val startDestination: String,
    val darkTheme: Boolean,
)

class NoOpScreenshotModeOrchestrator @Inject constructor() : ScreenshotModeOrchestrator {
    override suspend fun tryInitialize(intent: Intent?): ScreenshotModeResult? = null
}
