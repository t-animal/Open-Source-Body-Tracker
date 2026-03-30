package de.t_animal.opensourcebodytracker.feature.settings.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.BuildConfig
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.ui.components.SecondaryScreenScaffold
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme


@Composable
fun AboutRoute(
    onNavigateBack: () -> Unit,
) {
    AboutScreen(
        onNavigateBack = onNavigateBack,
        projectUrl = BuildConfig.ABOUT_PROJECT_URL,
        contactEmail = BuildConfig.ABOUT_CONTACT_EMAIL,
    )
}

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    projectUrl: String,
    contactEmail: String,
) {
    val uriHandler = LocalUriHandler.current
    val normalizedProjectUrl = if (projectUrl.startsWith("http://") || projectUrl.startsWith("https://")) {
        projectUrl
    } else {
        "https://$projectUrl"
    }

    SecondaryScreenScaffold(
        title = stringResource(R.string.about_title),
        onNavigateBack = onNavigateBack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.about_app_name),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = stringResource(R.string.about_tagline),
                style = MaterialTheme.typography.bodyMedium,
            )

            Text(
                text = stringResource(R.string.about_section_project),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            OutlinedButton(
                onClick = { uriHandler.openUri(normalizedProjectUrl) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.about_button_github))
            }

            Text(
                text = stringResource(R.string.about_section_contact),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            OutlinedButton(
                onClick = { uriHandler.openUri("mailto:$contactEmail") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(contactEmail)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutScreenPreview() {
    BodyTrackerTheme {
        AboutScreen(
            onNavigateBack = {},
            projectUrl = "example.com",
            contactEmail = "contact@example.com",
        )
    }
}
