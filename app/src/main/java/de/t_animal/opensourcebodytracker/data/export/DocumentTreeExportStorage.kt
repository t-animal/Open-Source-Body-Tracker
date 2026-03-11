package de.t_animal.opensourcebodytracker.data.export

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import java.io.IOException
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ExportTreeFile(
    val name: String,
    val documentUri: String,
    val mimeType: String?,
    val lastModifiedEpochMillis: Long?,
)

sealed interface ExportStorageError {
    data class InvalidTreeUri(
        val treeUri: String,
    ) : ExportStorageError

    data object PermissionDenied : ExportStorageError

    data class IoFailure(
        val reason: String,
    ) : ExportStorageError

    data class FileNotFound(
        val fileName: String,
    ) : ExportStorageError

    data class Unknown(
        val reason: String,
    ) : ExportStorageError
}

sealed interface ExportStorageResult<out T> {
    data class Success<T>(
        val value: T,
    ) : ExportStorageResult<T>

    data class Failure(
        val error: ExportStorageError,
    ) : ExportStorageResult<Nothing>
}

interface ExportDocumentTreeStorage {
    suspend fun writeOrReplaceFile(
        treeUri: String,
        fileName: String,
        mimeType: String,
        writeContent: (OutputStream) -> Unit,
    ): ExportStorageResult<ExportTreeFile>

    suspend fun writeOrReplaceFile(
        treeUri: String,
        fileName: String,
        mimeType: String,
        content: ByteArray,
    ): ExportStorageResult<ExportTreeFile> {
        return writeOrReplaceFile(treeUri, fileName, mimeType) { outputStream ->
            outputStream.write(content)
            outputStream.flush()
        }
    }

    suspend fun listFiles(treeUri: String): ExportStorageResult<List<ExportTreeFile>>

    suspend fun deleteFile(
        treeUri: String,
        fileName: String,
    ): ExportStorageResult<Unit>
}

class AndroidExportDocumentTreeStorage(
    context: Context,
) : ExportDocumentTreeStorage {
    private val contentResolver: ContentResolver = context.applicationContext.contentResolver

    override suspend fun writeOrReplaceFile(
        treeUri: String,
        fileName: String,
        mimeType: String,
        writeContent: (OutputStream) -> Unit,
    ): ExportStorageResult<ExportTreeFile> = withContext(Dispatchers.IO) {
        val tree = resolveTree(treeUri) ?: return@withContext ExportStorageResult.Failure(
            ExportStorageError.InvalidTreeUri(treeUri),
        )

        val existing = when (val queryResult = queryDocuments(tree, treeUri)) {
            is ExportStorageResult.Success -> queryResult.value
            is ExportStorageResult.Failure -> return@withContext queryResult
        }
        val existingDocumentUri = existing.firstOrNull { it.name == fileName }?.let { existingDocument ->
            DocumentsContract.buildDocumentUriUsingTree(tree.treeUri, existingDocument.documentId)
        }

        val tempName = "$fileName.tmp.${System.currentTimeMillis()}"
        val tempUri = DocumentsContract.createDocument(contentResolver, tree.treeDocumentUri, mimeType, tempName)
            ?: return@withContext ExportStorageResult.Failure(
                ExportStorageError.IoFailure("Could not create temporary file"),
            )

        val writeTempResult = runCatching {
            contentResolver.openOutputStream(tempUri, "w")?.use { outputStream ->
                writeContent(outputStream)
                outputStream.flush()
            } ?: throw IOException("Could not open output stream")
        }.fold(
            onSuccess = { ExportStorageResult.Success(Unit) },
            onFailure = { throwable ->
                runCatching { DocumentsContract.deleteDocument(contentResolver, tempUri) }
                mapFailure(throwable, treeUri)
            },
        )
        if (writeTempResult is ExportStorageResult.Failure) return@withContext writeTempResult

        val directRename = renameDocument(treeUri, tempUri, fileName)
        if (directRename is ExportStorageResult.Success) {
            return@withContext ExportStorageResult.Success(
                buildExportTreeFile(fileName, mimeType, directRename.value),
            )
        }
        val directRenameFailure = directRename as ExportStorageResult.Failure

        // fallback only if needed for providers that don't support overwrite-by-rename
        if (existingDocumentUri != null && directRenameFailure.error.supportsReplaceFallback()) {
            val deleteExistingResult = deleteDocument(
                documentUri = existingDocumentUri,
                treeUri = treeUri,
                failureReason = "Could not replace existing file",
            )
            if (deleteExistingResult is ExportStorageResult.Failure) {
                deleteDocumentQuietly(tempUri)
                return@withContext deleteExistingResult
            }

            val renameAfterDelete = renameDocument(treeUri, tempUri, fileName)
            if (renameAfterDelete is ExportStorageResult.Success) {
                return@withContext ExportStorageResult.Success(
                    buildExportTreeFile(fileName, mimeType, renameAfterDelete.value),
                )
            }
            val renameAfterDeleteFailure = renameAfterDelete as ExportStorageResult.Failure
            // keep temp for recovery when fallback rename fails
            return@withContext renameAfterDeleteFailure
        }

        deleteDocumentQuietly(tempUri)
        return@withContext directRenameFailure
    }

    override suspend fun listFiles(treeUri: String): ExportStorageResult<List<ExportTreeFile>> =
        withContext(Dispatchers.IO) {
            val tree = resolveTree(treeUri) ?: return@withContext ExportStorageResult.Failure(
                ExportStorageError.InvalidTreeUri(treeUri),
            )

            val rows = when (val queryResult = queryDocuments(tree, treeUri)) {
                is ExportStorageResult.Success -> queryResult.value
                is ExportStorageResult.Failure -> return@withContext queryResult
            }

            return@withContext ExportStorageResult.Success(
                rows.map { row ->
                    val documentUri = DocumentsContract.buildDocumentUriUsingTree(
                        tree.treeUri,
                        row.documentId,
                    )
                    ExportTreeFile(
                        name = row.name,
                        documentUri = documentUri.toString(),
                        mimeType = row.mimeType,
                        lastModifiedEpochMillis = row.lastModifiedEpochMillis,
                    )
                },
            )
        }

    override suspend fun deleteFile(
        treeUri: String,
        fileName: String,
    ): ExportStorageResult<Unit> = withContext(Dispatchers.IO) {
        val tree = resolveTree(treeUri) ?: return@withContext ExportStorageResult.Failure(
            ExportStorageError.InvalidTreeUri(treeUri),
        )

        val rows = when (val queryResult = queryDocuments(tree, treeUri)) {
            is ExportStorageResult.Success -> queryResult.value
            is ExportStorageResult.Failure -> return@withContext queryResult
        }
        val target = rows.firstOrNull { it.name == fileName }
            ?: return@withContext ExportStorageResult.Failure(ExportStorageError.FileNotFound(fileName))

        val documentUri = DocumentsContract.buildDocumentUriUsingTree(tree.treeUri, target.documentId)
        return@withContext deleteDocument(
            documentUri = documentUri,
            treeUri = treeUri,
            failureReason = "Could not delete file",
        )
    }

    private fun buildExportTreeFile(
        fileName: String,
        mimeType: String,
        documentUri: Uri,
    ): ExportTreeFile {
        return ExportTreeFile(
            name = fileName,
            documentUri = documentUri.toString(),
            mimeType = mimeType,
            lastModifiedEpochMillis = null,
        )
    }

    private fun renameDocument(
        treeUri: String,
        documentUri: Uri,
        targetName: String,
    ): ExportStorageResult<Uri> {
        val renamedDocumentUri = runCatching {
            DocumentsContract.renameDocument(contentResolver, documentUri, targetName)
        }.getOrElse { throwable ->
            return mapFailure(throwable, treeUri)
        } ?: return ExportStorageResult.Failure(
            ExportStorageError.IoFailure("Could not rename temporary file"),
        )

        return ExportStorageResult.Success(renamedDocumentUri)
    }

    private fun deleteDocument(
        documentUri: Uri,
        treeUri: String,
        failureReason: String,
    ): ExportStorageResult<Unit> {
        val deleted = runCatching {
            DocumentsContract.deleteDocument(contentResolver, documentUri)
        }.getOrElse { throwable ->
            return mapFailure(throwable, treeUri)
        }

        if (!deleted) {
            return ExportStorageResult.Failure(
                ExportStorageError.IoFailure(failureReason),
            )
        }

        return ExportStorageResult.Success(Unit)
    }

    private fun deleteDocumentQuietly(documentUri: Uri) {
        runCatching { DocumentsContract.deleteDocument(contentResolver, documentUri) }
    }

    private fun ExportStorageError.supportsReplaceFallback(): Boolean = when (this) {
        is ExportStorageError.IoFailure -> true
        is ExportStorageError.Unknown -> {
            reason.contains("exist", ignoreCase = true) ||
                reason.contains("conflict", ignoreCase = true)
        }
        is ExportStorageError.InvalidTreeUri -> false
        is ExportStorageError.PermissionDenied -> false
        is ExportStorageError.FileNotFound -> false
    }

    private fun mapFailure(
        throwable: Throwable,
        treeUri: String,
    ): ExportStorageResult.Failure {
        val error = when (throwable) {
            is SecurityException -> ExportStorageError.PermissionDenied
            is IllegalArgumentException -> ExportStorageError.InvalidTreeUri(treeUri)
            is IOException -> ExportStorageError.IoFailure(throwable.message ?: "I/O failure")
            else -> ExportStorageError.Unknown(throwable.message ?: throwable::class.java.simpleName)
        }
        return ExportStorageResult.Failure(error)
    }

    private fun resolveTree(treeUriText: String): ResolvedTree? {
        val treeUri = runCatching { Uri.parse(treeUriText) }.getOrNull() ?: return null
        val treeDocumentId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull() ?: return null

        val treeDocumentUri = runCatching {
            DocumentsContract.buildDocumentUriUsingTree(treeUri, treeDocumentId)
        }.getOrNull() ?: return null

        val childrenUri = runCatching {
            DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeDocumentId)
        }.getOrNull() ?: return null

        return ResolvedTree(
            treeUri = treeUri,
            treeDocumentUri = treeDocumentUri,
            childrenUri = childrenUri,
        )
    }

    private fun queryDocuments(
        tree: ResolvedTree,
        treeUri: String,
    ): ExportStorageResult<List<DocumentRow>> {
        return runCatching {
            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            )
            contentResolver.query(tree.childrenUri, projection, null, null, null)?.use { cursor ->
                val documentIdIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val displayNameIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeTypeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val lastModifiedIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

                buildList {
                    while (cursor.moveToNext()) {
                        add(
                            DocumentRow(
                                documentId = cursor.getString(documentIdIndex),
                                name = cursor.getString(displayNameIndex),
                                mimeType = cursor.getString(mimeTypeIndex),
                                lastModifiedEpochMillis = if (cursor.isNull(lastModifiedIndex)) {
                                    null
                                } else {
                                    cursor.getLong(lastModifiedIndex)
                                },
                            ),
                        )
                    }
                }
            } ?: emptyList()
        }.fold(
            onSuccess = { ExportStorageResult.Success(it) },
            onFailure = { throwable -> mapFailure(throwable, treeUri) },
        )
    }

    private data class ResolvedTree(
        val treeUri: Uri,
        val treeDocumentUri: Uri,
        val childrenUri: Uri,
    )

    private data class DocumentRow(
        val documentId: String,
        val name: String,
        val mimeType: String?,
        val lastModifiedEpochMillis: Long?,
    )
}
