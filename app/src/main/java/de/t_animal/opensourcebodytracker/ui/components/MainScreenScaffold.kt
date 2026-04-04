package de.t_animal.opensourcebodytracker.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.ui.navigation.MainDestination
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

data class DebugCallbacks(
    val onTriggerReminder: (() -> Unit)? = null,
    val onResetApp: (() -> Unit)? = null,
    val onOpenFakeDataGenerator: (() -> Unit)? = null,
    val onScheduleExportIn2Minutes: (() -> Unit)? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenScaffold(
    selectedDestination: MainDestination,
    onMainDestinationSelected: (MainDestination) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    debugCallbacks: DebugCallbacks? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    var overflowExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(selectedDestination.titleResId)) },
                modifier = Modifier.shadow(4.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                actions = {
                    IconButton(onClick = { overflowExpanded = true }) {
                        Icon(imageVector = Icons.Filled.MoreVert, contentDescription = stringResource(R.string.cd_more))
                    }
                    DropdownMenu(
                        expanded = overflowExpanded,
                        onDismissRequest = { overflowExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_settings)) },
                            onClick = {
                                overflowExpanded = false
                                onOpenSettings()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_about)) },
                            onClick = {
                                overflowExpanded = false
                                onOpenAbout()
                            },
                        )
                        if (debugCallbacks?.onTriggerReminder != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_trigger_reminder)) },
                                onClick = {
                                    overflowExpanded = false
                                    debugCallbacks.onTriggerReminder()
                                },
                            )
                        }
                        if (debugCallbacks?.onOpenFakeDataGenerator != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_fake_data)) },
                                onClick = {
                                    overflowExpanded = false
                                    debugCallbacks.onOpenFakeDataGenerator()
                                },
                            )
                        }
                        if (debugCallbacks?.onResetApp != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_reset_app)) },
                                onClick = {
                                    overflowExpanded = false
                                    debugCallbacks.onResetApp()
                                },
                            )
                        }
                        if (debugCallbacks?.onScheduleExportIn2Minutes != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_schedule_export)) },
                                onClick = {
                                    overflowExpanded = false
                                    debugCallbacks.onScheduleExportIn2Minutes()
                                },
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                MainDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = selectedDestination == destination,
                        onClick = { onMainDestinationSelected(destination) },
                        icon = {},
                        label = { Icon(imageVector = destination.icon, contentDescription = stringResource(destination.titleResId)) },
                    )
                }
            }
        },
        content = content,
    )
}

@Preview(showBackground = true)
@Composable
private fun MainScreenScaffoldPreview() {
    BodyTrackerTheme {
        MainScreenScaffold(
            selectedDestination = MainDestination.Measurements,
            onMainDestinationSelected = {},
            onOpenSettings = {},
            onOpenAbout = {},
        ) { contentPadding ->
            Text(
                text = "Preview Content",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            )
        }
    }
}
