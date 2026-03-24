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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

data class OnboardingStartUiState(
    val isBusy: Boolean = false,
    val hasError: Boolean = false,
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun OnboardingStartScreen(
    state: OnboardingStartUiState,
    onCreateProfileClicked: () -> Unit,
    onTryDemoDataClicked: () -> Unit,
    onImportBackupClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.onboarding_title)) })
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
                text = stringResource(R.string.onboarding_question),
                style = MaterialTheme.typography.titleLarge,
            )

            Text(
                text = stringResource(R.string.onboarding_description),
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
                    Text(stringResource(R.string.onboarding_button_demo))
                }
            }

            OutlinedButton(
                onClick = onImportBackupClicked,
                enabled = !state.isBusy,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            ) {
                Text(stringResource(R.string.onboarding_button_import))
            }

            Button(
                onClick = onCreateProfileClicked,
                enabled = !state.isBusy,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            ) {
                Text(stringResource(R.string.onboarding_button_create_profile))
            }

            if (state.hasError) {
                Text(
                    text = stringResource(R.string.onboarding_error_demo_failed),
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
            onImportBackupClicked = {},
        )
    }
}
