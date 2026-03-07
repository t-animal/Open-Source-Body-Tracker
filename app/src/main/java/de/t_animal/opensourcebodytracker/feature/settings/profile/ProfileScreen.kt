package de.t_animal.opensourcebodytracker.feature.settings.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.metrics.DerivedMetricsDependencyResolver
import de.t_animal.opensourcebodytracker.feature.settings.components.ProfileFormSection
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@Composable
fun ProfileRoute(
    repository: ProfileRepository,
    settingsRepository: SettingsRepository,
    mode: ProfileMode,
    onFinished: () -> Unit,
) {
    val vm: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(
            repository = repository,
            settingsRepository = settingsRepository,
            dependencyResolver = DerivedMetricsDependencyResolver(),
            mode = mode,
        ),
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                ProfileEvent.Saved -> onFinished()
            }
        }
    }

    ProfileScreen(
        state = state,
        onSexChanged = vm::onSexChanged,
        onDateOfBirthChanged = vm::onDateOfBirthChanged,
        onHeightChanged = vm::onHeightChanged,
        onSaveClicked = vm::onSaveClicked,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onSexChanged: (Sex) -> Unit,
    onDateOfBirthChanged: (String) -> Unit,
    onHeightChanged: (String) -> Unit,
    onSaveClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (state.mode) {
                            ProfileMode.Onboarding -> "Profile Setup"
                            ProfileMode.Settings -> "Profile"
                        },
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            ProfileFormSection(
                sex = state.sex,
                dateOfBirthText = state.dateOfBirthText,
                heightCmText = state.heightCmText,
                onSexChanged = onSexChanged,
                onDateOfBirthChanged = onDateOfBirthChanged,
                onHeightChanged = onHeightChanged,
            )

            Spacer(modifier = Modifier.height(16.dp))

            val error = state.errorMessage
            if (!error.isNullOrBlank()) {
                Text(text = error, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(onClick = onSaveClicked) {
                Text(
                    text = when (state.mode) {
                        ProfileMode.Onboarding -> "Continue"
                        ProfileMode.Settings -> "Save"
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview_Onboarding() {
    BodyTrackerTheme {
        ProfileScreen(
            state = ProfileUiState(
                mode = ProfileMode.Onboarding,
                sex = Sex.Male,
                dateOfBirthText = "1990-01-02",
                heightCmText = "180",
            ),
            onSexChanged = {},
            onDateOfBirthChanged = {},
            onHeightChanged = {},
            onSaveClicked = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview_Error() {
    BodyTrackerTheme {
        ProfileScreen(
            state = ProfileUiState(
                mode = ProfileMode.Settings,
                sex = null,
                dateOfBirthText = "",
                heightCmText = "",
                errorMessage = "Please select a sex",
            ),
            onSexChanged = {},
            onDateOfBirthChanged = {},
            onHeightChanged = {},
            onSaveClicked = {},
        )
    }
}