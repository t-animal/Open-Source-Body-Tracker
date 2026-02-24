package de.t_animal.opensourcebodytracker.feature.debug

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.domain.measurements.GenerateFakeMeasurementsUseCase

@Composable
fun FakeDataGeneratorRoute(
    profileRepository: ProfileRepository,
    generateFakeMeasurementsUseCase: GenerateFakeMeasurementsUseCase,
) {
    val vm: FakeDataGeneratorViewModel = viewModel(
        factory = FakeDataGeneratorViewModelFactory(
            profileRepository = profileRepository,
            generateFakeMeasurementsUseCase = generateFakeMeasurementsUseCase,
        ),
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    FakeDataGeneratorScreen(
        isGenerating = state.isGenerating,
        onGenerateClicked = vm::onGenerateClicked,
    )
}

@Composable
fun FakeDataGeneratorScreen(
    isGenerating: Boolean,
    onGenerateClicked: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Button(
            onClick = onGenerateClicked,
            enabled = !isGenerating,
        ) {
            if (isGenerating) {
                CircularProgressIndicator()
            } else {
                Text("Generate fake data")
            }
        }
    }
}