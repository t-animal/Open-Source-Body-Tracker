package de.t_animal.opensourcebodytracker.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.SsidChart
import androidx.compose.ui.graphics.vector.ImageVector
import de.t_animal.opensourcebodytracker.R

enum class MainDestination(
    val route: String,
    @StringRes val titleResId: Int,
    val icon: ImageVector,
) {
    Measurements(route = Routes.MeasurementList, titleResId = R.string.nav_measurements, icon = Icons.Filled.MonitorWeight),
    Analysis(route = Routes.Analysis, titleResId = R.string.nav_analysis, icon = Icons.Filled.SsidChart),
    Photos(route = Routes.Photos, titleResId = R.string.nav_photos, icon = Icons.Filled.CameraAlt),
}

fun mainDestinationForRoute(route: String?): MainDestination {
    return when (route) {
        Routes.Analysis -> MainDestination.Analysis
        Routes.Photos -> MainDestination.Photos
        else -> MainDestination.Measurements
    }
}
