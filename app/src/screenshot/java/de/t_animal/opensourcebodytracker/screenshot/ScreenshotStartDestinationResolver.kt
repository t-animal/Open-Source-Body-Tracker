package de.t_animal.opensourcebodytracker.screenshot

import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.ui.navigation.Routes
import javax.inject.Inject

class ScreenshotStartDestinationResolver @Inject constructor(
    private val measurementRepository: MeasurementRepository,
) {
    suspend fun resolve(target: ScreenshotTarget): String {
        return when (target) {
            ScreenshotTarget.MeasurementList -> Routes.MeasurementList
            ScreenshotTarget.MeasurementListAll -> Routes.MeasurementListAll
            ScreenshotTarget.Analysis -> Routes.Analysis
            ScreenshotTarget.Photo -> Routes.Photos
            ScreenshotTarget.PhotoCompare -> resolvePhotoCompareRoute()
        }
    }

    private suspend fun resolvePhotoCompareRoute(): String {
        val photoMeasurements = measurementRepository.getAll()
            .filter { it.photoFilePath != null }
            .sortedBy { it.dateEpochMillis }

        check(photoMeasurements.size >= 2) {
            "Screenshot photo compare requires at least two seeded demo measurements with photos."
        }

        return Routes.photoCompareRoute(
            leftMeasurementId = photoMeasurements[0].id,
            rightMeasurementId = photoMeasurements[1].id,
        )
    }
}
