package de.t_animal.opensourcebodytracker

import android.content.Intent
import android.os.SystemClock
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import de.t_animal.opensourcebodytracker.screenshot.ScreenshotCaptureRequest
import de.t_animal.opensourcebodytracker.screenshot.ScreenshotTarget
import de.t_animal.opensourcebodytracker.screenshot.ScreenshotTheme
import java.io.File
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReleaseScreenshotInstrumentedTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val targetContext = instrumentation.targetContext
    private val device = UiDevice.getInstance(instrumentation)

    private val outputDirectory: File by lazy {
        // externalMediaDirs maps to /sdcard/Android/media/<package>/ which adb pull can read
        // on real Android 11+ devices (unlike /sdcard/Android/data/ which is scoped storage).
        val dir = targetContext.externalMediaDirs.firstOrNull()
            ?: targetContext.getExternalFilesDir("test-screenshots")
        checkNotNull(dir) {
            "No writable external output directory - package: ${targetContext.packageName}, " +
            "storage state: ${android.os.Environment.getExternalStorageState()}"
        }
        dir.resolve("test-screenshots")
    }

    @Before
    fun prepareDevice() {
        android.util.Log.i("ReleaseScreenshotTest", "Preparing device...")

        outputDirectory.deleteRecursively()
        outputDirectory.mkdirs()
        check(outputDirectory.exists()) {
            "Failed to create output directory: $outputDirectory"
        }
        android.util.Log.i("ReleaseScreenshotTest", "Output directory ready: $outputDirectory")

        device.pressHome()
        device.unfreezeRotation()
        device.setOrientationNatural()
        android.util.Log.i("ReleaseScreenshotTest", "Device prepared")
    }

    @After
    fun cleanupDevice() {
        device.unfreezeRotation()
    }

    @Test
    fun captureReleaseScreenshots() {
        val results = ScreenshotTarget.entries.flatMap { target ->
            ScreenshotTheme.entries.map { theme ->
                captureScreenshot(
                    target = target,
                    theme = theme,
                )
            }
        }

        writeSummary(results)

        val failures = results.filter { !it.success }
        if (failures.isNotEmpty()) {
            val messages = failures.joinToString("\n") { "  ${it.fileName}: ${it.message}" }
            fail("${failures.size} screenshot(s) failed:\n$messages")
        }
    }

    private fun captureScreenshot(
        target: ScreenshotTarget,
        theme: ScreenshotTheme,
    ): ScreenshotCaptureResult {
        val fileName = "${target.fileNamePrefix}_${theme.fileNameSuffix}.png"

        return runCatching {
            launchScreenshotActivity(target = target, theme = theme).use {
                composeRule.waitForIdle()
                SystemClock.sleep(SETTLE_DELAY_MS)

                val screenshotFile = outputDirectory.resolve(fileName)
                check(device.takeScreenshot(screenshotFile)) {
                    "UiDevice failed to write $fileName"
                }
                check(screenshotFile.exists()) {
                    "Screenshot file missing after takeScreenshot: $screenshotFile"
                }
                android.util.Log.i("ReleaseScreenshotTest", "Captured: $fileName")
            }

            ScreenshotCaptureResult(
                fileName = fileName,
                success = true,
            )
        }.getOrElse { error ->
            android.util.Log.e("ReleaseScreenshotTest", "Failed to capture $fileName", error)
            ScreenshotCaptureResult(
                fileName = fileName,
                success = false,
                message = error.message ?: error::class.java.simpleName,
            )
        }
    }

    private fun launchScreenshotActivity(
        target: ScreenshotTarget,
        theme: ScreenshotTheme,
    ): androidx.test.core.app.ActivityScenario<MainActivity> {
        val launchIntent = Intent(targetContext, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra(ScreenshotCaptureRequest.ExtraEnabled, true)
            putExtra(ScreenshotCaptureRequest.ExtraTarget, target.value)
            putExtra(ScreenshotCaptureRequest.ExtraTheme, theme.value)
        }

        return androidx.test.core.app.ActivityScenario.launch(launchIntent)
    }

    private fun writeSummary(results: List<ScreenshotCaptureResult>) {
        val summaryText = buildString {
            appendLine("Release screenshot capture summary")
            results.forEach { result ->
                val status = if (result.success) "SUCCESS" else "FAILED"
                append(status)
                append(" - ")
                append(result.fileName)
                result.message?.let { message ->
                    append(": ")
                    append(message)
                }
                appendLine()
            }
        }

        outputDirectory.resolve("summary.txt").writeText(summaryText)
        println(summaryText)
    }

    private companion object {
        const val SETTLE_DELAY_MS = 3_000L
    }
}

private data class ScreenshotCaptureResult(
    val fileName: String,
    val success: Boolean,
    val message: String? = null,
)
