package de.t_animal.opensourcebodytracker.feature.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.domain.measurements.GenerateFakeMeasurementsUseCase
import de.t_animal.opensourcebodytracker.ui.components.DecimalNumberInputField

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
        leanBodyWeightKgText = state.leanBodyWeightKgText,
        minFatBodyWeightKgText = state.minFatBodyWeightKgText,
        maxFatBodyWeightKgText = state.maxFatBodyWeightKgText,
        inputError = state.inputError,
        isGenerating = state.isGenerating,
        onLeanBodyWeightChanged = vm::onLeanBodyWeightChanged,
        onMinFatBodyWeightChanged = vm::onMinFatBodyWeightChanged,
        onMaxFatBodyWeightChanged = vm::onMaxFatBodyWeightChanged,
        onGenerateClicked = vm::onGenerateClicked,
    )
}

@Composable
fun FakeDataGeneratorScreen(
    leanBodyWeightKgText: String,
    minFatBodyWeightKgText: String,
    maxFatBodyWeightKgText: String,
    inputError: String?,
    isGenerating: Boolean,
    onLeanBodyWeightChanged: (String) -> Unit,
    onMinFatBodyWeightChanged: (String) -> Unit,
    onMaxFatBodyWeightChanged: (String) -> Unit,
    onGenerateClicked: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DecimalNumberInputField(
            label = "Lean body weight (kg)",
            value = leanBodyWeightKgText,
            onValueChange = onLeanBodyWeightChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            imeAction = ImeAction.Next,
        )
        DecimalNumberInputField(
            label = "Min fat amount (kg)",
            value = minFatBodyWeightKgText,
            onValueChange = onMinFatBodyWeightChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            imeAction = ImeAction.Next,
        )
        DecimalNumberInputField(
            label = "Max fat amount (kg)",
            value = maxFatBodyWeightKgText,
            onValueChange = onMaxFatBodyWeightChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            imeAction = ImeAction.Done,
        )
        if (inputError != null) {
            Text(
                text = inputError,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp),
            )
        }
        Button(
            onClick = onGenerateClicked,
            enabled = !isGenerating,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            if (isGenerating) {
                CircularProgressIndicator()
            } else {
                Text("Generate fake data")
            }
        }
    }
}