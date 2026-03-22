package de.t_animal.opensourcebodytracker.feature.photos.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotosItemUiModel

@Composable
fun CompareSelectionBottomBar(
    selectedItems: List<PhotosItemUiModel>,
    compareEnabled: Boolean,
    onCompareClicked: () -> Unit,
) {
    if (selectedItems.isEmpty()) {
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
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(modifier = Modifier.weight(1f)) {
                selectedItems.forEachIndexed { index, item ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    }
                    AsyncImage(
                        model = item.photoFile,
                        contentDescription = stringResource(R.string.cd_selected_thumbnail),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    )
                }
            }
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            Button(
                onClick = onCompareClicked,
                enabled = compareEnabled,
            ) {
                Text(stringResource(R.string.photos_button_compare))
            }
        }
    }
}
