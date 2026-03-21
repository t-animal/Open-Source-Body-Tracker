package de.t_animal.opensourcebodytracker.domain.backup

import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class BackupMetadata(
    val schemaVersion: Int,
    val archiveFileName: String,
    val exportedAtEpochMillis: Long,
    val exportedAtUtc: String,
    val measurementCount: Int,
    val imageCount: Int,
    val missingImageCount: Int,
)

@Serializable
data class BackupProfile(
    val sex: String,
    val dateOfBirth: String,
    val heightCm: Float,
)

fun UserProfile.toBackupProfile(): BackupProfile = BackupProfile(
    sex = sex.name,
    dateOfBirth = dateOfBirth.toString(),
    heightCm = heightCm,
)

fun BackupProfile.toUserProfile(): UserProfile = UserProfile(
    sex = Sex.valueOf(sex),
    dateOfBirth = LocalDate.parse(dateOfBirth),
    heightCm = heightCm,
)
