package de.t_animal.opensourcebodytracker.domain.export

import de.t_animal.opensourcebodytracker.data.export.ExportArchiveEntry
import de.t_animal.opensourcebodytracker.data.export.ExportArchiveWriter
import de.t_animal.opensourcebodytracker.data.export.ExportDocumentTreeStorage
import de.t_animal.opensourcebodytracker.data.export.ExportStorageError
import de.t_animal.opensourcebodytracker.data.export.ExportStorageResult
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.first

open class ExportToFilesystemUseCase @Inject constructor(
    private val measurementRepository: MeasurementRepository,
    private val profileRepository: ProfileRepository,
    private val exportStorage: ExportDocumentTreeStorage,
    private val exportArchiveWriter: ExportArchiveWriter,
    private val exportDocumentsCreator: ExportDocumentsCreator,
    private val exportPhotoCollector: ExportPhotoCollector,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        command: ExportExecutionCommand,
        onProgress: ((ExportProgress) -> Unit)?,
    ): ExportActionResult {
        reportProgress(onProgress, ExportProgress.Validating)
        val validationResult = validateExportExecutionCommand(command)
        val validatedCommand = when (validationResult) {
            is ExportExecutionValidationResult.Valid -> validationResult.command
            is ExportExecutionValidationResult.Invalid -> {
                return ExportActionResult.Failure(
                    ExportActionError.Validation(validationResult.error),
                )
            }
        }

        reportProgress(onProgress, ExportProgress.LoadingProfile)
        val profile = profileRepository.profileFlow.first()
            ?: return ExportActionResult.Failure(ExportActionError.Unknown)

        reportProgress(onProgress, ExportProgress.LoadingMeasurements)
        val measurements = measurementRepository.getAll().sortedBy { it.dateEpochMillis }
        val exportInstant = clock.instant()
        val exportFileName = buildExportFileName(exportInstant, clock.zone)
        val collectedPhotos = exportPhotoCollector.collect(measurements) { progress ->
            reportProgress(
                onProgress,
                ExportProgress.CollectingPhotos(
                    processedMeasurementCount = progress.processedMeasurementCount,
                    totalMeasurementCount = progress.totalMeasurementCount,
                    exportedPhotoCount = progress.exportedPhotoCount,
                    missingPhotoCount = progress.missingPhotoCount,
                ),
            )
        }
        val documentEntries = exportDocumentsCreator.create(
            measurements = measurements,
            profile = profile,
            exportFileName = exportFileName,
            exportInstant = exportInstant,
            imageCount = collectedPhotos.exportedImageCount,
            missingImageCount = collectedPhotos.missingImageCount,
        )
        var currentDocumentIndex = 0
        var currentPhotoIndex = 0

        val writeResult = exportStorage.writeFile(
            treeUri = validatedCommand.exportFolderUri,
            fileName = exportFileName,
            mimeType = ZIP_MIME_TYPE,
        ) { outputStream ->
            runCatching {
                exportArchiveWriter.writeEncryptedZip(
                    entries = sequence {
                        yieldAll(documentEntries)
                        yieldAll(collectedPhotos.entries)
                    },
                    password = validatedCommand.exportPassword,
                    outputStream = outputStream,
                    onEntryStarted = { progress ->
                        when (val entry = progress.entry) {
                            is ExportArchiveEntry.InMemory -> {
                                currentDocumentIndex += 1
                                reportProgress(
                                    onProgress,
                                    ExportProgress.WritingArchiveData(
                                        currentDocumentIndex = currentDocumentIndex,
                                        totalDocumentCount = documentEntries.size,
                                        documentName = entry.path,
                                    ),
                                )
                            }

                            is ExportArchiveEntry.FileEntry -> {
                                currentPhotoIndex += 1
                                reportProgress(
                                    onProgress,
                                    ExportProgress.WritingPhoto(
                                        currentPhotoIndex = currentPhotoIndex,
                                        totalPhotoCount = collectedPhotos.exportedImageCount,
                                        photoName = entry.path.substringAfterLast('/'),
                                    ),
                                )
                            }
                        }
                    },
                )
            }.getOrElse { throwable ->
                throw IOException("Could not write encrypted archive", throwable)
            }
        }

        return when (writeResult) {
            is ExportStorageResult.Success -> {
                reportProgress(onProgress, ExportProgress.CleaningUpOldExports)
                cleanupOldExports(validatedCommand.exportFolderUri)
                ExportActionResult.Success(exportedFileName = exportFileName)
            }

            is ExportStorageResult.Failure -> ExportActionResult.Failure(writeResult.error.toActionError())
        }
    }

    private suspend fun cleanupOldExports(treeUri: String) {
        val listResult = exportStorage.listFiles(treeUri)
        if (listResult !is ExportStorageResult.Success) {
            return
        }

        listResult.value
            .filter { file ->
                file.name.startsWith(EXPORT_FILE_PREFIX) && file.name.endsWith(EXPORT_FILE_EXTENSION)
            }
            .sortedByDescending { file -> file.name }
            .drop(MAX_EXPORT_ARCHIVES)
            .forEach { file ->
                exportStorage.deleteFile(treeUri, file.name)
            }
    }

    private fun buildExportFileName(
        exportInstant: Instant,
        zoneId: ZoneId,
    ): String {
        val timestamp = FILE_NAME_TIMESTAMP_FORMATTER.format(exportInstant.atZone(zoneId))
        return "$EXPORT_FILE_PREFIX$timestamp$EXPORT_FILE_EXTENSION"
    }

    private fun ExportStorageError.toActionError(): ExportActionError = when (this) {
        is ExportStorageError.InvalidTreeUri -> ExportActionError.InvalidFolder
        is ExportStorageError.PermissionDenied -> ExportActionError.PermissionDenied
        is ExportStorageError.IoFailure -> ExportActionError.WriteFailed
        is ExportStorageError.FileNotFound -> ExportActionError.WriteFailed
        is ExportStorageError.Unknown -> ExportActionError.Unknown
    }

    private fun reportProgress(
        onProgress: ((ExportProgress) -> Unit)?,
        progress: ExportProgress,
    ) {
        onProgress?.invoke(progress)
    }

    private companion object {
        const val ZIP_MIME_TYPE = "application/zip"
        const val EXPORT_FILE_PREFIX = "bodytracker_export_"
        const val EXPORT_FILE_EXTENSION = ".zip"
        const val MAX_EXPORT_ARCHIVES = 2

        val FILE_NAME_TIMESTAMP_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
    }
}
