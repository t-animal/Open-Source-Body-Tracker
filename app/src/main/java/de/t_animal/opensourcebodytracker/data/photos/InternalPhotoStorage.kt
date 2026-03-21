package de.t_animal.opensourcebodytracker.data.photos

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import de.t_animal.opensourcebodytracker.core.photos.PersistedPhotoPath
import de.t_animal.opensourcebodytracker.core.photos.PhotoStorageContract
import de.t_animal.opensourcebodytracker.core.photos.TemporaryCapturePhotoPath
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.withContext

class InternalPhotoStorage @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val appContext = context.applicationContext
    private val photosDir: File = File(
        appContext.filesDir,
        PhotoStorageContract.PERSISTED_PHOTOS_DIRECTORY,
    ).apply {
        mkdirs()
    }
    private val captureCacheDir: File = File(
        appContext.cacheDir,
        PhotoStorageContract.TEMP_CAPTURE_DIRECTORY,
    ).apply {
        mkdirs()
    }

    fun createTemporaryNewPhotoCaptureTarget(): NewPhotoCaptureTarget? {
        val imageFile = runCatching {
            File.createTempFile(
                PhotoStorageContract.TEMP_CAPTURE_FILE_PREFIX,
                PhotoStorageContract.PHOTO_FILE_EXTENSION,
                captureCacheDir,
            )
        }.getOrNull() ?: return null

        val imageUri = runCatching {
            FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}${PhotoStorageContract.FILE_PROVIDER_AUTHORITY_SUFFIX}",
                imageFile,
            )
        }.getOrNull() ?: run {
            imageFile.delete()
            return null
        }

        return NewPhotoCaptureTarget(
            uri = imageUri,
            file = imageFile,
            absolutePath = TemporaryCapturePhotoPath(imageFile.absolutePath),
        )
    }

    suspend fun movePhotoForMeasurement(
        measurementId: Long,
        measurementDateEpochMillis: Long,
        sourceAbsolutePath: TemporaryCapturePhotoPath,
    ): PersistedPhotoPath = withContext(Dispatchers.IO) {
        val path = pathForMeasurement(measurementId, measurementDateEpochMillis)
        val sourceFile = File(sourceAbsolutePath.value)
        val targetFile = resolvePersistedPhotoFile(path)

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

    suspend fun writePhotoForMeasurement(
        measurementId: Long,
        measurementDateEpochMillis: Long,
        photoBinaryContent: ByteArray,
    ): PersistedPhotoPath = withContext(Dispatchers.IO) {
        val path = pathForMeasurement(measurementId, measurementDateEpochMillis)
        val targetFile = resolvePersistedPhotoFile(path)
        targetFile.writeBytes(photoBinaryContent)
        path
    }

    suspend fun deletePhoto(path: PersistedPhotoPath): Boolean = withContext(Dispatchers.IO) {
        val file = resolvePersistedPhotoFile(path)
        if (!file.exists()) {
            return@withContext true
        }
        file.delete()
    }

    suspend fun deleteTemporaryCapturePhoto(path: TemporaryCapturePhotoPath): Boolean =
        withContext(Dispatchers.IO) {
            val file = File(path.value)
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

    fun resolvePhotoFile(path: PersistedPhotoPath): File {
        return resolvePersistedPhotoFile(path)
    }

    fun resolveTemporaryCapturePhotoFile(path: TemporaryCapturePhotoPath): File {
        return File(path.value)
    }

    fun pathForMeasurement(measurementId: Long, measurementDateEpochMillis: Long): PersistedPhotoPath {
        val date = Instant.ofEpochMilli(measurementDateEpochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(FILE_DATE_FORMATTER)
        return PersistedPhotoPath(
            "${PhotoStorageContract.MEASUREMENT_FILE_PREFIX}${measurementId}_" +
                "$date${PhotoStorageContract.PHOTO_FILE_EXTENSION}",
        )
    }

    private fun resolvePersistedPhotoFile(path: PersistedPhotoPath): File {
        return File(photosDir, path.value)
    }

    private companion object {
        val FILE_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}

data class NewPhotoCaptureTarget(
    val uri: Uri,
    val file: File,
    val absolutePath: TemporaryCapturePhotoPath,
)
