package de.t_animal.opensourcebodytracker.domain.export

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import de.t_animal.opensourcebodytracker.data.export.ExportArchiveEntry
import java.time.Instant
import java.time.format.DateTimeFormatter

class ExportDocumentsCreator {
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
        val rows = buildList {
            add(MEASUREMENT_CSV_HEADERS.joinToString(separator = ","))
            measurements.forEach { measurement ->
                add(
                    listOf(
                        measurement.id.toString(),
                        measurement.dateEpochMillis.toString(),
                        measurement.photoFilePath?.value.orEmpty(),
                        measurement.weightKg.toCsvValue(),
                        measurement.bodyFatPercent.toCsvValue(),
                        measurement.neckCircumferenceCm.toCsvValue(),
                        measurement.chestCircumferenceCm.toCsvValue(),
                        measurement.waistCircumferenceCm.toCsvValue(),
                        measurement.abdomenCircumferenceCm.toCsvValue(),
                        measurement.hipCircumferenceCm.toCsvValue(),
                        measurement.chestSkinfoldMm.toCsvValue(),
                        measurement.abdomenSkinfoldMm.toCsvValue(),
                        measurement.thighSkinfoldMm.toCsvValue(),
                        measurement.tricepsSkinfoldMm.toCsvValue(),
                        measurement.suprailiacSkinfoldMm.toCsvValue(),
                    ).joinToString(separator = ",") { value -> value.toCsvCell() },
                )
            }
        }
        return rows.joinToString(separator = "\n", postfix = "\n").toByteArray(Charsets.UTF_8)
    }

    private fun buildProfileJson(profile: UserProfile): ByteArray {
        return buildString {
            appendLine("{")
            appendLine("  \"sex\": ${profile.sex.name.toJsonString()},")
            appendLine("  \"dateOfBirth\": ${profile.dateOfBirth.toString().toJsonString()},")
            appendLine("  \"heightCm\": ${profile.heightCm}")
            append('}')
        }.toByteArray(Charsets.UTF_8)
    }

    private fun buildMetadataJson(
        exportFileName: String,
        exportInstant: Instant,
        measurementCount: Int,
        imageCount: Int,
        missingImageCount: Int,
    ): ByteArray {
        return buildString {
            appendLine("{")
            appendLine("  \"schemaVersion\": $EXPORT_SCHEMA_VERSION,")
            appendLine("  \"archiveFileName\": ${exportFileName.toJsonString()},")
            appendLine("  \"exportedAtEpochMillis\": ${exportInstant.toEpochMilli()},")
            appendLine(
                "  \"exportedAtUtc\": ${DateTimeFormatter.ISO_INSTANT.format(exportInstant).toJsonString()},",
            )
            appendLine("  \"measurementCount\": $measurementCount,")
            appendLine("  \"imageCount\": $imageCount,")
            appendLine("  \"missingImageCount\": $missingImageCount")
            append('}')
        }.toByteArray(Charsets.UTF_8)
    }

    private fun Double?.toCsvValue(): String = this?.toString().orEmpty()

    private fun String.toCsvCell(): String {
        if (contains(',') || contains('"') || contains('\n')) {
            return '"' + replace("\"", "\"\"") + '"'
        }
        return this
    }

    private fun String.toJsonString(): String {
        return buildString {
            append('"')
            this@toJsonString.forEach { character ->
                when (character) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(character)
                }
            }
            append('"')
        }
    }

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
        )
    }
}
