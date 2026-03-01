package de.t_animal.opensourcebodytracker.feature.measurements.helpers

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

data class PendingCaptureTarget(
    val uri: Uri,
    val file: File,
)

fun createTemporaryImageUri(context: Context): PendingCaptureTarget? {
    val cameraDir = File(context.cacheDir, "images").apply { mkdirs() }
    val imageFile = runCatching {
        File.createTempFile("capture_", ".jpg", cameraDir)
    }.getOrNull() ?: return null

    val imageUri = runCatching {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile,
        )
    }.getOrNull() ?: return null

    return PendingCaptureTarget(
        uri = imageUri,
        file = imageFile,
    )
}
