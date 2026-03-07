package de.t_animal.opensourcebodytracker.feature.settings.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.measurements.GenerateFakeMeasurementsWithPhotosUseCase
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@Composable
fun OnboardingStartRoute(
    profileRepository: ProfileRepository,
    settingsRepository: SettingsRepository,
    generateFakeMeasurementsWithPhotosUseCase: GenerateFakeMeasurementsWithPhotosUseCase,
    onCreateProfileSelected: () -> Unit,
    onDemoModeCompleted: () -> Unit,
) {
    val vm: OnboardingStartViewModel = viewModel(
        factory = OnboardingStartViewModelFactory(
            profileRepository = profileRepository,
            settingsRepository = settingsRepository,
            generateFakeMeasurementsWithPhotosUseCase = generateFakeMeasurementsWithPhotosUseCase,
        ),
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                OnboardingStartEvent.DemoModeInitializationCompleted -> onDemoModeCompleted()
            }
        }
    }

    OnboardingStartScreen(
        state = state,
        onCreateProfileClicked = onCreateProfileSelected,
        onTryDemoDataClicked = vm::onTryDemoDataClicked,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun OnboardingStartScreen(
    state: OnboardingStartUiState,
    onCreateProfileClicked: () -> Unit,
    onTryDemoDataClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Initial Configuration") })
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "How do you want to start?",
                style = MaterialTheme.typography.titleLarge,
            )

            Text(
                text = "You can try the app with demo data or create your own profile.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )

            OutlinedButton(
                onClick = onTryDemoDataClicked,
                enabled = !state.isBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                } else {
                    Text("Try with Demo Data")
                }
            }

            Button(
                onClick = onCreateProfileClicked,
                enabled = !state.isBusy,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            ) {
                Text("Create Profile")
            }

            if (!state.errorMessage.isNullOrBlank()) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingStartScreenPreview() {
    BodyTrackerTheme {
        OnboardingStartScreen(
            state = OnboardingStartUiState(),
            onCreateProfileClicked = {},
            onTryDemoDataClicked = {},
        )
    }
}
