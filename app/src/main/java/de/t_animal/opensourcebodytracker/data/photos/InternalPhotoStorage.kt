package de.t_animal.opensourcebodytracker.data.photos

import android.content.Context
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InternalPhotoStorage(
    context: Context,
) {
    private val photosDir: File = File(context.filesDir, PHOTOS_DIRECTORY).apply {
        mkdirs()
    }
    private val captureCacheDir: File = File(context.cacheDir, TEMP_CAPTURE_DIRECTORY).apply {
        mkdirs()
    }

    suspend fun movePhotoForMeasurement(
        measurementId: Long,
        measurementDateEpochMillis: Long,
        sourceAbsolutePath: String,
    ): String = withContext(Dispatchers.IO) {
        val path = pathForMeasurement(measurementId, measurementDateEpochMillis)
        val sourceFile = File(sourceAbsolutePath)
        val targetFile = resolveFile(path)

        if (sourceFile.absolutePath == targetFile.absolutePath) {
            return@withContext path
        }

        val moved = sourceFile.renameTo(targetFile)
        if (!moved) {
            sourceFile.copyTo(targetFile, overwrite = true)
            sourceFile.delete()
        }

        path
    }

    suspend fun deletePhoto(path: String): Boolean = withContext(Dispatchers.IO) {
        val file = resolveFile(path)
        if (!file.exists()) {
            return@withContext true
        }
        file.delete()
    }

    suspend fun deletePhotoAtAbsolutePath(absolutePath: String): Boolean = withContext(Dispatchers.IO) {
        val file = File(absolutePath)
        if (!file.exists()) {
            return@withContext true
        }
        file.delete()
    }

    suspend fun clearTemporaryCapturePhotos(): Unit = withContext(Dispatchers.IO) {
        val files = captureCacheDir.listFiles().orEmpty()
        files.forEach { file ->
            if (file.isFile) {
                file.delete()
            }
        }
    }

    fun resolvePhotoFile(path: String): File {
        return resolveFile(path)
    }

    fun pathForMeasurement(measurementId: Long, measurementDateEpochMillis: Long): String {
        val date = Instant.ofEpochMilli(measurementDateEpochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(FILE_DATE_FORMATTER)
        return "measurement_${measurementId}_${date}.jpg"
    }

    private fun resolveFile(path: String): File {
        return File(photosDir, path)
    }

    private companion object {
        const val PHOTOS_DIRECTORY = "measurement_photos"
        const val TEMP_CAPTURE_DIRECTORY = "images"
        val FILE_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}
