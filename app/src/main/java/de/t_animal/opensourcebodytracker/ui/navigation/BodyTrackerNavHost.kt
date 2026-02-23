package de.t_animal.opensourcebodytracker.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.domain.metrics.CalculateMeasurementDerivedMetricsUseCase
import de.t_animal.opensourcebodytracker.feature.analysis.AnalysisScreen
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementEditRoute
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementListAddButton
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementListFullRoute
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementListRoute
import de.t_animal.opensourcebodytracker.feature.photos.PhotosScreen
import de.t_animal.opensourcebodytracker.feature.profile.ProfileRoute
import de.t_animal.opensourcebodytracker.feature.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyTrackerNavHost(
    profileRepository: ProfileRepository,
    measurementRepository: MeasurementRepository,
    calculateMeasurementDerivedMetrics: CalculateMeasurementDerivedMetricsUseCase,
) {
    val navController = rememberNavController()

    val profileOrNull by profileRepository.profileFlow.collectAsStateWithLifecycle(initialValue = null)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(profileOrNull, currentRoute) {
        if (profileOrNull != null && currentRoute == Routes.Onboarding) {
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

        composable(Routes.Profile) {
            ProfileRoute(
                repository = profileRepository,
                mode = de.t_animal.opensourcebodytracker.feature.profile.ProfileMode.Settings,
                onFinished = { navController.popBackStack() },
            )
        }

        composable(Routes.Settings) {
            SettingsScreen()
        }

        composable(Routes.MeasurementList) {
            MainScreenScaffold(
                selectedDestination = MainDestination.Measurements,
                onMainDestinationSelected = { destination ->
                    navController.navigate(destination.route) {
                        launchSingleTop = true
                    }
                },
                onOpenProfile = { navController.navigate(Routes.Profile) },
                onOpenSettings = { navController.navigate(Routes.Settings) },
                floatingActionButton = {
                    MeasurementListAddButton(onAdd = { navController.navigate(Routes.MeasurementAdd) })
                },
            ) { contentPadding ->
                MeasurementListRoute(
                    measurementRepository = measurementRepository,
                    profileRepository = profileRepository,
                    calculateMeasurementDerivedMetrics = calculateMeasurementDerivedMetrics,
                    onEdit = { id -> navController.navigate(Routes.measurementEditRoute(id)) },
                    onAdd = { navController.navigate(Routes.MeasurementAdd) },
                    onOpenMore = { navController.navigate(Routes.MeasurementListAll) },
                    contentPadding = contentPadding,
                )
            }
        }

        composable(Routes.MeasurementListAll) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("All Measurements") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                )
                            }
                        },
                    )
                },
            ) { contentPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                ) {
                    MeasurementListFullRoute(
                        measurementRepository = measurementRepository,
                        profileRepository = profileRepository,
                        calculateMeasurementDerivedMetrics = calculateMeasurementDerivedMetrics,
                        onEdit = { id -> navController.navigate(Routes.measurementEditRoute(id)) },
                    )
                }
            }
        }

        composable(Routes.Analysis) {
            MainScreenScaffold(
                selectedDestination = MainDestination.Analysis,
                onMainDestinationSelected = { destination ->
                    navController.navigate(destination.route) {
                        launchSingleTop = true
                    }
                },
                onOpenProfile = { navController.navigate(Routes.Profile) },
                onOpenSettings = { navController.navigate(Routes.Settings) },
            ) { contentPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                ) {
                    AnalysisScreen()
                }
            }
        }

        composable(Routes.Photos) {
            MainScreenScaffold(
                selectedDestination = MainDestination.Photos,
                onMainDestinationSelected = { destination ->
                    navController.navigate(destination.route) {
                        launchSingleTop = true
                    }
                },
                onOpenProfile = { navController.navigate(Routes.Profile) },
                onOpenSettings = { navController.navigate(Routes.Settings) },
            ) { contentPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                ) {
                    PhotosScreen()
                }
            }
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
