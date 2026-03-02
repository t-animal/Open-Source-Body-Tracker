package de.t_animal.opensourcebodytracker.domain.measurements

import android.content.res.AssetManager
import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UserProfile
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min

class GenerateFakeMeasurementsWithPhotosUseCase(
    private val measurementRepository: MeasurementRepository,
    private val photoStorage: InternalPhotoStorage,
    private val generateFakeMeasurementsUseCase: GenerateFakeMeasurementsUseCase,
    private val assetManager: AssetManager,
) {
    suspend operator fun invoke(
        profile: UserProfile?,
        leanBodyWeightKg: Double,
        minFatBodyWeightKg: Double,
        maxFatBodyWeightKg: Double,
    ) {
        cleanupExistingMeasurementPhotos()

        generateFakeMeasurementsUseCase(
            profile = profile,
            leanBodyWeightKg = leanBodyWeightKg,
            minFatBodyWeightKg = minFatBodyWeightKg,
            maxFatBodyWeightKg = maxFatBodyWeightKg,
        )

        seedPhotosForGeneratedMeasurements(profile = profile)
    }

    private suspend fun cleanupExistingMeasurementPhotos() {
        val existingPhotoPaths = measurementRepository.getAll()
            .mapNotNull { it.photoFilePath }
            .distinct()

        existingPhotoPaths.forEach { path ->
            photoStorage.deletePhoto(path)
        }
    }

    private suspend fun seedPhotosForGeneratedMeasurements(profile: UserProfile?) {
        val effectiveSex = profile?.sex ?: Sex.Male
        val effectiveHeightCm = (profile?.heightCm?.toDouble() ?: 175.0).coerceAtLeast(120.0)

        val navyMeasurements = measurementRepository.getAll()
            .mapNotNull { measurement ->
                val navyBodyFatPercent = measurement.computeNavyBodyFatPercent(
                    sex = effectiveSex,
                    heightCm = effectiveHeightCm,
                ) ?: return@mapNotNull null
                NavyMeasurement(measurement = measurement, navyBodyFatPercent = navyBodyFatPercent)
            }

        if (navyMeasurements.isEmpty()) {
            return
        }

        val minNavyBodyFatPercent = navyMeasurements.minOf { it.navyBodyFatPercent }
        val maxNavyBodyFatPercent = navyMeasurements.maxOf { it.navyBodyFatPercent }
        val photoTargets = buildPhotoTargetBodyFatPercents(
            minBodyFatPercent = minNavyBodyFatPercent,
            maxBodyFatPercent = maxNavyBodyFatPercent,
            photoCount = DEFAULT_FAKE_PHOTO_COUNT,
        )
        val photoBinaryCache = mutableMapOf<Int, ByteArray?>()
        val everySecondMeasurement = navyMeasurements.filterIndexed { index, _ -> index % 2 == 0 }

        everySecondMeasurement.forEach { navyMeasurement ->
            val photoLabel = closestPhotoLabel(
                bodyFatPercent = navyMeasurement.navyBodyFatPercent,
                targetBodyFatPercents = photoTargets,
            )
            val photoBinaryContent = photoBinaryCache.getOrPut(photoLabel) {
                loadAssetPhoto(label = photoLabel)
            } ?: return@forEach

            val photoPath = photoStorage.writePhotoForMeasurement(
                measurementId = navyMeasurement.measurement.id,
                measurementDateEpochMillis = navyMeasurement.measurement.dateEpochMillis,
                photoBinaryContent = photoBinaryContent,
            )
            measurementRepository.update(navyMeasurement.measurement.copy(photoFilePath = photoPath))
        }
    }

    private fun loadAssetPhoto(label: Int): ByteArray? {
        return runCatching {
            assetManager.open("$ASSET_DIRECTORY/$label.jpg").use { inputStream ->
                inputStream.readBytes()
            }
        }.getOrNull()
    }

    private companion object {
        const val ASSET_DIRECTORY = "ordered"
        const val DEFAULT_FAKE_PHOTO_COUNT = 10
    }
}

private data class NavyMeasurement(
    val measurement: BodyMeasurement,
    val navyBodyFatPercent: Double,
)

internal fun buildPhotoTargetBodyFatPercents(
    minBodyFatPercent: Double,
    maxBodyFatPercent: Double,
    photoCount: Int,
): List<Double> {
    if (photoCount <= 0) {
        return emptyList()
    }

    val normalizedMin = min(minBodyFatPercent, maxBodyFatPercent)
    val normalizedMax = max(minBodyFatPercent, maxBodyFatPercent)

    if (photoCount == 1) {
        return listOf(normalizedMax)
    }

    val step = (normalizedMax - normalizedMin) / (photoCount - 1).toDouble()
    return (0 until photoCount)
        .map { index ->
            normalizedMax - (step * index)
        }
}

internal fun closestPhotoLabel(
    bodyFatPercent: Double,
    targetBodyFatPercents: List<Double>,
): Int {
    if (targetBodyFatPercents.isEmpty()) {
        return 1
    }

    return targetBodyFatPercents
        .indices
        .minByOrNull { index -> abs(targetBodyFatPercents[index] - bodyFatPercent) }
        ?.plus(1)
        ?: 1
}

private fun BodyMeasurement.computeNavyBodyFatPercent(
    sex: Sex,
    heightCm: Double,
): Double? {
    if (heightCm <= 0.0) {
        return null
    }

    val neck = neckCircumferenceCm ?: return null
    val waist = waistCircumferenceCm ?: return null

    val measurementPart = when (sex) {
        Sex.Male -> waist - neck
        Sex.Female -> {
            val hip = hipCircumferenceCm ?: return null
            waist + hip - neck
        }
    }

    if (measurementPart <= 0.0) {
        return null
    }

    return when (sex) {
        Sex.Male -> (86.010 * log10(measurementPart)) - (70.041 * log10(heightCm)) + 30.30
        Sex.Female -> (163.205 * log10(measurementPart)) - (97.684 * log10(heightCm)) - 104.912
    }
}
