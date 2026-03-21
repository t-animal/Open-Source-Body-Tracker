package de.t_animal.opensourcebodytracker.domain.demodata

import de.t_animal.opensourcebodytracker.core.model.UserProfile
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import javax.inject.Inject

class GenerateDemoDataUseCase @Inject constructor(
    private val measurementRepository: MeasurementRepository,
    private val demoDataMeasurementSeriesGenerator: DemoDataMeasurementSeriesGenerator,
    private val demoDataPhotoSeeder: DemoDataPhotoSeeder,
) {
    suspend operator fun invoke(
        profile: UserProfile?,
        leanBodyWeightKg: Double,
        minFatBodyWeightKg: Double,
        maxFatBodyWeightKg: Double,
    ) {
        demoDataPhotoSeeder.cleanupExistingMeasurementPhotos()

        measurementRepository.replaceAll(
            demoDataMeasurementSeriesGenerator.generateMeasurements(
                sex = profile?.sex,
                heightCm = profile?.heightCm?.toDouble(),
                dateOfBirth = profile?.dateOfBirth,
                leanBodyWeightKg = leanBodyWeightKg,
                minFatBodyWeightKg = minFatBodyWeightKg,
                maxFatBodyWeightKg = maxFatBodyWeightKg,
            ),
        )

        demoDataPhotoSeeder.seedPhotosForGeneratedMeasurements(profile = profile)
    }
}
