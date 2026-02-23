package de.t_animal.opensourcebodytracker.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.domain.metrics.CalculateMeasurementDerivedMetricsUseCase
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementEditRoute
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementListRoute
import de.t_animal.opensourcebodytracker.feature.profile.ProfileRoute

@Composable
fun BodyTrackerNavHost(
    profileRepository: ProfileRepository,
    measurementRepository: MeasurementRepository,
    calculateMeasurementDerivedMetrics: CalculateMeasurementDerivedMetricsUseCase,
) {
    val navController = rememberNavController()

    val profileOrNull by profileRepository.profileFlow.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(profileOrNull) {
        if (profileOrNull != null) {
            navController.navigate(Routes.MeasurementList) {
                popUpTo(Routes.Onboarding) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Onboarding,
    ) {
        composable(Routes.Onboarding) {
            ProfileRoute(
                repository = profileRepository,
                mode = de.t_animal.opensourcebodytracker.feature.profile.ProfileMode.Onboarding,
                onFinished = {
                    navController.navigate(Routes.MeasurementList) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.Settings) {
            ProfileRoute(
                repository = profileRepository,
                mode = de.t_animal.opensourcebodytracker.feature.profile.ProfileMode.Settings,
                onFinished = { navController.popBackStack() },
            )
        }

        composable(Routes.MeasurementList) {
            MeasurementListRoute(
                measurementRepository = measurementRepository,
                profileRepository = profileRepository,
                calculateMeasurementDerivedMetrics = calculateMeasurementDerivedMetrics,
                onAdd = { navController.navigate(Routes.MeasurementAdd) },
                onEdit = { id -> navController.navigate(Routes.measurementEditRoute(id)) },
                onOpenSettings = { navController.navigate(Routes.Settings) },
            )
        }

        composable(Routes.MeasurementAdd) {
            MeasurementEditRoute(
                repository = measurementRepository,
                profileRepository = profileRepository,
                measurementId = null,
                onFinished = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.MeasurementEdit,
            arguments = listOf(
                navArgument(Routes.MeasurementEditIdArg) { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong(Routes.MeasurementEditIdArg)
            if (id == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@composable
            }

            MeasurementEditRoute(
                repository = measurementRepository,
                profileRepository = profileRepository,
                measurementId = id,
                onFinished = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }
    }
}
