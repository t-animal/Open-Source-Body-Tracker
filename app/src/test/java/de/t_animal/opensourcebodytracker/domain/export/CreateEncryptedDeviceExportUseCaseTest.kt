package de.t_animal.opensourcebodytracker.domain.export

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import de.t_animal.opensourcebodytracker.core.photos.PersistedPhotoPath
import de.t_animal.opensourcebodytracker.data.export.ExportArchiveEntry
import de.t_animal.opensourcebodytracker.data.export.ExportArchiveWriter
import de.t_animal.opensourcebodytracker.data.export.ExportDocumentTreeStorage
import de.t_animal.opensourcebodytracker.data.export.ExportStorageError
import de.t_animal.opensourcebodytracker.data.export.ExportStorageResult
import de.t_animal.opensourcebodytracker.data.export.ExportTreeFile
import de.t_animal.opensourcebodytracker.data.export.Zip4jExportArchiveWriter
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.lingala.zip4j.ZipFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateEncryptedDeviceExportUseCaseTest {

    @Test
    fun invoke_returnsValidationError_whenPasswordMissing() = runTest {
        val storage = FakeExportDocumentTreeStorage()
        val useCase = createUseCase(
            exportStorage = storage,
        )

        val result = useCase(
            ExportExecutionCommand(
                exportToDeviceStorageEnabled = true,
                exportFolderUri = "content://example/tree/export",
                exportPassword = "",
            ),
        )

        assertEquals(0, storage.writeCalls)
        assertEquals(
            ExportActionResult.Failure(
                ExportActionError.Validation(ExportValidationError.EnterPassword),
            ),
            result,
        )
    }

    @Test
    fun invoke_writesEncryptedArchive_withCsvJsonImages_andKeepsTwoNewestExports() = runTest {
        val storage = FakeExportDocumentTreeStorage(
            initialFiles = listOf(
                exportTreeFile("bodytracker_export_2026-03-01_00-00-00_000.zip"),
                exportTreeFile("bodytracker_export_2026-03-02_00-00-00_000.zip"),
                exportTreeFile("bodytracker_export_2026-03-03_00-00-00_000.zip"),
            ),
        )
        val measurements = listOf(
            BodyMeasurement(
                id = 7,
                dateEpochMillis = 1_741_564_800_000,
                photoFilePath = PersistedPhotoPath("measurement_7_2025-03-09.jpg"),
                weightKg = 81.5,
                waistCircumferenceCm = 91.2,
            ),
            BodyMeasurement(
                id = 8,
                dateEpochMillis = 1_741_651_200_000,
                bodyFatPercent = 17.4,
            ),
        )
        val useCase = createUseCase(
            measurementRepository = FakeMeasurementRepository(measurements),
            exportStorage = storage,
            exportPhotoCollector = FakeExportPhotoCollector(
                contentByPath = mapOf(
                    "measurement_7_2025-03-09.jpg" to "jpeg-data".toByteArray(),
                ),
            ),
            clock = fixedClock(),
        )

        val result = useCase(validCommand())

        assertEquals(
            ExportActionResult.Success("bodytracker_export_2026-03-11_08-30-45.zip"),
            result,
        )
        assertEquals(1, storage.writeCalls)
        assertEquals("bodytracker_export_2026-03-11_08-30-45.zip", storage.lastWriteFileName)
        assertEquals("application/zip", storage.lastWriteMimeType)

        val entryNames = readZipEntryNames(storage.lastWriteContent, "super-secret")
        assertTrue(entryNames.contains("measurements.csv"))
        assertTrue(entryNames.contains("profile.json"))
        assertTrue(entryNames.contains("metadata.json"))
        assertTrue(entryNames.contains("images/measurement_7_2025-03-09.jpg"))

        val measurementsCsv = readZipEntryText(storage.lastWriteContent, "super-secret", "measurements.csv")
        assertTrue(measurementsCsv.contains("id,dateEpochMillis,photoFilePath"))
        assertTrue(measurementsCsv.contains("7,1741564800000,measurement_7_2025-03-09.jpg,81.5"))
        assertTrue(measurementsCsv.contains("8,1741651200000,,"))

        val profileJson = readZipEntryText(storage.lastWriteContent, "super-secret", "profile.json")
        assertTrue(profileJson.contains("\"sex\": \"Male\""))
        assertTrue(profileJson.contains("\"dateOfBirth\": \"1990-04-03\""))
        assertTrue(profileJson.contains("\"heightCm\": 180.0"))

        val metadataJson = readZipEntryText(storage.lastWriteContent, "super-secret", "metadata.json")
        assertTrue(metadataJson.contains("\"schemaVersion\": 1"))
        assertTrue(metadataJson.contains("\"measurementCount\": 2"))
        assertTrue(metadataJson.contains("\"imageCount\": 1"))
        assertTrue(metadataJson.contains("\"missingImageCount\": 0"))

        assertEquals(
            listOf(
                "bodytracker_export_2026-03-02_00-00-00_000.zip",
                "bodytracker_export_2026-03-01_00-00-00_000.zip",
            ),
            storage.deletedFileNames,
        )
    }

    @Test
    fun invoke_recordsMissingImagesInMetadata_whenPhotoFileIsAbsent() = runTest {
        val storage = FakeExportDocumentTreeStorage()
        val measurements = listOf(
            BodyMeasurement(
                id = 11,
                dateEpochMillis = 1_741_564_800_000,
                photoFilePath = PersistedPhotoPath("missing.jpg"),
            ),
        )
        val useCase = createUseCase(
            measurementRepository = FakeMeasurementRepository(measurements),
            exportStorage = storage,
            exportPhotoCollector = FakeExportPhotoCollector(),
            clock = fixedClock(),
        )

        val result = useCase(validCommand())

        assertEquals(
            ExportActionResult.Success("bodytracker_export_2026-03-11_08-30-45.zip"),
            result,
        )
        val entryNames = readZipEntryNames(storage.lastWriteContent, "super-secret")
        assertFalse(entryNames.contains("images/missing.jpg"))

        val metadataJson = readZipEntryText(storage.lastWriteContent, "super-secret", "metadata.json")
        assertTrue(metadataJson.contains("\"imageCount\": 0"))
        assertTrue(metadataJson.contains("\"missingImageCount\": 1"))
    }

    @Test
    fun invoke_returnsPermissionError_whenStorageWriteFails() = runTest {
        val storage = FakeExportDocumentTreeStorage(
            nextWriteResult = ExportStorageResult.Failure(ExportStorageError.PermissionDenied),
        )
        val useCase = createUseCase(
            exportStorage = storage,
        )

        val result = useCase(validCommand())

        assertEquals(
            ExportActionResult.Failure(ExportActionError.PermissionDenied),
            result,
        )
    }

    private fun createUseCase(
        measurementRepository: MeasurementRepository = FakeMeasurementRepository(emptyList()),
        profileRepository: ProfileRepository = FakeProfileRepository(
            UserProfile(
                sex = Sex.Male,
                dateOfBirth = LocalDate.parse("1990-04-03"),
                heightCm = 180f,
            ),
        ),
        exportStorage: ExportDocumentTreeStorage = FakeExportDocumentTreeStorage(),
        exportArchiveWriter: ExportArchiveWriter = Zip4jExportArchiveWriter(),
        exportDocumentsCreator: ExportDocumentsCreator = ExportDocumentsCreator(),
        exportPhotoCollector: ExportPhotoCollector = FakeExportPhotoCollector(),
        clock: Clock = fixedClock(),
    ): CreateEncryptedDeviceExportUseCase {
        return CreateEncryptedDeviceExportUseCase(
            measurementRepository = measurementRepository,
            profileRepository = profileRepository,
            exportStorage = exportStorage,
            exportArchiveWriter = exportArchiveWriter,
            exportDocumentsCreator = exportDocumentsCreator,
            exportPhotoCollector = exportPhotoCollector,
            clock = clock,
        )
    }

    private fun validCommand(): ExportExecutionCommand {
        return ExportExecutionCommand(
            exportToDeviceStorageEnabled = true,
            exportFolderUri = "content://example/tree/export",
            exportPassword = "super-secret",
        )
    }

    private fun fixedClock(): Clock {
        return Clock.fixed(Instant.parse("2026-03-11T08:30:45.123Z"), ZoneOffset.UTC)
    }

    private fun exportTreeFile(name: String): ExportTreeFile {
        return ExportTreeFile(
            name = name,
            documentUri = "content://example/document/$name",
            mimeType = "application/zip",
            lastModifiedEpochMillis = null,
        )
    }

    private fun readZipEntryNames(zipBytes: ByteArray, password: String): List<String> {
        return withZipFile(zipBytes, password) { zipFile ->
            assertTrue(zipFile.isEncrypted)
            zipFile.fileHeaders.map { it.fileName }
        }
    }

    private fun readZipEntryText(zipBytes: ByteArray, password: String, entryName: String): String {
        return withZipFile(zipBytes, password) { zipFile ->
            val fileHeader = zipFile.getFileHeader(entryName)
            assertNotNull(fileHeader)
            zipFile.getInputStream(fileHeader).bufferedReader().use { reader ->
                reader.readText()
            }
        }
    }

    private fun <T> withZipFile(zipBytes: ByteArray, password: String, block: (ZipFile) -> T): T {
        val tempFile = File.createTempFile("export-test", ".zip")
        return try {
            tempFile.writeBytes(zipBytes)
            block(ZipFile(tempFile, password.toCharArray()))
        } finally {
            tempFile.delete()
        }
    }
}

private class FakeMeasurementRepository(
    private val measurements: List<BodyMeasurement>,
) : MeasurementRepository {
    override fun observeAll(): Flow<List<BodyMeasurement>> = flowOf(measurements)

    override suspend fun getAll(): List<BodyMeasurement> = measurements

    override suspend fun getById(id: Long): BodyMeasurement? = measurements.firstOrNull { it.id == id }

    override suspend fun insert(measurement: BodyMeasurement): Long = measurement.id

    override suspend fun update(measurement: BodyMeasurement) = Unit

    override suspend fun deleteById(id: Long) = Unit

    override suspend fun replaceAll(measurements: List<BodyMeasurement>) = Unit
}

private class FakeProfileRepository(
    profile: UserProfile?,
) : ProfileRepository {
    override val profileFlow: Flow<UserProfile?> = flowOf(profile)

    override suspend fun saveProfile(profile: UserProfile) = Unit
}

private class FakeExportPhotoCollector(
    private val contentByPath: Map<String, ByteArray> = emptyMap(),
) : ExportPhotoCollector {
    override fun collect(measurements: List<BodyMeasurement>): CollectedExportPhotos {
        val entries = mutableListOf<ExportArchiveEntry.FileEntry>()
        var missingImageCount = 0

        measurements.forEach { measurement ->
            val photoPath = measurement.photoFilePath ?: return@forEach
            val content = contentByPath[photoPath.value]
            if (content == null) {
                missingImageCount += 1
                return@forEach
            }

            val tempFile = File.createTempFile("export-photo", ".bin").apply {
                writeBytes(content)
                deleteOnExit()
            }
            entries += ExportArchiveEntry.FileEntry(
                path = "images/${photoPath.value}",
                file = tempFile,
            )
        }

        return CollectedExportPhotos(
            entries = entries,
            exportedImageCount = entries.size,
            missingImageCount = missingImageCount,
        )
    }
}

private class FakeExportDocumentTreeStorage(
    initialFiles: List<ExportTreeFile> = emptyList(),
    private val nextWriteResult: ExportStorageResult<ExportTreeFile>? = null,
) : ExportDocumentTreeStorage {
    private val files = initialFiles.toMutableList()

    var writeCalls: Int = 0
    var lastWriteFileName: String? = null
    var lastWriteMimeType: String? = null
    var lastWriteContent: ByteArray = ByteArray(0)
    val deletedFileNames = mutableListOf<String>()

    override suspend fun writeOrReplaceFile(
        treeUri: String,
        fileName: String,
        mimeType: String,
        writeContent: (OutputStream) -> Unit,
    ): ExportStorageResult<ExportTreeFile> {
        writeCalls += 1
        lastWriteFileName = fileName
        lastWriteMimeType = mimeType

        nextWriteResult?.let { return it }

        lastWriteContent = ByteArrayOutputStream().use { outputStream ->
            writeContent(outputStream)
            outputStream.toByteArray()
        }

        val file = ExportTreeFile(
            name = fileName,
            documentUri = "content://example/document/$fileName",
            mimeType = mimeType,
            lastModifiedEpochMillis = null,
        )
        files.removeAll { existing -> existing.name == fileName }
        files += file
        return ExportStorageResult.Success(file)
    }

    override suspend fun listFiles(treeUri: String): ExportStorageResult<List<ExportTreeFile>> {
        return ExportStorageResult.Success(files.toList())
    }

    override suspend fun deleteFile(treeUri: String, fileName: String): ExportStorageResult<Unit> {
        deletedFileNames += fileName
        files.removeAll { file -> file.name == fileName }
        return ExportStorageResult.Success(Unit)
    }
}
