package de.t_animal.opensourcebodytracker.data.export

import java.io.File
import java.io.OutputStream
import net.lingala.zip4j.io.outputstream.ZipOutputStream
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod

sealed interface ExportArchiveEntry {
    val path: String

    data class InMemory(
        override val path: String,
        val content: ByteArray,
    ) : ExportArchiveEntry

    data class FileEntry(
        override val path: String,
        val file: File,
    ) : ExportArchiveEntry
}

interface ExportArchiveWriter {
    fun writeEncryptedZip(
        entries: Sequence<ExportArchiveEntry>,
        password: String,
        outputStream: OutputStream,
    )
}

class Zip4jExportArchiveWriter : ExportArchiveWriter {
    override fun writeEncryptedZip(
        entries: Sequence<ExportArchiveEntry>,
        password: String,
        outputStream: OutputStream,
    ) {
        ZipOutputStream(outputStream, password.toCharArray()).use { zipOutputStream ->
            entries.forEach { entry ->
                zipOutputStream.putNextEntry(buildZipParameters(entry.path))
                when (entry) {
                    is ExportArchiveEntry.InMemory -> zipOutputStream.write(entry.content)
                    is ExportArchiveEntry.FileEntry -> {
                        entry.file.inputStream().buffered().use { inputStream ->
                            inputStream.copyTo(zipOutputStream)
                        }
                    }
                }
                zipOutputStream.closeEntry()
            }
        }
    }

    private fun buildZipParameters(path: String): ZipParameters {
        return ZipParameters().apply {
            fileNameInZip = path
            compressionMethod = CompressionMethod.DEFLATE
            isEncryptFiles = true
            encryptionMethod = EncryptionMethod.AES
            aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256
        }
    }
}