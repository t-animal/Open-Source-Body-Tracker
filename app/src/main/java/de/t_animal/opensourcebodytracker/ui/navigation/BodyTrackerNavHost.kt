package de.t_animal.opensourcebodytracker.ui.navigation

import android.content.pm.ApplicationInfo
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
import androidx.compose.ui.platform.LocalContext
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
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.measurements.GenerateFakeMeasurementsWithPhotosUseCase
import de.t_animal.opensourcebodytracker.domain.metrics.CalculateMeasurementDerivedMetricsUseCase
import de.t_animal.opensourcebodytracker.feature.analysis.AnalysisRoute
import de.t_animal.opensourcebodytracker.feature.debug.FakeDataGeneratorRoute
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementEditRoute
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementListAddButton
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementListFullRoute
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementListRoute
import de.t_animal.opensourcebodytracker.feature.photos.PhotoCompareRoute
import de.t_animal.opensourcebodytracker.feature.photos.PhotosRoute
import de.t_animal.opensourcebodytracker.feature.profile.ProfileRoute
import de.t_animal.opensourcebodytracker.feature.settings.SettingsRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyTrackerNavHost(
    profileRepository: ProfileRepository,
    settingsRepository: SettingsRepository,
    measurementRepository: MeasurementRepository,
    internalPhotoStorage: InternalPhotoStorage,
    calculateMeasurementDerivedMetrics: CalculateMeasurementDerivedMetricsUseCase,
    generateFakeMeasurementsWithPhotosUseCase: GenerateFakeMeasurementsWithPhotosUseCase,
) {
    val isDebuggable = (LocalContext.current.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    val navController = rememberNavController()

    val hasProfile by profileRepository.hasProfileFlow.collectAsStateWithLifecycle(initialValue = false)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(hasProfile, currentRoute) {
        if (hasProfile && currentRoute == Routes.Onboarding) {
            navController.navigate(Routes.MeasurementList) {
                popUpTo(Routes.Onboarding) { inclusive = true }
                launchSingleTop = true
            }
        } else if (!hasProfile && currentRoute != null && currentRoute != Routes.Onboarding) {
            while (navController.popBackStack()) {
            }
            navController.navigate(Routes.Onboarding) {
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
                settingsRepository = settingsRepository,
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
                settingsRepository = settingsRepository,
                mode = de.t_animal.opensourcebodytracker.feature.profile.ProfileMode.Settings,
                onFinished = { navController.popBackStack() },
            )
        }

        composable(Routes.Settings) {
            Scaffold { contentPadding ->
                SettingsRoute(
                    settingsRepository = settingsRepository,
                    profileRepository = profileRepository,
                    onNavigateBack = { navController.popBackStack() },
                    contentPadding = contentPadding,
                )
            }
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
                onOpenFakeDataGenerator = if (isDebuggable) {
                    { navController.navigate(Routes.FakeDataGenerator) }
                } else {
                    null
                },
                floatingActionButton = {
                    MeasurementListAddButton(onAdd = { navController.navigate(Routes.MeasurementAdd) })
                },
            ) { contentPadding ->
                MeasurementListRoute(
                    measurementRepository = measurementRepository,
                    profileRepository = profileRepository,
                    settingsRepository = settingsRepository,
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
                        settingsRepository = settingsRepository,
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
                onOpenFakeDataGenerator = if (isDebuggable) {
                    { navController.navigate(Routes.FakeDataGenerator) }
                } else {
                    null
                },
            ) { contentPadding ->
                AnalysisRoute(
                    measurementRepository = measurementRepository,
                    profileRepository = profileRepository,
                    settingsRepository = settingsRepository,
                    calculateMeasurementDerivedMetrics = calculateMeasurementDerivedMetrics,
                    contentPadding = contentPadding,
                )
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
                onOpenFakeDataGenerator = if (isDebuggable) {
                    { navController.navigate(Routes.FakeDataGenerator) }
                } else {
                    null
                },
            ) { contentPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                ) {
                    PhotosRoute(
                        measurementRepository = measurementRepository,
                        photoStorage = internalPhotoStorage,
                        onOpenCompare = { leftMeasurementId, rightMeasurementId ->
                            navController.navigate(
                                Routes.photoCompareRoute(
                                    leftMeasurementId = leftMeasurementId,
                                    rightMeasurementId = rightMeasurementId,
                                ),
                            )
                        },
                    )
                }
            }
        }

        composable(
            route = Routes.PhotoCompare,
            arguments = listOf(
                navArgument(Routes.PhotoCompareLeftIdArg) { type = NavType.LongType },
                navArgument(Routes.PhotoCompareRightIdArg) { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            val leftMeasurementId = backStackEntry.arguments?.getLong(Routes.PhotoCompareLeftIdArg)
            val rightMeasurementId = backStackEntry.arguments?.getLong(Routes.PhotoCompareRightIdArg)

            if (leftMeasurementId == null || rightMeasurementId == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@composable
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Compare") },
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
                    PhotoCompareRoute(
                        measurementRepository = measurementRepository,
                        photoStorage = internalPhotoStorage,
                        leftMeasurementId = leftMeasurementId,
                        rightMeasurementId = rightMeasurementId,
                    )
                }
            }
        }

        if (isDebuggable) {
            composable(Routes.FakeDataGenerator) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Fake data generator") },
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
                        FakeDataGeneratorRoute(
                            profileRepository = profileRepository,
                            generateFakeMeasurementsWithPhotosUseCase = generateFakeMeasurementsWithPhotosUseCase,
                        )
                    }
                }
            }
        }

        composable(Routes.MeasurementAdd) {
            MeasurementEditRoute(
                repository = measurementRepository,
                photoStorage = internalPhotoStorage,
                profileRepository = profileRepository,
                settingsRepository = settingsRepository,
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
                photoStorage = internalPhotoStorage,
                profileRepository = profileRepository,
                settingsRepository = settingsRepository,
                measurementId = id,
                onFinished = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }
    }
}
