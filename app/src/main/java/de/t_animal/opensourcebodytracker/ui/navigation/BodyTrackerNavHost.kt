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
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.res.stringResource
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.GeneralSettings
import de.t_animal.opensourcebodytracker.infra.notifications.ReminderNotificationPoster
import de.t_animal.opensourcebodytracker.infra.notifications.ReminderNotificationResult
import de.t_animal.opensourcebodytracker.data.settings.GeneralSettingsRepository
import de.t_animal.opensourcebodytracker.domain.export.AutomaticExportScheduler
import de.t_animal.opensourcebodytracker.feature.analysis.AnalysisRoute
import de.t_animal.opensourcebodytracker.feature.debug.FakeDataGeneratorRoute
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementEditRoute
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementListFullRoute
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementListRoute
import de.t_animal.opensourcebodytracker.feature.photos.PhotoAnimationRoute
import de.t_animal.opensourcebodytracker.feature.photos.PhotoCompareRoute
import de.t_animal.opensourcebodytracker.feature.photos.PhotosRoute
import de.t_animal.opensourcebodytracker.feature.settings.SettingsRoute
import de.t_animal.opensourcebodytracker.feature.settings.misc.MiscSettingsRoute
import de.t_animal.opensourcebodytracker.feature.settings.about.AboutRoute
import de.t_animal.opensourcebodytracker.feature.importbackup.ImportBackupRoute
import de.t_animal.opensourcebodytracker.feature.settings.export.ExportSettingsRoute
import de.t_animal.opensourcebodytracker.feature.settings.measurements.MeasurementSettingsRoute
import de.t_animal.opensourcebodytracker.feature.settings.onboarding.onboardingNavGraph
import de.t_animal.opensourcebodytracker.feature.settings.profile.ProfileMode
import de.t_animal.opensourcebodytracker.feature.settings.profile.ProfileRoute
import de.t_animal.opensourcebodytracker.feature.settings.reminders.ReminderMode
import de.t_animal.opensourcebodytracker.feature.settings.reminders.ReminderSettingsRoute
import de.t_animal.opensourcebodytracker.feature.settings.visibility.MeasurementVisibilitySettingsRoute
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyTrackerNavHost(
    generalSettingsRepository: GeneralSettingsRepository,
    automaticExportScheduler: AutomaticExportScheduler,
    reminderNotificationPoster: ReminderNotificationPoster,
    openMeasurementAddSignal: StateFlow<Long>,
    onResetApp: () -> Unit,
) {
    val context = LocalContext.current
    val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    val navController = rememberNavController()

    val generalSettings by generalSettingsRepository.settingsFlow.collectAsStateWithLifecycle(
        initialValue = GeneralSettings(),
    )
    val openMeasurementAddRequest by openMeasurementAddSignal.collectAsStateWithLifecycle(
        initialValue = 0L,
    )
    var handledOpenMeasurementAddRequest by remember { mutableLongStateOf(0L) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val onboardingRoutes = setOf(
        Routes.OnboardingGraph,
        Routes.OnboardingStart,
        Routes.OnboardingProfile,
        Routes.OnboardingAnalysis,
        Routes.OnboardingReminders,
        Routes.ImportBackup,
    )

    LaunchedEffect(generalSettings, currentRoute) {
        val route = currentRoute ?: return@LaunchedEffect

        val shouldShowOnboarding = !generalSettings.onboardingCompleted

        if (shouldShowOnboarding && route !in onboardingRoutes) {
            while (navController.popBackStack()) {
            }
            navController.navigate(Routes.OnboardingStart) {
                launchSingleTop = true
            }
        } else if (!shouldShowOnboarding && route in onboardingRoutes && openMeasurementAddRequest <= 0L) {
            navController.navigate(Routes.MeasurementList) {
                popUpTo(Routes.OnboardingGraph) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(openMeasurementAddRequest, generalSettings.onboardingCompleted, currentRoute) {
        if (openMeasurementAddRequest <= handledOpenMeasurementAddRequest) {
            return@LaunchedEffect
        }
        if (!generalSettings.onboardingCompleted) {
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
                    popUpTo(Routes.OnboardingGraph) { inclusive = true }
                }
                launchSingleTop = true
            }
        }

        navController.navigate(Routes.MeasurementAdd) {
            launchSingleTop = true
        }
    }

    val reminderDisabledMessage = stringResource(R.string.reminder_toast_notifications_disabled)
    val reminderFailedMessage = stringResource(R.string.reminder_toast_failed)
    val onTriggerReminder = {
        when (reminderNotificationPoster.showReminderNotification()) {
            ReminderNotificationResult.Shown -> Unit
            ReminderNotificationResult.NotificationsDisabled -> {
                Toast.makeText(
                    context,
                    reminderDisabledMessage,
                    Toast.LENGTH_LONG,
                ).show()
            }

            ReminderNotificationResult.Failed -> {
                Toast.makeText(
                    context,
                    reminderFailedMessage,
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    val onDebugTriggerReminder: (() -> Unit)? = if (isDebuggable) onTriggerReminder else null
    val onDebugResetApp: (() -> Unit)? = if (isDebuggable) onResetApp else null
    val onDebugOpenFakeDataGenerator: (() -> Unit)? = if (isDebuggable) {
        { navController.navigate(Routes.FakeDataGenerator) }
    } else {
        null
    }
    val onDebugScheduleExportIn2Minutes: (() -> Unit)? = if (isDebuggable) {
        { automaticExportScheduler.scheduleExportInMinutes(2) }
    } else {
        null
    }

    NavHost(
        navController = navController,
        startDestination = Routes.OnboardingGraph,
    ) {
        onboardingNavGraph(
            navController = navController,
            onDemoModeCompleted = {
                navController.navigate(Routes.MeasurementList) {
                    popUpTo(Routes.OnboardingGraph) { inclusive = true }
                }
            },
            onImportBackupClicked = {
                navController.navigate(Routes.ImportBackup) {
                    launchSingleTop = true
                }
            },
            onOnboardingCompleted = {
                navController.navigate(Routes.MeasurementList) {
                    popUpTo(Routes.OnboardingGraph) { inclusive = true }
                }
            },
        )

        composable(Routes.ImportBackup) {
            ImportBackupRoute(
                onNavigateBack = { navController.popBackStack() },
                onImportCompleted = {
                    navController.navigate(Routes.MeasurementList) {
                        popUpTo(Routes.OnboardingGraph) { inclusive = true }
                    }
                },
                onResetApp = onResetApp,
            )
        }

        composable(Routes.Profile) {
            ProfileRoute(
                mode = ProfileMode.Settings,
                onFinished = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Routes.Settings) {
            SettingsRoute(
                onNavigateBack = { navController.popBackStack() },
                onOpenProfile = { navController.navigate(Routes.Profile) },
                onOpenMisc = { navController.navigate(Routes.SettingsMisc) },
                onOpenMeasurementsAndAnalysis = { navController.navigate(Routes.SettingsMeasurements) },
                onOpenMeasurementVisibility = { navController.navigate(Routes.SettingsMeasurementVisibility) },
                onOpenReminders = { navController.navigate(Routes.Reminders) },
                onOpenExport = { navController.navigate(Routes.Export) },
                onOpenAbout = { navController.navigate(Routes.About) },
            )
        }

        composable(Routes.SettingsMisc) {
            MiscSettingsRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SettingsMeasurements) {
            MeasurementSettingsRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SettingsMeasurementVisibility) {
            MeasurementVisibilitySettingsRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Routes.About) {
            AboutRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Routes.Reminders) {
            ReminderSettingsRoute(
                mode = ReminderMode.Settings,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Routes.Export) {
            ExportSettingsRoute(
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
                onOpenSettings = { navController.navigate(Routes.Settings) },
                onOpenAbout = { navController.navigate(Routes.About) },
                onTriggerReminder = onDebugTriggerReminder,
                onResetApp = onDebugResetApp,
                onOpenFakeDataGenerator = onDebugOpenFakeDataGenerator,
                onScheduleExportIn2Minutes = onDebugScheduleExportIn2Minutes,
            ) { contentPadding ->
                MeasurementListRoute(
                    onEdit = { id -> navController.navigate(Routes.measurementEditRoute(id)) },
                    onAdd = { navController.navigate(Routes.MeasurementAdd) },
                    onOpenMore = { navController.navigate(Routes.MeasurementListAll) },
                    showDemoBanner = generalSettings.isDemoMode,
                    onResetApp = onResetApp,
                    contentPadding = contentPadding,
                )
            }
        }

        composable(Routes.MeasurementListAll) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.measurement_list_title)) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.cd_back),
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
                onOpenSettings = { navController.navigate(Routes.Settings) },
                onOpenAbout = { navController.navigate(Routes.About) },
                onTriggerReminder = onDebugTriggerReminder,
                onResetApp = onDebugResetApp,
                onOpenFakeDataGenerator = onDebugOpenFakeDataGenerator,
                onScheduleExportIn2Minutes = onDebugScheduleExportIn2Minutes,
            ) { contentPadding ->
                AnalysisRoute(
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
                onOpenSettings = { navController.navigate(Routes.Settings) },
                onOpenAbout = { navController.navigate(Routes.About) },
                onTriggerReminder = onDebugTriggerReminder,
                onResetApp = onDebugResetApp,
                onOpenFakeDataGenerator = onDebugOpenFakeDataGenerator,
                onScheduleExportIn2Minutes = onDebugScheduleExportIn2Minutes,
            ) { contentPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                ) {
                    PhotosRoute(
                        onOpenCompare = { leftMeasurementId, rightMeasurementId ->
                            navController.navigate(
                                Routes.photoCompareRoute(
                                    leftMeasurementId = leftMeasurementId,
                                    rightMeasurementId = rightMeasurementId,
                                ),
                            )
                        },
                        onOpenAnimate = { selectedMeasurementIds ->
                            navController.navigate(Routes.photoAnimateRoute(selectedMeasurementIds))
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
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.photos_title_compare)) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.cd_back),
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
                    PhotoCompareRoute()
                }
            }
        }

        composable(
            route = Routes.PhotoAnimate,
            arguments = listOf(
                navArgument(Routes.PhotoAnimateIdsArg) { type = NavType.StringType },
            ),
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.photos_title_animation)) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.cd_back),
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
                    PhotoAnimationRoute()
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
                        FakeDataGeneratorRoute()
                    }
                }
            }
        }

        composable(Routes.MeasurementAdd) {
            MeasurementEditRoute(
                onFinished = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.MeasurementEdit,
            arguments = listOf(
                navArgument(Routes.MeasurementEditIdArg) { type = NavType.LongType },
            ),
        ) {
            MeasurementEditRoute(
                onFinished = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }
    }
}
