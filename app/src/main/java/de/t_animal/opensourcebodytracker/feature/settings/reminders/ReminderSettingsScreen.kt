package de.t_animal.opensourcebodytracker.feature.settings.reminders

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val weekdayDisplayOrder = listOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
    DayOfWeek.SUNDAY,
)

@Composable
fun ReminderSettingsRoute(
    mode: ReminderMode,
    onNavigateBack: () -> Unit,
) {
    val vm = hiltViewModel<ReminderSettingsViewModel, ReminderSettingsViewModel.Factory> { it.create(mode) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val latestOnSaveClicked by rememberUpdatedState(vm::onSaveClicked)
    var showPermissionDeniedAlert by remember { mutableStateOf(false) }
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            latestOnSaveClicked()
            return@rememberLauncherForActivityResult
        }

        vm.onPermissionDeniedWhileSaving()
        showPermissionDeniedAlert = true
    }

    val onSaveRequested = onSaveRequested@{
        if (state.enabled && shouldRequestNotificationPermission(context)) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return@onSaveRequested
        }

        vm.onSaveClicked()
    }

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                ReminderSettingsEvent.Saved -> onNavigateBack()
            }
        }
    }

    ReminderSettingsScreen(
        state = state,
        onEnabledChanged = vm::onEnabledChanged,
        onWeekdayToggled = vm::onWeekdayToggled,
        onTimeChanged = vm::onTimeChanged,
        onSaveClicked = onSaveRequested,
        onBackClicked = onNavigateBack,
    )

    if (showPermissionDeniedAlert) {
        PermissionDeniedAlert(
            onDismiss = { showPermissionDeniedAlert = false },
        )
    }
}

@Composable
private fun PermissionDeniedAlert(
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notification Permission Required") },
        text = {
            Text(
                "Notification permission is required to keep reminders enabled." +
                    "Reminders have been disabled for now." + 
                    "To enable reminders, please grant notification permission in the app settings.",
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        },
    )
}

private fun shouldRequestNotificationPermission(
    context: Context,
): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return false
    }

    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
    ) != PackageManager.PERMISSION_GRANTED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingsScreen(
    state: ReminderSettingsUiState,
    onEnabledChanged: (Boolean) -> Unit,
    onWeekdayToggled: (DayOfWeek) -> Unit,
    onTimeChanged: (LocalTime) -> Unit,
    onSaveClicked: () -> Unit,
    onBackClicked: () -> Unit,
) {
    val context = LocalContext.current
    val latestOnTimeChanged by rememberUpdatedState(onTimeChanged)

    Scaffold(
        topBar = {
            ReminderSettingsTopAppBar(
                mode = state.mode,
                onBackClicked = onBackClicked,
            )
        },
    ) { contentPadding ->
        if (state.isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp),
        ) {
            Text(
                "Set up reminders to log your measurements regularly " +
                    "and stay on track with your goals. You will receive a notification " +
                    "at the selected time on the chosen weekdays.", style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Enable Reminders",
                    style = MaterialTheme.typography.titleMedium,
                )
                Switch(
                    checked = state.enabled,
                    onCheckedChange = onEnabledChanged,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Weekdays",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(60.dp),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(weekdayDisplayOrder) { day ->
                    FilterChip(
                        selected = day in state.weekdays,
                        onClick = { onWeekdayToggled(day) },
                        label = {
                            Text(
                                day.name.substring(0, 2),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        enabled = state.enabled,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Time",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            latestOnTimeChanged(LocalTime.of(hourOfDay, minute))
                        },
                        state.time.hour,
                        state.time.minute,
                        true,
                    ).show()
                },
                enabled = state.enabled,
            ) {
                Text(formatReminderTime(state.time))
            }

            val error = state.errorMessage
            if (!error.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            ReminderSettingsActionButtons(
                mode = state.mode,
                onBackClicked = onBackClicked,
                onSaveClicked = onSaveClicked,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderSettingsTopAppBar(
    mode: ReminderMode,
    onBackClicked: () -> Unit,
) {
    TopAppBar(
        title = { Text(mode.titleText()) },
        navigationIcon = {
            if (mode.showsBackNavigation()) {
                IconButton(onClick = onBackClicked) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            }
        },
    )
}

@Composable
private fun ReminderSettingsActionButtons(
    mode: ReminderMode,
    onBackClicked: () -> Unit,
    onSaveClicked: () -> Unit,
) {
    when (mode) {
        ReminderMode.Onboarding -> {
            ReminderSaveButton(
                label = mode.primaryButtonText(),
                onSaveClicked = onSaveClicked,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        ReminderMode.Settings -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onBackClicked,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancel")
                }
                ReminderSaveButton(
                    label = mode.primaryButtonText(),
                    onSaveClicked = onSaveClicked,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ReminderSaveButton(
    label: String,
    onSaveClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onSaveClicked,
        modifier = modifier,
    ) {
        Text(label)
    }
}

private fun ReminderMode.showsBackNavigation(): Boolean = this == ReminderMode.Settings

private fun ReminderMode.titleText(): String = when (this) {
    ReminderMode.Onboarding -> "Reminder Setup"
    ReminderMode.Settings -> "Reminder Settings"
}

private fun ReminderMode.primaryButtonText(): String = when (this) {
    ReminderMode.Onboarding -> "Finish"
    ReminderMode.Settings -> "Save"
}

private fun formatReminderTime(time: LocalTime): String {
    return time.format(DateTimeFormatter.ofPattern("HH:mm"))
}

@Preview(showBackground = true)
@Composable
private fun ReminderSettingsScreenPreview() {
    BodyTrackerTheme {
        ReminderSettingsScreen(
            state = ReminderSettingsUiState(
                mode = ReminderMode.Settings,
                isLoading = false,
                enabled = true,
                weekdays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                time = LocalTime.of(20, 0),
            ),
            onEnabledChanged = {},
            onWeekdayToggled = {},
            onTimeChanged = {},
            onSaveClicked = {},
            onBackClicked = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderSettingsScreenOnboardingPreview() {
    BodyTrackerTheme {
        ReminderSettingsScreen(
            state = ReminderSettingsUiState(
                mode = ReminderMode.Onboarding,
                isLoading = false,
                enabled = true,
                weekdays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                time = LocalTime.of(20, 0),
            ),
            onEnabledChanged = {},
            onWeekdayToggled = {},
            onTimeChanged = {},
            onSaveClicked = {},
            onBackClicked = {},
        )
    }
}
