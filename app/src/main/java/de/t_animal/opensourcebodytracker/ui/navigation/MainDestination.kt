package de.t_animal.opensourcebodytracker.ui.navigation

enum class MainDestination(
    val route: String,
    val title: String,
    val label: String,
) {
    Measurements(route = Routes.MeasurementList, title = "Measurements", label = "M"),
    Analysis(route = Routes.Analysis, title = "Analysis", label = "A"),
    Photos(route = Routes.Photos, title = "Photos", label = "P"),
}

fun mainDestinationForRoute(route: String?): MainDestination {
    return when (route) {
        Routes.Analysis -> MainDestination.Analysis
        Routes.Photos -> MainDestination.Photos
        else -> MainDestination.Measurements
    }
}
