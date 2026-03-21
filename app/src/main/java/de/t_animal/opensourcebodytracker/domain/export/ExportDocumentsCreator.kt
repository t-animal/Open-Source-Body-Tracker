package de.t_animal.opensourcebodytracker.domain.export

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import de.t_animal.opensourcebodytracker.core.photos.PhotoStorageContract
import de.t_animal.opensourcebodytracker.data.export.ExportArchiveEntry
import de.t_animal.opensourcebodytracker.domain.backup.BackupMetadata
import de.t_animal.opensourcebodytracker.domain.backup.toBackupProfile
import kotlinx.serialization.json.Json
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.StringWriter
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ExportDocumentsCreator @Inject constructor() {

    private val json = Json { prettyPrint = true }

    fun create(
        measurements: List<BodyMeasurement>,
        profile: UserProfile,
        exportFileName: String,
        exportInstant: Instant,
        imageCount: Int,
        missingImageCount: Int,
    ): List<ExportArchiveEntry.InMemory> {
        return listOf(
            ExportArchiveEntry.InMemory(MEASUREMENTS_FILE_NAME, buildMeasurementsCsv(measurements)),
            ExportArchiveEntry.InMemory(PROFILE_FILE_NAME, buildProfileJson(profile)),
            ExportArchiveEntry.InMemory(
                METADATA_FILE_NAME,
                buildMetadataJson(
                    exportFileName = exportFileName,
                    exportInstant = exportInstant,
                    measurementCount = measurements.size,
                    imageCount = imageCount,
                    missingImageCount = missingImageCount,
                ),
            ),
        )
    }

    private fun buildMeasurementsCsv(measurements: List<BodyMeasurement>): ByteArray {
        val writer = StringWriter()
        val csvFormat = CSVFormat.DEFAULT.builder()
            .setHeader(*MEASUREMENT_CSV_HEADERS.toTypedArray())
            .build()
        CSVPrinter(writer, csvFormat).use { printer ->
            measurements.forEach { measurement ->
                printer.printRecord(
                    measurement.id,
                    measurement.dateEpochMillis,
                    measurement.photoFilePath?.let {
                        "${PhotoStorageContract.PERSISTED_PHOTOS_DIRECTORY}/${it.value}"
                    }.orEmpty(),
                    measurement.weightKg.orEmpty(),
                    measurement.bodyFatPercent.orEmpty(),
                    measurement.neckCircumferenceCm.orEmpty(),
                    measurement.chestCircumferenceCm.orEmpty(),
                    measurement.waistCircumferenceCm.orEmpty(),
                    measurement.abdomenCircumferenceCm.orEmpty(),
                    measurement.hipCircumferenceCm.orEmpty(),
                    measurement.chestSkinfoldMm.orEmpty(),
                    measurement.abdomenSkinfoldMm.orEmpty(),
                    measurement.thighSkinfoldMm.orEmpty(),
                    measurement.tricepsSkinfoldMm.orEmpty(),
                    measurement.suprailiacSkinfoldMm.orEmpty(),
                    measurement.note.orEmpty(),
                )
            }
        }
        return writer.toString().toByteArray(Charsets.UTF_8)
    }

    private fun buildProfileJson(profile: UserProfile): ByteArray {
        return json.encodeToString(profile.toBackupProfile()).toByteArray(Charsets.UTF_8)
    }

    private fun buildMetadataJson(
        exportFileName: String,
        exportInstant: Instant,
        measurementCount: Int,
        imageCount: Int,
        missingImageCount: Int,
    ): ByteArray {
        val metadata = BackupMetadata(
            schemaVersion = EXPORT_SCHEMA_VERSION,
            archiveFileName = exportFileName,
            exportedAtEpochMillis = exportInstant.toEpochMilli(),
            exportedAtUtc = DateTimeFormatter.ISO_INSTANT.format(exportInstant),
            measurementCount = measurementCount,
            imageCount = imageCount,
            missingImageCount = missingImageCount,
        )
        return json.encodeToString(metadata).toByteArray(Charsets.UTF_8)
    }

    private fun Double?.orEmpty(): String = this?.toString() ?: ""

    private companion object {
        const val EXPORT_SCHEMA_VERSION = 1
        const val MEASUREMENTS_FILE_NAME = "measurements.csv"
        const val PROFILE_FILE_NAME = "profile.json"
        const val METADATA_FILE_NAME = "metadata.json"

        val MEASUREMENT_CSV_HEADERS = listOf(
            "id",
            "dateEpochMillis",
            "photoFilePath",
            "weightKg",
            "bodyFatPercent",
            "neckCircumferenceCm",
            "chestCircumferenceCm",
            "waistCircumferenceCm",
            "abdomenCircumferenceCm",
            "hipCircumferenceCm",
            "chestSkinfoldMm",
            "abdomenSkinfoldMm",
            "thighSkinfoldMm",
            "tricepsSkinfoldMm",
            "suprailiacSkinfoldMm",
            "note",
        )
    }
}
