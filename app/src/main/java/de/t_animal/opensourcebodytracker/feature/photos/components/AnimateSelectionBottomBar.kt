package de.t_animal.opensourcebodytracker.feature.photos.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.R

@Composable
fun AnimateSelectionBottomBar(
    selectedCount: Int,
    playEnabled: Boolean,
    onPlayClicked: () -> Unit,
) {
    if (selectedCount <= 0) {
        return
    }

    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = pluralStringResource(R.plurals.photos_selected_count, selectedCount, selectedCount),
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(
                onClick = onPlayClicked,
                enabled = playEnabled,
            ) {
                Text(stringResource(R.string.photos_button_play))
            }
        }
    }
}
