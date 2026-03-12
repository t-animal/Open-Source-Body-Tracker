package de.t_animal.opensourcebodytracker.ui.navigation

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenScaffold(
    selectedDestination: MainDestination,
    onMainDestinationSelected: (MainDestination) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onTriggerReminder: (() -> Unit)? = null,
    onResetApp: (() -> Unit)? = null,
    onOpenFakeDataGenerator: (() -> Unit)? = null,
    onScheduleExportIn2Minutes: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    var overflowExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedDestination.title) },
                modifier = Modifier.shadow(4.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                actions = {
                    IconButton(onClick = { overflowExpanded = true }) {
                        Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = overflowExpanded,
                        onDismissRequest = { overflowExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = {
                                overflowExpanded = false
                                onOpenSettings()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("About") },
                            onClick = {
                                overflowExpanded = false
                                onOpenAbout()
                            },
                        )
                        if (onTriggerReminder != null) {
                            DropdownMenuItem(
                                text = { Text("Trigger Reminder") },
                                onClick = {
                                    overflowExpanded = false
                                    onTriggerReminder()
                                },
                            )
                        }
                        if (onOpenFakeDataGenerator != null) {
                            DropdownMenuItem(
                                text = { Text("Fake data generator") },
                                onClick = {
                                    overflowExpanded = false
                                    onOpenFakeDataGenerator()
                                },
                            )
                        }
                        if (onResetApp != null) {
                            DropdownMenuItem(
                                text = { Text("Reset App Data") },
                                onClick = {
                                    overflowExpanded = false
                                    onResetApp()
                                },
                            )
                        }
                        if (onScheduleExportIn2Minutes != null) {
                            DropdownMenuItem(
                                text = { Text("Schedule Export in 2 min") },
                                onClick = {
                                    overflowExpanded = false
                                    onScheduleExportIn2Minutes()
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
                        label = { Icon(imageVector = destination.icon, contentDescription = destination.title) },
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
