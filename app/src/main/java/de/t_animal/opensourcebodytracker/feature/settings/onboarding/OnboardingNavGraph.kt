package de.t_animal.opensourcebodytracker.feature.settings.onboarding

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import de.t_animal.opensourcebodytracker.feature.settings.measurements.ChooseMeasurementSettingsScreen
import de.t_animal.opensourcebodytracker.feature.settings.measurements.ChooseMeasurementSettingsUiState
import de.t_animal.opensourcebodytracker.feature.settings.measurements.MeasurementSettingsMode
import de.t_animal.opensourcebodytracker.feature.settings.profile.ProfileMode
import de.t_animal.opensourcebodytracker.feature.settings.profile.ProfileScreen
import de.t_animal.opensourcebodytracker.feature.settings.profile.ProfileUiState
import de.t_animal.opensourcebodytracker.feature.settings.reminders.PermissionDeniedAlert
import de.t_animal.opensourcebodytracker.feature.settings.reminders.ReminderMode
import de.t_animal.opensourcebodytracker.feature.settings.reminders.ReminderSettingsScreen
import de.t_animal.opensourcebodytracker.feature.settings.reminders.ReminderSettingsUiState
import de.t_animal.opensourcebodytracker.feature.settings.reminders.shouldRequestNotificationPermission
import de.t_animal.opensourcebodytracker.ui.navigation.Routes

fun NavGraphBuilder.onboardingNavGraph(
    navController: NavController,
    onDemoModeCompleted: () -> Unit,
    onImportBackupClicked: () -> Unit,
    onOnboardingCompleted: () -> Unit,
) {
    navigation(
        startDestination = Routes.OnboardingStart,
        route = Routes.OnboardingGraph,
    ) {
        composable(Routes.OnboardingStart) { backStackEntry ->
            OnboardingStartRoute(
                navController = navController,
                backStackEntry = backStackEntry,
                onDemoModeCompleted = onDemoModeCompleted,
                onImportBackupClicked = onImportBackupClicked,
            )
        }

        composable(Routes.OnboardingProfile) { backStackEntry ->
            OnboardingProfileRoute(
                navController = navController,
                backStackEntry = backStackEntry,
            )
        }

        composable(Routes.OnboardingAnalysis) { backStackEntry ->
            OnboardingAnalysisRoute(
                navController = navController,
                backStackEntry = backStackEntry,
            )
        }

        composable(Routes.OnboardingReminders) { backStackEntry ->
            OnboardingRemindersRoute(
                navController = navController,
                backStackEntry = backStackEntry,
                onOnboardingCompleted = onOnboardingCompleted,
            )
        }
    }
}

@Composable
private fun onboardingViewModel(
    navController: NavController,
    backStackEntry: NavBackStackEntry,
): OnboardingViewModel {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(Routes.OnboardingGraph)
    }
    return hiltViewModel(parentEntry)
}


@Composable
private fun OnboardingStartRoute(
    navController: NavController,
    backStackEntry: NavBackStackEntry,
    onDemoModeCompleted: () -> Unit,
    onImportBackupClicked: () -> Unit,
) {
    val vm = onboardingViewModel(navController, backStackEntry)
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            if (event is OnboardingEvent.DemoModeCompleted) {
                onDemoModeCompleted()
            }
        }
    }

    OnboardingStartScreen(
        state = OnboardingStartUiState(
            isBusy = state.start.isDemoModeBusy,
            hasError = state.start.demoModeError,
        ),
        onCreateProfileClicked = {
            navController.navigate(Routes.OnboardingProfile) {
                launchSingleTop = true
            }
        },
        onTryDemoDataClicked = vm::onTryDemoDataClicked,
        onImportBackupClicked = onImportBackupClicked,
    )
}

@Composable
private fun OnboardingProfileRoute(
    navController: NavController,
    backStackEntry: NavBackStackEntry,
) {
    val vm = onboardingViewModel(navController, backStackEntry)
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            if (event is OnboardingEvent.ProfileValid) {
                navController.navigate(Routes.OnboardingAnalysis) {
                    launchSingleTop = true
                }
            }
        }
    }

    ProfileScreen(
        state = ProfileUiState(
            mode = ProfileMode.Onboarding,
            sex = state.profile.sex,
            dateOfBirthText = state.profile.dateOfBirthText,
            heightCmText = state.profile.heightCmText,
            unitSystem = state.profile.unitSystem,
            validationError = state.profile.validationError,
        ),
        onSexChanged = vm::onSexChanged,
        onDateOfBirthChanged = vm::onDateOfBirthChanged,
        onHeightCmChanged = vm::onHeightCmChanged,
        onUnitSystemChanged = vm::onUnitSystemChanged,
        onSaveClicked = vm::validateProfile,
    )
}

@Composable
private fun OnboardingAnalysisRoute(
    navController: NavController,
    backStackEntry: NavBackStackEntry,
) {
    val vm = onboardingViewModel(navController, backStackEntry)
    val state by vm.uiState.collectAsStateWithLifecycle()

    ChooseMeasurementSettingsScreen(
        state = ChooseMeasurementSettingsUiState(
            mode = MeasurementSettingsMode.Onboarding,
            isLoading = state.analysis.isLoading,
            isSaving = state.isSaving,
            settings = state.analysis.settings,
            requiredMeasurements = state.analysis.requiredMeasurements,
            measurementToAnalysisMethods = state.analysis.measurementToAnalysisMethods,
            hasError = state.saveError,
        ),
        onBmiEnabledChanged = vm::onBmiEnabledChanged,
        onNavyBodyFatEnabledChanged = vm::onNavyBodyFatEnabledChanged,
        onSkinfoldBodyFatEnabledChanged = vm::onSkinfoldBodyFatEnabledChanged,
        onWaistHipRatioEnabledChanged = vm::onWaistHipRatioEnabledChanged,
        onWaistHeightRatioEnabledChanged = vm::onWaistHeightRatioEnabledChanged,
        onMeasurementEnabledChanged = vm::onMeasurementEnabledChanged,
        onContinueClicked = {
            navController.navigate(Routes.OnboardingReminders) {
                launchSingleTop = true
            }
        },
    )
}

@Composable
private fun OnboardingRemindersRoute(
    navController: NavController,
    backStackEntry: NavBackStackEntry,
    onOnboardingCompleted: () -> Unit,
) {
    val vm = onboardingViewModel(navController, backStackEntry)
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showPermissionDeniedAlert by remember { mutableStateOf(false) }
    val latestOnFinish by rememberUpdatedState(vm::validateAndFinish)
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            latestOnFinish()
            return@rememberLauncherForActivityResult
        }
        vm.onPermissionDeniedWhileSaving()
        showPermissionDeniedAlert = true
    }

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            if (event is OnboardingEvent.OnboardingCompleted) {
                onOnboardingCompleted()
            }
        }
    }

    val onSaveRequested = onSaveRequested@{
        if (state.reminders.enabled && shouldRequestNotificationPermission(context)) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return@onSaveRequested
        }
        vm.validateAndFinish()
    }

    ReminderSettingsScreen(
        state = ReminderSettingsUiState(
            mode = ReminderMode.Onboarding,
            isLoading = false,
            enabled = state.reminders.enabled,
            weekdays = state.reminders.weekdays,
            time = state.reminders.time,
            validationError = state.reminders.validationError,
        ),
        onEnabledChanged = vm::onReminderEnabledChanged,
        onWeekdayToggled = vm::onWeekdayToggled,
        onTimeChanged = vm::onTimeChanged,
        onSaveClicked = onSaveRequested,
        onBackClicked = onOnboardingCompleted,
    )

    if (showPermissionDeniedAlert) {
        PermissionDeniedAlert(
            onDismiss = { showPermissionDeniedAlert = false },
        )
    }
}