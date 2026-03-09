package de.t_animal.opensourcebodytracker.ui.navigation

import android.content.pm.ApplicationInfo
import android.widget.Toast
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import de.t_animal.opensourcebodytracker.core.model.defaultSettingsState
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.core.notifications.ReminderAlarmScheduler
import de.t_animal.opensourcebodytracker.core.notifications.ReminderNotificationPoster
import de.t_animal.opensourcebodytracker.core.notifications.ReminderNotificationResult
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.demodata.GenerateDemoDataUseCase
import de.t_animal.opensourcebodytracker.domain.measurements.DeleteMeasurementUseCase
import de.t_animal.opensourcebodytracker.domain.measurements.SaveMeasurementUseCase
import de.t_animal.opensourcebodytracker.domain.metrics.CalculateMeasurementDerivedMetricsUseCase
import de.t_animal.opensourcebodytracker.feature.analysis.AnalysisRoute
import de.t_animal.opensourcebodytracker.feature.debug.FakeDataGeneratorRoute
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementEditRoute
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementListFullRoute
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementListRoute
import de.t_animal.opensourcebodytracker.feature.photos.PhotoAnimationRoute
import de.t_animal.opensourcebodytracker.feature.photos.PhotoCompareRoute
import de.t_animal.opensourcebodytracker.feature.photos.PhotosRoute
import de.t_animal.opensourcebodytracker.feature.settings.SettingsRoute
import de.t_animal.opensourcebodytracker.feature.settings.onboarding.OnboardingAnalysisRoute
import de.t_animal.opensourcebodytracker.feature.settings.onboarding.OnboardingStartRoute
import de.t_animal.opensourcebodytracker.feature.settings.profile.ProfileMode
import de.t_animal.opensourcebodytracker.feature.settings.profile.ProfileRoute
import de.t_animal.opensourcebodytracker.feature.settings.reminders.ReminderMode
import de.t_animal.opensourcebodytracker.feature.settings.reminders.ReminderSettingsRoute
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyTrackerNavHost(
    profileRepository: ProfileRepository,
    settingsRepository: SettingsRepository,
    measurementRepository: MeasurementRepository,
    internalPhotoStorage: InternalPhotoStorage,
    calculateMeasurementDerivedMetrics: CalculateMeasurementDerivedMetricsUseCase,
    generateDemoDataUseCase: GenerateDemoDataUseCase,
    deleteMeasurementUseCase: DeleteMeasurementUseCase,
    saveMeasurementUseCase: SaveMeasurementUseCase,
    reminderNotificationPoster: ReminderNotificationPoster,
    reminderAlarmScheduler: ReminderAlarmScheduler,
    openMeasurementAddSignal: StateFlow<Long>,
    onResetApp: () -> Unit,
) {
    val context = LocalContext.current
    val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    val navController = rememberNavController()

    val settings by settingsRepository.settingsFlow.collectAsStateWithLifecycle(
        initialValue = defaultSettingsState(),
    )
    val openMeasurementAddRequest by openMeasurementAddSignal.collectAsStateWithLifecycle(
        initialValue = 0L,
    )
    var handledOpenMeasurementAddRequest by remember { mutableStateOf(0L) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val onboardingRoutes = setOf(
        Routes.OnboardingStart,
        Routes.OnboardingProfile,
        Routes.OnboardingAnalysis,
        Routes.OnboardingReminders,
    )

    LaunchedEffect(settings, currentRoute) {
        val route = currentRoute ?: return@LaunchedEffect

        val shouldShowOnboarding = !settings.onboardingCompleted

        if (shouldShowOnboarding && route !in onboardingRoutes) {
            while (navController.popBackStack()) {
            }
            navController.navigate(Routes.OnboardingStart) {
                launchSingleTop = true
            }
        } else if (!shouldShowOnboarding && route in onboardingRoutes && openMeasurementAddRequest <= 0L) {
            navController.navigate(Routes.MeasurementList) {
                popUpTo(Routes.OnboardingStart) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(openMeasurementAddRequest, settings.onboardingCompleted, currentRoute) {
        if (openMeasurementAddRequest <= handledOpenMeasurementAddRequest) {
            return@LaunchedEffect
        }
        if (!settings.onboardingCompleted) {
            return@LaunchedEffect
        }

        val route = currentRoute ?: return@LaunchedEffect
        handledOpenMeasurementAddRequest = openMeasurementAddRequest

        if (route == Routes.MeasurementAdd) {
            return@LaunchedEffect
        }

        if (route != Routes.MeasurementList) {
            navController.navigate(Routes.MeasurementList) {
                if (route in onboardingRoutes) {
                    popUpTo(Routes.OnboardingStart) { inclusive = true }
                }
                launchSingleTop = true
            }
        }

        navController.navigate(Routes.MeasurementAdd) {
            launchSingleTop = true
        }
    }

    val onTriggerReminder = {
        when (reminderNotificationPoster.showReminderNotification()) {
            ReminderNotificationResult.Shown -> Unit
            ReminderNotificationResult.NotificationsDisabled -> {
                Toast.makeText(
                    context,
                    "Notifications are disabled for this app.",
                    Toast.LENGTH_LONG,
                ).show()
            }

            ReminderNotificationResult.Failed -> {
                Toast.makeText(
                    context,
                    "Could not show reminder notification.",
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.OnboardingStart,
    ) {
        composable(Routes.OnboardingStart) {
            OnboardingStartRoute(
                profileRepository = profileRepository,
                settingsRepository = settingsRepository,
                generateDemoDataUseCase = generateDemoDataUseCase,
                onCreateProfileSelected = {
                    navController.navigate(Routes.OnboardingProfile) {
                        launchSingleTop = true
                    }
                },
                onDemoModeCompleted = {
                    navController.navigate(Routes.MeasurementList) {
                        popUpTo(Routes.OnboardingStart) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.OnboardingProfile) {
            ProfileRoute(
                repository = profileRepository,
                settingsRepository = settingsRepository,
                mode = ProfileMode.Onboarding,
                onFinished = {
                    navController.navigate(Routes.OnboardingAnalysis) {
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Routes.OnboardingAnalysis) {
            OnboardingAnalysisRoute(
                settingsRepository = settingsRepository,
                profileRepository = profileRepository,
                onFinished = {
                    navController.navigate(Routes.OnboardingReminders) {
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Routes.OnboardingReminders) {
            ReminderSettingsRoute(
                settingsRepository = settingsRepository,
                reminderAlarmScheduler = reminderAlarmScheduler,
                mode = ReminderMode.Onboarding,
                onNavigateBack = {
                    navController.navigate(Routes.MeasurementList) {
                        popUpTo(Routes.OnboardingStart) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.Profile) {
            ProfileRoute(
                repository = profileRepository,
                settingsRepository = settingsRepository,
                mode = ProfileMode.Settings,
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

        composable(Routes.Reminders) {
            ReminderSettingsRoute(
                settingsRepository = settingsRepository,
                reminderAlarmScheduler = reminderAlarmScheduler,
                onNavigateBack = { navController.popBackStack() },
            )
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
                onOpenReminders = { navController.navigate(Routes.Reminders) },
                onTriggerReminder = onTriggerReminder,
                onOpenFakeDataGenerator = if (isDebuggable) {
                    { navController.navigate(Routes.FakeDataGenerator) }
                } else {
                    null
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
                    showDemoBanner = settings.isDemoMode,
                    onResetApp = onResetApp,
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
                onOpenReminders = { navController.navigate(Routes.Reminders) },
                onTriggerReminder = onTriggerReminder,
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
                onOpenReminders = { navController.navigate(Routes.Reminders) },
                onTriggerReminder = onTriggerReminder,
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
                        onOpenAnimate = { selectedMeasurementIds ->
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set(
                                    Routes.PhotoAnimateSelectionIdsKey,
                                    selectedMeasurementIds.toLongArray(),
                                )
                            navController.navigate(Routes.PhotoAnimate)
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

        composable(Routes.PhotoAnimate) {
            val selectedMeasurementIds = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<LongArray>(Routes.PhotoAnimateSelectionIdsKey)
                ?.toList()
                .orEmpty()

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Animation") },
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
                    PhotoAnimationRoute(
                        measurementRepository = measurementRepository,
                        photoStorage = internalPhotoStorage,
                        selectedMeasurementIds = selectedMeasurementIds,
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
                            generateDemoDataUseCase = generateDemoDataUseCase,
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
                deleteMeasurementUseCase = deleteMeasurementUseCase,
                saveMeasurementUseCase = saveMeasurementUseCase,
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
                deleteMeasurementUseCase = deleteMeasurementUseCase,
                saveMeasurementUseCase = saveMeasurementUseCase,
                measurementId = id,
                onFinished = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }
    }
}
