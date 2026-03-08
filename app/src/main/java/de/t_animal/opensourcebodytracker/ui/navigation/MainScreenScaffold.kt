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
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenReminders: () -> Unit,
    onTriggerReminder: () -> Unit,
    onOpenFakeDataGenerator: (() -> Unit)? = null,
    floatingActionButton: @Composable () -> Unit = {},
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
                            text = { Text("Profile") },
                            onClick = {
                                overflowExpanded = false
                                onOpenProfile()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = {
                                overflowExpanded = false
                                onOpenSettings()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Reminders") },
                            onClick = {
                                overflowExpanded = false
                                onOpenReminders()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Trigger Reminder") },
                            onClick = {
                                overflowExpanded = false
                                onTriggerReminder()
                            },
                        )
                        if (onOpenFakeDataGenerator != null) {
                            DropdownMenuItem(
                                text = { Text("Fake data generator") },
                                onClick = {
                                    overflowExpanded = false
                                    onOpenFakeDataGenerator()
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
        floatingActionButton = floatingActionButton,
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
            onOpenProfile = {},
            onOpenSettings = {},
            onOpenReminders = {},
            onTriggerReminder = {},
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
