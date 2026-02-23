package de.t_animal.opensourcebodytracker.ui.navigation

object Routes {
    const val Onboarding = "onboarding"
    const val Profile = "profile"
    const val Settings = "settings"
    
    const val MeasurementList = "measurement_list"
    const val MeasurementAdd = "measurement_add"
    const val MeasurementEditIdArg = "measurementId"
    const val MeasurementEdit = "measurement_edit/{$MeasurementEditIdArg}"

    const val Analysis = "analysis"
    const val Photos = "photos"

    fun measurementEditRoute(id: Long): String = "measurement_edit/$id"
}
