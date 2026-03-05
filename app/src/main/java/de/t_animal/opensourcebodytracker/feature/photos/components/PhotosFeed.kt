package de.t_animal.opensourcebodytracker.feature.photos.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
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
import coil.compose.AsyncImage
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotoMode
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotosItemUiModel
import de.t_animal.opensourcebodytracker.ui.components.formatEpochMillisToLocalizedNumericDate

@Composable
fun PhotosFeed(
    items: List<PhotosItemUiModel>,
    selectedIds: Set<Long>,
    mode: PhotoMode,
    listBottomPadding: Dp,
    onPhotoClicked: (Long) -> Unit,
) {
    if (items.isEmpty()) {
        Text(
            text = "No photos yet",
            modifier = Modifier.padding(24.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    } else {
        Crossfade(
            targetState = mode,
            modifier = Modifier.fillMaxSize(),
            label = "PhotosFeedModeTransition",
        ) { targetMode ->
            if (targetMode == PhotoMode.ANIMATE) {
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
                        PhotoGridItem(
                            item = item,
                            isSelected = selectedIds.contains(item.measurementId),
                            mode = targetMode,
                            onPhotoClicked = onPhotoClicked,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = listBottomPadding),
                ) {
                    items(
                        items = items,
                        key = { it.measurementId },
                    ) { item ->
                        PhotoListItem(
                            item = item,
                            isSelected = selectedIds.contains(item.measurementId),
                            mode = targetMode,
                            onPhotoClicked = onPhotoClicked,
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoListItem(
    item: PhotosItemUiModel,
    isSelected: Boolean,
    mode: PhotoMode,
    onPhotoClicked: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        PhotoTile(
            item = item,
            isSelected = isSelected,
            mode = mode,
            onPhotoClicked = onPhotoClicked,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .align(Alignment.CenterHorizontally),
            dateFontSize = 14.sp,
            selectedIndicatorSize = 28.dp,
            selectionPadding = 8.dp,
        )
    }
}

@Composable
private fun PhotoGridItem(
    item: PhotosItemUiModel,
    isSelected: Boolean,
    mode: PhotoMode,
    onPhotoClicked: (Long) -> Unit,
) {
    PhotoTile(
        item = item,
        isSelected = isSelected,
        mode = mode,
        onPhotoClicked = onPhotoClicked,
        modifier = Modifier.fillMaxWidth(),
        dateFontSize = 10.sp,
        selectedIndicatorSize = 22.dp,
        selectionPadding = 6.dp,
    )
}

@Composable
private fun PhotoTile(
    item: PhotosItemUiModel,
    isSelected: Boolean,
    mode: PhotoMode,
    onPhotoClicked: (Long) -> Unit,
    modifier: Modifier,
    dateFontSize: androidx.compose.ui.unit.TextUnit,
    selectedIndicatorSize: Dp,
    selectionPadding: Dp,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = mode != PhotoMode.NORMAL) {
                onPhotoClicked(item.measurementId)
            },
    ) {
        AsyncImage(
            model = item.photoFile,
            contentDescription = "Progress photo",
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
        if (mode != PhotoMode.NORMAL && isSelected) {
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
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}
