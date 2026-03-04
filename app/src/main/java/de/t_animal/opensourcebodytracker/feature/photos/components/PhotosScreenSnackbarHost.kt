package de.t_animal.opensourcebodytracker.feature.photos.components

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp

@Composable
fun PhotosScreenSnackbarHost(
    snackbarHostState: SnackbarHostState,
    isSelectionBottomBarVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier.padding(
            bottom = if (isSelectionBottomBarVisible) {
                96.dp
            } else {
                24.dp
            },
        ),
    )
}
