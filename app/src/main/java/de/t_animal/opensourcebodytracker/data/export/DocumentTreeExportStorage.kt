package de.t_animal.opensourcebodytracker.data.export

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.net.toUri
import java.io.IOException
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
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
    suspend fun writeFile(
        treeUri: String,
        fileName: String,
        mimeType: String,
        writeContent: (OutputStream) -> Unit,
    ): ExportStorageResult<ExportTreeFile>

    suspend fun listFiles(treeUri: String): ExportStorageResult<List<ExportTreeFile>>

    suspend fun deleteFile(
        treeUri: String,
        fileName: String,
    ): ExportStorageResult<Unit>
}

class AndroidExportDocumentTreeStorage @Inject constructor(
    @ApplicationContext context: Context,
) : ExportDocumentTreeStorage {
    private val contentResolver: ContentResolver = context.applicationContext.contentResolver

    override suspend fun writeFile(
        treeUri: String,
        fileName: String,
        mimeType: String,
        writeContent: (OutputStream) -> Unit,
    ): ExportStorageResult<ExportTreeFile> = withContext(Dispatchers.IO) {
        val tree = resolveTree(treeUri) ?: return@withContext ExportStorageResult.Failure(
            ExportStorageError.InvalidTreeUri(treeUri),
        )

        val createdUri = runCatching {
            DocumentsContract.createDocument(contentResolver, tree.treeDocumentUri, mimeType, fileName)
        }.getOrElse { throwable ->
            return@withContext mapFailure(throwable, treeUri)
        } ?: return@withContext ExportStorageResult.Failure(
            ExportStorageError.IoFailure("Could not create file"),
        )

        runCatching {
            contentResolver.openOutputStream(createdUri, "w")?.use { outputStream ->
                writeContent(outputStream)
                outputStream.flush()
            } ?: throw IOException("Could not open output stream")
        }.getOrElse { throwable ->
            runCatching { DocumentsContract.deleteDocument(contentResolver, createdUri) }
            return@withContext mapFailure(throwable, treeUri)
        }

        return@withContext ExportStorageResult.Success(
            ExportTreeFile(
                name = fileName,
                documentUri = createdUri.toString(),
                mimeType = mimeType,
                lastModifiedEpochMillis = null,
            ),
        )
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
        val treeUri = runCatching { treeUriText.toUri() }.getOrNull() ?: return null
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
