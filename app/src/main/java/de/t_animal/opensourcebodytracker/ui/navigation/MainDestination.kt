package de.t_animal.opensourcebodytracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.SsidChart
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
) {
    Measurements(route = Routes.MeasurementList, title = "Measurements", icon = Icons.Filled.MonitorWeight),
    Analysis(route = Routes.Analysis, title = "Analysis", icon = Icons.Filled.SsidChart),
    Photos(route = Routes.Photos, title = "Photos", icon = Icons.Filled.CameraAlt),
}

fun mainDestinationForRoute(route: String?): MainDestination {
    return when (route) {
        Routes.Analysis -> MainDestination.Analysis
        Routes.Photos -> MainDestination.Photos
        else -> MainDestination.Measurements
    }
}
