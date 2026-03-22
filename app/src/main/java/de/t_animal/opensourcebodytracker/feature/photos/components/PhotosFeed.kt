package de.t_animal.opensourcebodytracker.feature.photos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotosItemUiModel
import de.t_animal.opensourcebodytracker.ui.components.formatEpochMillisToLocalizedNumericDate

@Composable
fun PhotosFeed(
    items: List<PhotosItemUiModel>,
    selectedIds: Set<Long>,
    isSelectionMode: Boolean,
    listBottomPadding: Dp,
    onPhotoClicked: (Long) -> Unit,
) {
    if (items.isEmpty()) {
        Text(
            text = stringResource(R.string.photos_empty),
            modifier = Modifier.padding(24.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 8.dp,
                top = 8.dp,
                end = 8.dp,
                bottom = listBottomPadding,
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = items,
                key = { it.measurementId },
            ) { item ->
                PhotoTile(
                    item = item,
                    isSelected = selectedIds.contains(item.measurementId),
                    isSelectionMode = isSelectionMode,
                    onPhotoClicked = onPhotoClicked,
                    modifier = Modifier.fillMaxWidth(),
                    dateFontSize = 10.sp,
                    selectedIndicatorSize = 22.dp,
                    selectionPadding = 6.dp,
                )
            }
        }
    }
}

@Composable
private fun PhotoTile(
    item: PhotosItemUiModel,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onPhotoClicked: (Long) -> Unit,
    modifier: Modifier,
    dateFontSize: androidx.compose.ui.unit.TextUnit,
    selectedIndicatorSize: Dp,
    selectionPadding: Dp,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                onPhotoClicked(item.measurementId)
            },
    ) {
        AsyncImage(
            model = item.photoFile,
            contentDescription = stringResource(R.string.cd_progress_photo),
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = formatEpochMillisToLocalizedNumericDate(item.dateEpochMillis),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(6.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp),
                )
                .padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = dateFontSize,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
        if (isSelectionMode && isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(selectionPadding)
                    .size(selectedIndicatorSize)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = stringResource(R.string.cd_selected),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}
