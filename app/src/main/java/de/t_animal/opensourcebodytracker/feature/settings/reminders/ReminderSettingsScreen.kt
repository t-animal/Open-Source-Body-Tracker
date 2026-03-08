package de.t_animal.opensourcebodytracker.feature.settings.reminders

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.app.TimePickerDialog
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.t_animal.opensourcebodytracker.core.notifications.ReminderAlarmScheduler
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
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
    settingsRepository: SettingsRepository,
    reminderAlarmScheduler: ReminderAlarmScheduler,
    onNavigateBack: () -> Unit,
) {
    val vm: ReminderSettingsViewModel = viewModel(
        factory = ReminderSettingsViewModelFactory(
            settingsRepository = settingsRepository,
            reminderAlarmScheduler = reminderAlarmScheduler,
        ),
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val latestOnSaveClicked by rememberUpdatedState(vm::onSaveClicked)
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        latestOnSaveClicked()
    }

    val onSaveRequested = onSaveRequested@{
        if (!state.enabled) {
            return@onSaveRequested
        }

        if (shouldRequestNotificationPermission(context)) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
            TopAppBar(
                title = { Text("Reminder Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                }
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
                Button(
                    onClick = onSaveClicked,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Save")
                }

            }
        }
    }
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
