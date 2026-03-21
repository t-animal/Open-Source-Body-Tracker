package de.t_animal.opensourcebodytracker.domain.importbackup

import android.content.Context
import android.net.Uri
import android.util.Log

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.photos.PersistedPhotoPath
import de.t_animal.opensourcebodytracker.core.photos.PhotoStorageContract
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.backup.BackupMetadata
import de.t_animal.opensourcebodytracker.domain.backup.BackupProfile
import de.t_animal.opensourcebodytracker.domain.backup.toUserProfile
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import java.io.File
import javax.inject.Inject

class ImportBackupUseCase @Inject constructor(
    private val measurementRepository: MeasurementRepository,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val photoStorage: InternalPhotoStorage,
    private val csvParser: MeasurementCsvParser,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend operator fun invoke(
        context: Context,
        fileUri: Uri,
        password: String,
        onProgress: (ImportProgress) -> Unit = {},
    ): ImportResult {
        val tempFile = copyToTempFile(context, fileUri)
            ?: return ImportResult.FileNotReadable

        try {
            onProgress(ImportProgress.ValidatingArchive)
            val zipFile = ZipFile(tempFile, password.toCharArray())

            if (!zipFile.isValidZipFile) {
                return ImportResult.InvalidArchive
            }

            val metadataHeader = zipFile.getFileHeader(METADATA_FILE_NAME)
                ?: return ImportResult.IncompleteBackup

            val metadata = try {
                val metadataText = zipFile.getInputStream(metadataHeader)
                    .bufferedReader()
                    .use { it.readText() }
                json.decodeFromString<BackupMetadata>(metadataText)
            } catch (_: ZipException) {
                return ImportResult.WrongPassword
            } catch (_: Exception) {
                return ImportResult.IncompleteBackup
            }

            if (metadata.schemaVersion != SUPPORTED_SCHEMA_VERSION) {
                return ImportResult.UnsupportedVersion(
                    foundVersion = metadata.schemaVersion,
                    supportedVersion = SUPPORTED_SCHEMA_VERSION,
                )
            }

            onProgress(ImportProgress.ReadingProfile)
            val profileHeader = zipFile.getFileHeader(PROFILE_FILE_NAME)
                ?: return ImportResult.IncompleteBackup
            val profile = try {
                val profileText = zipFile.getInputStream(profileHeader)
                    .bufferedReader()
                    .use { it.readText() }
                json.decodeFromString<BackupProfile>(profileText).toUserProfile()
            } catch (_: Exception) {
                return ImportResult.IncompleteBackup
            }

            onProgress(ImportProgress.ReadingMeasurements)
            val measurementsHeader = zipFile.getFileHeader(MEASUREMENTS_FILE_NAME)
                ?: return ImportResult.IncompleteBackup
            val parsedMeasurements = try {
                val csvText = zipFile.getInputStream(measurementsHeader)
                    .bufferedReader()
                    .use { it.readText() }
                csvParser.parse(csvText)
            } catch (_: Exception) {
                return ImportResult.IncompleteBackup
            }

            // Pre-insert validation: null out photo paths missing from ZIP
            val photoPrefix = "${PhotoStorageContract.PERSISTED_PHOTOS_DIRECTORY}/"
            val zipPhotoEntries = zipFile.fileHeaders
                .filter { it.fileName.startsWith(photoPrefix) && !it.isDirectory }
                .map { it.fileName }
                .toSet()

            var nulledPhotoCount = 0
            val measurements = parsedMeasurements.map { measurement ->
                val photoPath = measurement.photoFilePath ?: return@map measurement
                val expectedEntry = "$photoPrefix${photoPath.value}"
                if (expectedEntry in zipPhotoEntries) {
                    measurement
                } else {
                    nulledPhotoCount++
                    measurement.copy(photoFilePath = null)
                }
            }

            // --- Point of no return: write to database and filesystem ---

            onProgress(ImportProgress.SavingToDatabase)
            try {
                profileRepository.saveProfile(profile)
                measurementRepository.replaceAll(measurements)
            } catch (e: Exception) {
                Log.e("ImportBackup", "Failed to save imported data to database", e)
                return ImportResult.CatastrophicFailure.DatabaseWriteFailed
            }

            try {
                extractPhotos(zipFile, onProgress)
            } catch (e: Exception) {
                Log.e("ImportBackup", "Failed to extract photos", e)
                return ImportResult.CatastrophicFailure.PhotoExtractionFailed
            }

            onProgress(ImportProgress.VerifyingPhotos)
            if (!verifyPhotosOnDisk(measurements)) {
                Log.e("ImportBackup", "Photo verification failed after extraction")
                return ImportResult.CatastrophicFailure.PhotoVerificationFailed
            }

            try {
                val currentSettings = settingsRepository.settingsFlow.first()
                settingsRepository.saveSettings(
                    currentSettings.copy(
                        onboardingCompleted = true,
                        isDemoMode = false,
                    ),
                )
            } catch (e: Exception) {
                Log.e("ImportBackup", "Failed to save imported settings", e)
                return ImportResult.CatastrophicFailure.SettingsWriteFailed
            }

            return if (nulledPhotoCount > 0) {
                ImportResult.SuccessWithWarning(droppedPhotoCount = nulledPhotoCount)
            } else {
                ImportResult.Success
            }
        } finally {
            tempFile.delete()
        }
    }

    private fun verifyPhotosOnDisk(measurements: List<BodyMeasurement>): Boolean {
        return measurements.none { measurement ->
            val photoPath = measurement.photoFilePath ?: return@none false
            !photoStorage.resolvePhotoFile(photoPath).exists()
        }
    }

    private fun extractPhotos(
        zipFile: ZipFile,
        onProgress: (ImportProgress) -> Unit,
    ) {
        val photoPrefix = "${PhotoStorageContract.PERSISTED_PHOTOS_DIRECTORY}/"
        val photoHeaders = zipFile.fileHeaders.filter {
            it.fileName.startsWith(photoPrefix) && !it.isDirectory
        }
        val total = photoHeaders.size

        if (total == 0) return

        photoHeaders.forEachIndexed { index, header ->
            onProgress(ImportProgress.ExtractingPhotos(current = index + 1, total = total))

            val fileName = header.fileName.removePrefix(photoPrefix)
            if (fileName.isEmpty()) return@forEachIndexed

            val targetFile = photoStorage.resolvePhotoFile(PersistedPhotoPath(fileName))
            zipFile.getInputStream(header).use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    private fun copyToTempFile(context: Context, uri: Uri): File? {
        return try {
            val tempFile = File.createTempFile("import_backup_", ".zip", context.cacheDir)
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: run {
                tempFile.delete()
                return null
            }
            tempFile
        } catch (_: Exception) {
            null
        }
    }

    private companion object {
        const val SUPPORTED_SCHEMA_VERSION = 1
        const val METADATA_FILE_NAME = "metadata.json"
        const val PROFILE_FILE_NAME = "profile.json"
        const val MEASUREMENTS_FILE_NAME = "measurements.csv"
    }
}

sealed interface ImportProgress {
    data object ValidatingArchive : ImportProgress
    data object ReadingProfile : ImportProgress
    data object ReadingMeasurements : ImportProgress
    data object SavingToDatabase : ImportProgress
    data class ExtractingPhotos(val current: Int, val total: Int) : ImportProgress
    data object VerifyingPhotos : ImportProgress
}

sealed interface ImportResult {
    data object Success : ImportResult
    data class SuccessWithWarning(val droppedPhotoCount: Int) : ImportResult
    data object WrongPassword : ImportResult
    data class UnsupportedVersion(val foundVersion: Int, val supportedVersion: Int) : ImportResult
    data object InvalidArchive : ImportResult
    data object IncompleteBackup : ImportResult
    data object FileNotReadable : ImportResult

    sealed interface CatastrophicFailure : ImportResult {
        data object DatabaseWriteFailed : CatastrophicFailure
        data object PhotoExtractionFailed : CatastrophicFailure
        data object PhotoVerificationFailed : CatastrophicFailure
        data object SettingsWriteFailed : CatastrophicFailure
    }
}
