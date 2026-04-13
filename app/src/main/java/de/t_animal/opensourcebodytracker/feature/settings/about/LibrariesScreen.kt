package de.t_animal.opensourcebodytracker.feature.settings.about

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.ui.components.SecondaryScreenScaffold
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@Composable
fun LibrariesRoute(
    onNavigateBack: () -> Unit,
) {
    LibrariesScreen(onNavigateBack = onNavigateBack)
}

@Composable
fun LibrariesScreen(
    onNavigateBack: () -> Unit,
) {
    val libraries by produceLibraries(R.raw.aboutlibraries)
    SecondaryScreenScaffold(
        title = stringResource(R.string.about_libraries_title),
        onNavigateBack = onNavigateBack,
    ) {
        LibrariesContainer(libraries)
    }
}

@Preview(showBackground = true)
@Composable
private fun LibrariesScreenPreview() {
    BodyTrackerTheme {
        LibrariesScreen(onNavigateBack = {})
    }
}
