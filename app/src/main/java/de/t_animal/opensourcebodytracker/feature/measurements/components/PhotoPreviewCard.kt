package de.t_animal.opensourcebodytracker.feature.measurements.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.R
import coil.compose.AsyncImage
import java.io.File

@Composable
fun PhotoPreviewCard(
    photoPreviewModel: File,
    onClick: () -> Unit,
) {
    val previewShape = MaterialTheme.shapes.medium

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = photoPreviewModel,
            contentDescription = stringResource(R.string.cd_photo_preview),
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .heightIn(max = 500.dp)
                .clip(previewShape)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = previewShape,
                )
                .clickable { onClick() },
            contentScale = ContentScale.Fit,
        )
    }
}
