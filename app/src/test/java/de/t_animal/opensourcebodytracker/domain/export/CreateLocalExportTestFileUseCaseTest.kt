package de.t_animal.opensourcebodytracker.domain.export

import de.t_animal.opensourcebodytracker.data.export.ExportDocumentTreeStorage
import de.t_animal.opensourcebodytracker.data.export.ExportStorageError
import de.t_animal.opensourcebodytracker.data.export.ExportStorageResult
import de.t_animal.opensourcebodytracker.data.export.ExportTreeFile
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateLocalExportTestFileUseCaseTest {

    @Test
    fun invoke_returnsValidationError_whenPasswordMissing() = runTest {
        val storage = FakeExportDocumentTreeStorage()
        val useCase = CreateLocalExportTestFileUseCase(storage)

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
    fun invoke_writesTestFile_whenCommandIsValid() = runTest {
        val storage = FakeExportDocumentTreeStorage()
        val useCase = CreateLocalExportTestFileUseCase(storage)

        val result = useCase(
            ExportExecutionCommand(
                exportToDeviceStorageEnabled = true,
                exportFolderUri = "content://example/tree/export",
                exportPassword = "super-secret",
            ),
        )

        assertEquals(1, storage.writeCalls)
        assertEquals("content://example/tree/export", storage.lastWriteTreeUri)
        assertEquals("export_test.txt", storage.lastWriteFileName)
        assertEquals(ExportActionResult.Success("export_test.txt"), result)
        assertTrue(storage.lastWriteContentAsText.startsWith("OpenSourceBodyTracker export test file"))
    }

    @Test
    fun invoke_returnsUserError_whenStoragePermissionDenied() = runTest {
        val storage = FakeExportDocumentTreeStorage(
            nextWriteResult = ExportStorageResult.Failure(ExportStorageError.PermissionDenied),
        )
        val useCase = CreateLocalExportTestFileUseCase(storage)

        val result = useCase(
            ExportExecutionCommand(
                exportToDeviceStorageEnabled = true,
                exportFolderUri = "content://example/tree/export",
                exportPassword = "super-secret",
            ),
        )

        assertEquals(
            ExportActionResult.Failure(ExportActionError.PermissionDenied),
            result,
        )
    }
}

private class FakeExportDocumentTreeStorage(
    private val nextWriteResult: ExportStorageResult<ExportTreeFile> = ExportStorageResult.Success(
        ExportTreeFile(
            name = "export_test.txt",
            documentUri = "content://example/document/export_test.txt",
            mimeType = "text/plain",
            lastModifiedEpochMillis = null,
        ),
    ),
) : ExportDocumentTreeStorage {
    var writeCalls: Int = 0
    var lastWriteTreeUri: String? = null
    var lastWriteFileName: String? = null
    var lastWriteContentAsText: String = ""

    override suspend fun writeOrReplaceFile(
        treeUri: String,
        fileName: String,
        mimeType: String,
        content: ByteArray,
    ): ExportStorageResult<ExportTreeFile> {
        writeCalls += 1
        lastWriteTreeUri = treeUri
        lastWriteFileName = fileName
        lastWriteContentAsText = content.toString(Charsets.UTF_8)
        return nextWriteResult
    }

    override suspend fun listFiles(treeUri: String): ExportStorageResult<List<ExportTreeFile>> {
        return ExportStorageResult.Success(emptyList())
    }

    override suspend fun deleteFile(treeUri: String, fileName: String): ExportStorageResult<Unit> {
        return ExportStorageResult.Success(Unit)
    }
}
