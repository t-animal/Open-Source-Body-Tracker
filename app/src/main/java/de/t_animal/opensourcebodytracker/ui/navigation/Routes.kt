package de.t_animal.opensourcebodytracker.ui.navigation

import androidx.lifecycle.SavedStateHandle

object Routes {
    const val OnboardingStart = "onboarding/start"
    const val OnboardingProfile = "onboarding/profile"
    const val OnboardingAnalysis = "onboarding/analysis"
    const val OnboardingReminders = "onboarding/reminders"
    const val ImportBackup = "import_backup"

    const val Profile = "profile"
    const val Settings = "settings"
    const val SettingsMeasurements = "settings/measurements"
    const val SettingsMeasurementVisibility = "settings/measurement_visibility"
    const val Reminders = "reminders" // TODO: align name and value with other routes, e.g. "settings/reminders"
    const val Export = "export"
    const val About = "about"
    const val FakeDataGenerator = "fake_data_generator"
    
    const val MeasurementList = "measurement_list"
    const val MeasurementListAll = "measurement_list_all"
    const val MeasurementAdd = "measurement_add"
    const val MeasurementEditIdArg = "measurementId"
    const val MeasurementEdit = "measurement_edit/{$MeasurementEditIdArg}"

    const val Analysis = "analysis"
    const val Photos = "photos"
    const val PhotoCompareLeftIdArg = "leftMeasurementId"
    const val PhotoCompareRightIdArg = "rightMeasurementId"
    const val PhotoCompare =
        "photo_compare/{$PhotoCompareLeftIdArg}/{$PhotoCompareRightIdArg}"
    const val PhotoAnimateIdsArg = "ids"
    const val PhotoAnimate = "photo_animate/{$PhotoAnimateIdsArg}"

    fun measurementEditRoute(id: Long): String = "measurement_edit/$id"

    fun parseMeasurementEditId(savedStateHandle: SavedStateHandle): Long? =
        savedStateHandle.get<Long>(MeasurementEditIdArg)

    fun photoCompareRoute(leftMeasurementId: Long, rightMeasurementId: Long): String {
        return "photo_compare/$leftMeasurementId/$rightMeasurementId"
    }

    fun parsePhotoCompareIds(savedStateHandle: SavedStateHandle): Pair<Long, Long> {
        val left: Long = checkNotNull(savedStateHandle[PhotoCompareLeftIdArg])
        val right: Long = checkNotNull(savedStateHandle[PhotoCompareRightIdArg])
        return left to right
    }

    fun photoAnimateRoute(selectedMeasurementIds: List<Long>): String {
        return "photo_animate/${selectedMeasurementIds.joinToString(",")}"
    }

    fun parsePhotoAnimateIds(savedStateHandle: SavedStateHandle): List<Long> {
        return checkNotNull(savedStateHandle.get<String>(PhotoAnimateIdsArg))
            .split(",")
            .map { it.toLong() }
    }
}
