package de.t_animal.opensourcebodytracker.feature.settings.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.PhotoQuality
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoQualitySelector(
    photoQuality: PhotoQuality,
    onPhotoQualityChanged: (PhotoQuality) -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = stringResource(R.string.settings_photo_quality_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.settings_photo_quality_intro),
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            PhotoQuality.entries.forEachIndexed { index, quality ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = PhotoQuality.entries.size,
                    ),
                    onClick = { onPhotoQualityChanged(quality) },
                    selected = quality == photoQuality,
                ) {
                    Text(text = qualityLabel(quality))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = qualityDescription(photoQuality),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.animateContentSize(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.settings_photo_quality_hint_new_photos_only),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun qualityLabel(quality: PhotoQuality): String = when (quality) {
    PhotoQuality.Original -> stringResource(R.string.settings_photo_quality_original)
    PhotoQuality.High -> stringResource(R.string.settings_photo_quality_high)
    PhotoQuality.Medium -> stringResource(R.string.settings_photo_quality_medium)
    PhotoQuality.Low -> stringResource(R.string.settings_photo_quality_low)
}

@Composable
private fun qualityDescription(quality: PhotoQuality): String = when (quality) {
    PhotoQuality.Original -> stringResource(R.string.settings_photo_quality_description_original)
    PhotoQuality.High -> stringResource(R.string.settings_photo_quality_description_high)
    PhotoQuality.Medium -> stringResource(R.string.settings_photo_quality_description_medium)
    PhotoQuality.Low -> stringResource(R.string.settings_photo_quality_description_low)
}

@Preview(showBackground = true)
@Composable
private fun PhotoQualitySelectorOriginalPreview() {
    BodyTrackerTheme {
        PhotoQualitySelector(
            photoQuality = PhotoQuality.Original,
            onPhotoQualityChanged = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotoQualitySelectorMediumPreview() {
    BodyTrackerTheme {
        PhotoQualitySelector(
            photoQuality = PhotoQuality.Medium,
            onPhotoQualityChanged = {},
        )
    }
}
