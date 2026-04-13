package de.t_animal.opensourcebodytracker.ui.navigation

import androidx.lifecycle.SavedStateHandle

object Routes {
    const val OnboardingGraph = "onboarding"
    const val OnboardingStart = "onboarding/start"
    const val OnboardingProfile = "onboarding/profile"
    const val OnboardingAnalysis = "onboarding/analysis"
    const val OnboardingReminders = "onboarding/reminders"
    const val ImportBackup = "onboarding/import"

    const val Settings = "settings"
    const val Profile = "settings/profile"
    const val SettingsMisc = "settings/misc"
    const val SettingsMeasurements = "settings/measurements"
    const val SettingsMeasurementVisibility = "settings/measurement-visibility"
    const val Reminders = "settings/reminders"
    const val Export = "settings/export"
    const val About = "settings/about"

    const val MeasurementList = "measurements"
    const val MeasurementListAll = "measurements/all"
    const val MeasurementAdd = "measurements/add"
    const val MeasurementEditIdArg = "measurementId"
    const val MeasurementEdit = "measurements/edit/{$MeasurementEditIdArg}"

    const val Analysis = "analysis"

    const val Photos = "photos"
    const val PhotoCompareLeftIdArg = "leftMeasurementId"
    const val PhotoCompareRightIdArg = "rightMeasurementId"
    const val PhotoCompare =
        "photos/compare/{$PhotoCompareLeftIdArg}/{$PhotoCompareRightIdArg}"
    const val PhotoAnimateIdsArg = "ids"
    const val PhotoAnimate = "photos/animate/{$PhotoAnimateIdsArg}"

    const val MeasurementGuidance = "measurement-guidance"
    const val HealthRatingGuide = "health-rating-guide"

    const val FakeDataGenerator = "debug/fake-data-generator"

    fun measurementEditRoute(id: Long): String = "measurements/edit/$id"

    fun parseMeasurementEditId(savedStateHandle: SavedStateHandle): Long? =
        savedStateHandle.get<Long>(MeasurementEditIdArg)

    fun photoCompareRoute(leftMeasurementId: Long, rightMeasurementId: Long): String {
        return "photos/compare/$leftMeasurementId/$rightMeasurementId"
    }

    fun parsePhotoCompareIds(savedStateHandle: SavedStateHandle): Pair<Long, Long> {
        val left: Long = checkNotNull(savedStateHandle[PhotoCompareLeftIdArg])
        val right: Long = checkNotNull(savedStateHandle[PhotoCompareRightIdArg])
        return left to right
    }

    fun photoAnimateRoute(selectedMeasurementIds: List<Long>): String {
        return "photos/animate/${selectedMeasurementIds.joinToString(",")}"
    }

    fun parsePhotoAnimateIds(savedStateHandle: SavedStateHandle): List<Long> {
        return checkNotNull(savedStateHandle.get<String>(PhotoAnimateIdsArg))
            .split(",")
            .map { it.toLong() }
    }
}
