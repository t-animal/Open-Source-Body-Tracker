package de.t_animal.opensourcebodytracker.screenshot

import android.content.Intent

data class ScreenshotCaptureRequest(
    val target: ScreenshotTarget,
    val theme: ScreenshotTheme,
) {
    companion object {
        const val ExtraEnabled = "de.t_animal.opensourcebodytracker.extra.SCREENSHOT_CAPTURE_ENABLED"
        const val ExtraTarget = "de.t_animal.opensourcebodytracker.extra.SCREENSHOT_CAPTURE_TARGET"
        const val ExtraTheme = "de.t_animal.opensourcebodytracker.extra.SCREENSHOT_CAPTURE_THEME"

        fun fromIntent(intent: Intent?): ScreenshotCaptureRequest? {
            if (intent?.getBooleanExtra(ExtraEnabled, false) != true) {
                return null
            }

            val target = intent.getStringExtra(ExtraTarget)
                ?.let(ScreenshotTarget::fromValue)
                ?: return null
            val theme = intent.getStringExtra(ExtraTheme)
                ?.let(ScreenshotTheme::fromValue)
                ?: return null

            return ScreenshotCaptureRequest(
                target = target,
                theme = theme,
            )
        }
    }
}

enum class ScreenshotTarget(
    val value: String,
    val fileNamePrefix: String,
) {
    MeasurementList(
        value = "measurement_list",
        fileNamePrefix = "measurement_list",
    ),
    MeasurementListAll(
        value = "measurement_list_all",
        fileNamePrefix = "measurement_list_all",
    ),
    Analysis(
        value = "analysis",
        fileNamePrefix = "analysis",
    ),
    Photo(
        value = "photo",
        fileNamePrefix = "photo",
    ),
    PhotoCompare(
        value = "photo_compare",
        fileNamePrefix = "photo_compare",
    ),
    ;

    companion object {
        fun fromValue(value: String): ScreenshotTarget? = entries.firstOrNull { it.value == value }
    }
}

enum class ScreenshotTheme(
    val value: String,
    val isDarkTheme: Boolean,
    val fileNameSuffix: String,
) {
    Light(
        value = "light",
        isDarkTheme = false,
        fileNameSuffix = "light",
    ),
    Dark(
        value = "dark",
        isDarkTheme = true,
        fileNameSuffix = "dark",
    ),
    ;

    companion object {
        fun fromValue(value: String): ScreenshotTheme? = entries.firstOrNull { it.value == value }
    }
}
