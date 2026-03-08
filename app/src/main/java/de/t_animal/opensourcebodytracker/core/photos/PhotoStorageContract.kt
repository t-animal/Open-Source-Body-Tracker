package de.t_animal.opensourcebodytracker.core.photos

object PhotoStorageContract {
    const val PERSISTED_PHOTOS_DIRECTORY = "measurement_photos"
    const val TEMP_CAPTURE_DIRECTORY = "images"
    const val TEMP_CAPTURE_FILE_PROVIDER_PATH = "images/"
    const val TEMP_CAPTURE_FILE_PREFIX = "capture_"
    const val PHOTO_FILE_EXTENSION = ".jpg"
    const val MEASUREMENT_FILE_PREFIX = "measurement_"
    const val FILE_PROVIDER_AUTHORITY_SUFFIX = ".fileprovider"
}

@JvmInline
value class PersistedPhotoPath(val value: String)

@JvmInline
value class TemporaryCapturePhotoPath(val value: String)

fun String?.toPersistedPhotoPathOrNull(): PersistedPhotoPath? {
    val normalized = this?.trim().orEmpty()
    if (normalized.isEmpty()) {
        return null
    }
    return PersistedPhotoPath(normalized)
}

fun String?.toTemporaryCapturePhotoPathOrNull(): TemporaryCapturePhotoPath? {
    val normalized = this?.trim().orEmpty()
    if (normalized.isEmpty()) {
        return null
    }
    return TemporaryCapturePhotoPath(normalized)
}