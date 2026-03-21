package de.t_animal.opensourcebodytracker.domain.importbackup

import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.photos.PhotoStorageContract
import de.t_animal.opensourcebodytracker.core.photos.toPersistedPhotoPathOrNull
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.StringReader
import javax.inject.Inject

class MeasurementCsvParser @Inject constructor() {

    fun parse(csvContent: String): List<BodyMeasurement> {
        val format = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .build()

        return CSVParser(StringReader(csvContent), format).use { parser ->
            parser.mapNotNull { record ->
                runCatching {
                    BodyMeasurement(
                        id = 0,
                        dateEpochMillis = record.get("dateEpochMillis").toLong(),
                        photoFilePath = record.get("photoFilePath")
                            .stripPhotoDirectoryPrefix()
                            .toPersistedPhotoPathOrNull(),
                        weightKg = record.get("weightKg").toDoubleOrNull(),
                        bodyFatPercent = record.get("bodyFatPercent").toDoubleOrNull(),
                        neckCircumferenceCm = record.get("neckCircumferenceCm").toDoubleOrNull(),
                        chestCircumferenceCm = record.get("chestCircumferenceCm").toDoubleOrNull(),
                        waistCircumferenceCm = record.get("waistCircumferenceCm").toDoubleOrNull(),
                        abdomenCircumferenceCm = record.get("abdomenCircumferenceCm").toDoubleOrNull(),
                        hipCircumferenceCm = record.get("hipCircumferenceCm").toDoubleOrNull(),
                        chestSkinfoldMm = record.get("chestSkinfoldMm").toDoubleOrNull(),
                        abdomenSkinfoldMm = record.get("abdomenSkinfoldMm").toDoubleOrNull(),
                        thighSkinfoldMm = record.get("thighSkinfoldMm").toDoubleOrNull(),
                        tricepsSkinfoldMm = record.get("tricepsSkinfoldMm").toDoubleOrNull(),
                        suprailiacSkinfoldMm = record.get("suprailiacSkinfoldMm").toDoubleOrNull(),
                        note = record.get("note").ifEmpty { null },
                    )
                }.getOrNull()
            }
        }
    }

    private fun String.stripPhotoDirectoryPrefix(): String {
        val prefix = "${PhotoStorageContract.PERSISTED_PHOTOS_DIRECTORY}/"
        return if (startsWith(prefix)) removePrefix(prefix) else this
    }
}
