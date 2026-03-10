package de.t_animal.opensourcebodytracker.domain.export

import de.t_animal.opensourcebodytracker.data.export.ExportDocumentTreeStorage
import de.t_animal.opensourcebodytracker.data.export.ExportStorageError
import de.t_animal.opensourcebodytracker.data.export.ExportStorageResult

class CreateLocalExportTestFileUseCase(
    private val exportStorage: ExportDocumentTreeStorage,
) {
    suspend operator fun invoke(command: ExportExecutionCommand): ExportActionResult {
        val validationResult = validateExportExecutionCommand(command)
        val validatedCommand = when (validationResult) {
            is ExportExecutionValidationResult.Valid -> validationResult.command
            is ExportExecutionValidationResult.Invalid -> {
                return ExportActionResult.Failure(
                    ExportActionError.Validation(validationResult.error),
                )
            }
        }

        val writeResult = exportStorage.writeOrReplaceFile(
            treeUri = validatedCommand.exportFolderUri,
            fileName = EXPORT_TEST_FILE_NAME,
            mimeType = MIME_TYPE_TEXT,
            content = EXPORT_TEST_FILE_CONTENT.toByteArray(Charsets.UTF_8),
        )

        return when (writeResult) {
            is ExportStorageResult.Success -> ExportActionResult.Success(
                exportedFileName = EXPORT_TEST_FILE_NAME,
            )

            is ExportStorageResult.Failure -> ExportActionResult.Failure(
                error = writeResult.error.toActionError(),
            )
        }
    }

    private fun ExportStorageError.toActionError(): ExportActionError = when (this) {
        is ExportStorageError.InvalidTreeUri -> ExportActionError.InvalidFolder
        is ExportStorageError.PermissionDenied -> ExportActionError.PermissionDenied
        is ExportStorageError.IoFailure -> ExportActionError.WriteFailed
        is ExportStorageError.FileNotFound -> ExportActionError.WriteFailed
        is ExportStorageError.Unknown -> ExportActionError.Unknown
    }

    private companion object {
        const val EXPORT_TEST_FILE_NAME = "export_test.txt"
        const val MIME_TYPE_TEXT = "text/plain"
        const val EXPORT_TEST_FILE_CONTENT = "OpenSourceBodyTracker export test file\n"
    }
}
