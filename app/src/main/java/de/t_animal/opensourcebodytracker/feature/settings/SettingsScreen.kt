package de.t_animal.opensourcebodytracker.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@Composable
fun SettingsRoute(
    onNavigateBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenMeasurementsAndAnalysis: () -> Unit,
    onOpenMeasurementVisibility: () -> Unit,
    onOpenReminders: () -> Unit,
    onOpenExport: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    SettingsScreen(
        onNavigateBack = onNavigateBack,
        onOpenProfile = onOpenProfile,
        onOpenMeasurementsAndAnalysis = onOpenMeasurementsAndAnalysis,
        onOpenMeasurementVisibility = onOpenMeasurementVisibility,
        onOpenReminders = onOpenReminders,
        onOpenExport = onOpenExport,
        onOpenAbout = onOpenAbout,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenMeasurementsAndAnalysis: () -> Unit,
    onOpenMeasurementVisibility: () -> Unit,
    onOpenReminders: () -> Unit,
    onOpenExport: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item {
                Text(
                    text = "Configure your profile, measurements, analysis, export, and app information.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }

            item {
                SettingsNavigationItem(
                    title = "Profile",
                    onClick = onOpenProfile,
                )
            }
            item {
                SettingsNavigationItem(
                    title = "Measurements & Analysis",
                    onClick = onOpenMeasurementsAndAnalysis,
                )
            }
            item {
                SettingsNavigationItem(
                    title = "Measurement Visibility",
                    onClick = onOpenMeasurementVisibility,
                )
            }
            item {
                SettingsNavigationItem(
                    title = "Reminders",
                    onClick = onOpenReminders,
                )
            }
            item {
                SettingsNavigationItem(
                    title = "Export",
                    onClick = onOpenExport,
                )
            }
            item {
                SettingsNavigationItem(
                    title = "About",
                    onClick = onOpenAbout,
                )
            }
        }
    }
}

@Composable
private fun SettingsNavigationItem(
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    BodyTrackerTheme {
        SettingsScreen(
            onNavigateBack = {},
            onOpenProfile = {},
            onOpenMeasurementsAndAnalysis = {},
            onOpenMeasurementVisibility = {},
            onOpenReminders = {},
            onOpenExport = {},
            onOpenAbout = {},
        )
    }
}
