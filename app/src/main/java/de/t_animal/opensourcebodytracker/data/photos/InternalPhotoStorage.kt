package de.t_animal.opensourcebodytracker.data.photos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
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

    suspend fun savePhotoForMeasurement(
        measurementId: Long,
        measurementDateEpochMillis: Long,
        bitmap: Bitmap,
    ): String = withContext(Dispatchers.IO) {
        val path = pathForMeasurement(measurementId, measurementDateEpochMillis)
        val file = resolveFile(path)
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
            output.flush()
        }
        path
    }

    suspend fun loadPhoto(path: String): Bitmap? = withContext(Dispatchers.IO) {
        val file = resolveFile(path)
        if (!file.exists()) {
            return@withContext null
        }
        BitmapFactory.decodeFile(file.absolutePath)
    }

    suspend fun deletePhoto(path: String): Boolean = withContext(Dispatchers.IO) {
        val file = resolveFile(path)
        if (!file.exists()) {
            return@withContext true
        }
        file.delete()
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
        const val JPEG_QUALITY = 92
        val FILE_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}
