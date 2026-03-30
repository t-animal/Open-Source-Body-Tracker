package de.t_animal.opensourcebodytracker.feature.settings.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.ProfileValidationError
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UnitSystem
import de.t_animal.opensourcebodytracker.feature.settings.components.ProfileFormSection
import de.t_animal.opensourcebodytracker.feature.settings.components.UnitSystemSelector
import de.t_animal.opensourcebodytracker.ui.components.SecondaryScreenScaffold
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@Composable
fun ProfileRoute(
    mode: ProfileMode,
    onFinished: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
) {
    val vm = hiltViewModel<ProfileViewModel, ProfileViewModel.Factory> { it.create(mode) }
    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                ProfileEvent.Saved -> onFinished()
            }
        }
    }

    val state by vm.uiState.collectAsStateWithLifecycle()

    when (val state = state) {
        is ProfileUiState.Loading -> {}
        is ProfileUiState.Loaded -> ProfileScreen(
            state = state,
            onNavigateBack = onNavigateBack,
            onSexChanged = vm::onSexChanged,
            onDateOfBirthChanged = vm::onDateOfBirthChanged,
            onHeightCmChanged = vm::onHeightCmChanged,
            onUnitSystemChanged = vm::onUnitSystemChanged,
            onSaveClicked = vm::onSaveClicked,
        )
    }
}

@Composable
fun ProfileScreen(
    state: ProfileUiState.Loaded,
    onNavigateBack: (() -> Unit)? = null,
    onSexChanged: (Sex) -> Unit,
    onDateOfBirthChanged: (String) -> Unit,
    onHeightCmChanged: (String) -> Unit,
    onUnitSystemChanged: (UnitSystem) -> Unit,
    onSaveClicked: () -> Unit,
) {
    SecondaryScreenScaffold(
        title = stringResource(
            if (state.mode == ProfileMode.Onboarding) R.string.profile_title_onboarding
            else R.string.profile_title_settings,
        ),
        onNavigateBack = onNavigateBack ?: {},
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            if (state.mode == ProfileMode.Onboarding) {
                UnitSystemSelector(
                    unitSystem = state.unitSystem,
                    onUnitSystemChanged = onUnitSystemChanged,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.settings_item_profile_subheading),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            ProfileFormSection(
                sex = state.sex,
                dateOfBirthText = state.dateOfBirthText,
                heightCmText = state.heightCmText,
                unitSystem = state.unitSystem,
                onSexChanged = onSexChanged,
                onDateOfBirthChanged = onDateOfBirthChanged,
                onHeightCmChanged = onHeightCmChanged,
            )

            Spacer(modifier = Modifier.height(16.dp))

            val errorMessage = when (state.validationError) {
                ProfileValidationError.MissingSex -> stringResource(R.string.profile_error_missing_sex)
                ProfileValidationError.InvalidDateOfBirth -> stringResource(R.string.profile_error_invalid_dob)
                ProfileValidationError.InvalidHeight -> stringResource(R.string.profile_error_invalid_height)
                null -> null
            }
            if (!errorMessage.isNullOrBlank()) {
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(onClick = onSaveClicked) {
                Text(
                    text = stringResource(
                        if (state.mode == ProfileMode.Onboarding) R.string.common_continue
                        else R.string.common_save,
                    ),
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
            state = ProfileUiState.Loaded(
                mode = ProfileMode.Onboarding,
                sex = Sex.Male,
                dateOfBirthText = "1990-01-02",
                heightCmText = "180",
                unitSystem = UnitSystem.Metric,
                validationError = null,
            ),
            onNavigateBack = {},
            onSexChanged = {},
            onDateOfBirthChanged = {},
            onHeightCmChanged = {},
            onUnitSystemChanged = {},
            onSaveClicked = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview_Error() {
    BodyTrackerTheme {
        ProfileScreen(
            state = ProfileUiState.Loaded(
                mode = ProfileMode.Settings,
                sex = null,
                dateOfBirthText = "",
                heightCmText = "",
                unitSystem = UnitSystem.Metric,
                validationError = ProfileValidationError.MissingSex,
            ),
            onNavigateBack = {},
            onSexChanged = {},
            onDateOfBirthChanged = {},
            onHeightCmChanged = {},
            onUnitSystemChanged = {},
            onSaveClicked = {},
        )
    }
}
