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

class ImportBackupUseCase(
    private val measurementRepository: MeasurementRepository,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val photoStorage: InternalPhotoStorage,
    private val csvParser: MeasurementCsvParser = MeasurementCsvParser(),
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend operator fun invoke(
        context: Context,
        fileUri: Uri,
        password: String,
    ): ImportResult {
        val tempFile = copyToTempFile(context, fileUri)
            ?: return ImportResult.GeneralFailure("Could not read the selected file.")

        try {
            val zipFile = ZipFile(tempFile, password.toCharArray())

            if (!zipFile.isValidZipFile) {
                return ImportResult.UnsupportedFormat(
                    "The selected file is not a valid ZIP archive.",
                )
            }

            val metadataHeader = zipFile.getFileHeader(METADATA_FILE_NAME)
                ?: return ImportResult.IncompleteBackup(
                    "The archive does not contain metadata. It may not be a valid backup.",
                )

            val metadata = try {
                val metadataText = zipFile.getInputStream(metadataHeader)
                    .bufferedReader()
                    .use { it.readText() }
                json.decodeFromString<BackupMetadata>(metadataText)
            } catch (_: ZipException) {
                return ImportResult.WrongPassword("Wrong password. Please try again.")
            } catch (_: Exception) {
                return ImportResult.IncompleteBackup("Could not read backup metadata.")
            }

            if (metadata.schemaVersion != SUPPORTED_SCHEMA_VERSION) {
                return ImportResult.UnsupportedFormat(
                    "Unsupported backup version (${metadata.schemaVersion}). " +
                        "This app supports version $SUPPORTED_SCHEMA_VERSION.",
                )
            }

            val profileHeader = zipFile.getFileHeader(PROFILE_FILE_NAME)
                ?: return ImportResult.IncompleteBackup(
                    "The archive does not contain a profile.",
                )
            val profile = try {
                val profileText = zipFile.getInputStream(profileHeader)
                    .bufferedReader()
                    .use { it.readText() }
                json.decodeFromString<BackupProfile>(profileText).toUserProfile()
            } catch (_: Exception) {
                return ImportResult.IncompleteBackup("Could not read profile data.")
            }

            val measurementsHeader = zipFile.getFileHeader(MEASUREMENTS_FILE_NAME)
                ?: return ImportResult.IncompleteBackup(
                    "The archive does not contain measurements.",
                )
            val parsedMeasurements = try {
                val csvText = zipFile.getInputStream(measurementsHeader)
                    .bufferedReader()
                    .use { it.readText() }
                csvParser.parse(csvText)
            } catch (_: Exception) {
                return ImportResult.IncompleteBackup("Could not read measurement data.")
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

            val missingFromZipWarning = if (nulledPhotoCount > 0) {
                "$nulledPhotoCount photo(s) referenced in measurements are missing from " +
                    "the archive. Those measurements were imported without photos."
            } else {
                null
            }

            // --- Point of no return: write to database and filesystem ---

            try {
                profileRepository.saveProfile(profile)
                measurementRepository.replaceAll(measurements)
            } catch (e: Exception) {
                Log.e("ImportBackup", "Failed to save imported data to database", e)
                return ImportResult.CatastrophicFailure(
                    "Failed to save data to the database. The app may be in an inconsistent state.",
                )
            }

            try {
                extractPhotos(zipFile)
            } catch (e: Exception) {
                Log.e("ImportBackup", "Failed to extract photos", e)
                return ImportResult.CatastrophicFailure(
                    "Photos could not be restored after data was saved. " +
                        "The app may be in an inconsistent state.",
                )
            }

            val verificationFailure = verifyPhotosOnDisk(measurements)
            if (verificationFailure != null) {
                Log.e("ImportBackup", verificationFailure)
                return ImportResult.CatastrophicFailure(verificationFailure)
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
                return ImportResult.CatastrophicFailure(
                    "Data was imported but settings could not be saved. " +
                        "The app may be in an inconsistent state. Error: ${e.message}",
                )
            }

            return if (missingFromZipWarning != null) {
                ImportResult.SuccessWithWarning(missingFromZipWarning)
            } else {
                ImportResult.Success
            }
        } finally {
            tempFile.delete()
        }
    }

    private fun verifyPhotosOnDisk(measurements: List<BodyMeasurement>): String? {
        val missingCount = measurements.count { measurement ->
            val photoPath = measurement.photoFilePath ?: return@count false
            !photoStorage.resolvePhotoFile(photoPath).exists()
        }
        return if (missingCount > 0) {
            "$missingCount photo(s) referenced by measurements could not be found on disk."
        } else {
            null
        }
    }

    private fun extractPhotos(zipFile: ZipFile) {
        val photoPrefix = "${PhotoStorageContract.PERSISTED_PHOTOS_DIRECTORY}/"
        val photoHeaders = zipFile.fileHeaders.filter {
            it.fileName.startsWith(photoPrefix) && !it.isDirectory
        }

        for (header in photoHeaders) {
            val fileName = header.fileName.removePrefix(photoPrefix)
            if (fileName.isEmpty()) continue

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

sealed interface ImportResult {
    data object Success : ImportResult
    data class SuccessWithWarning(val message: String) : ImportResult
    data class WrongPassword(val message: String) : ImportResult
    data class UnsupportedFormat(val message: String) : ImportResult
    data class IncompleteBackup(val message: String) : ImportResult
    data class CatastrophicFailure(val message: String) : ImportResult
    data class GeneralFailure(val message: String) : ImportResult
}
