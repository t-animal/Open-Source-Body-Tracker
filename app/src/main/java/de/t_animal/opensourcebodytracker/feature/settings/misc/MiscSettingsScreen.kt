package de.t_animal.opensourcebodytracker.feature.settings.misc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.PhotoQuality
import de.t_animal.opensourcebodytracker.core.model.UnitSystem
import de.t_animal.opensourcebodytracker.feature.settings.components.PhotoQualitySelector
import de.t_animal.opensourcebodytracker.feature.settings.components.UnitSystemSelector
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@Composable
fun MiscSettingsRoute(
    onNavigateBack: () -> Unit,
) {
    val vm: MiscSettingsViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    when (val uiState = uiState) {
        is MiscSettingsUiState.Loading -> {}
        is MiscSettingsUiState.Loaded -> MiscSettingsScreen(
            onNavigateBack = onNavigateBack,
            uiState = uiState,
            onUnitSystemChanged = vm::onUnitSystemChanged,
            onPhotoQualityChanged = vm::onPhotoQualityChanged,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiscSettingsScreen(
    onNavigateBack: () -> Unit,
    uiState: MiscSettingsUiState.Loaded,
    onUnitSystemChanged: (UnitSystem) -> Unit,
    onPhotoQualityChanged: (PhotoQuality) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_misc_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
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
                    text = stringResource(R.string.settings_misc_description),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }

            item {
                UnitSystemSelector(
                    unitSystem = uiState.unitSystem,
                    onUnitSystemChanged = onUnitSystemChanged,
                )
            }

            item {
                PhotoQualitySelector(
                    photoQuality = uiState.photoQuality,
                    onPhotoQualityChanged = onPhotoQualityChanged,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MiscSettingsScreenPreview() {
    BodyTrackerTheme {
        MiscSettingsScreen(
            onNavigateBack = {},
            uiState = MiscSettingsUiState.Loaded(
                unitSystem = UnitSystem.Metric,
                photoQuality = PhotoQuality.High,
            ),
            onUnitSystemChanged = {},
            onPhotoQualityChanged = {},
        )
    }
}
