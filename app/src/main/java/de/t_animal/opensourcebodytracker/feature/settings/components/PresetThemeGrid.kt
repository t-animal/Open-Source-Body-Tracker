package de.t_animal.opensourcebodytracker.feature.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.ThemePreset
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PresetThemeGrid(
    selectedPreset: ThemePreset,
    onPresetSelected: (ThemePreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        maxItemsInEachRow = 4,
    ) {
        ThemePreset.entries.forEach { preset ->
            PresetThemeChip(
                preset = preset,
                selected = preset == selectedPreset,
                onClick = { onPresetSelected(preset) },
            )
        }
    }
}

@Composable
private fun PresetThemeChip(
    preset: ThemePreset,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(preset.seedColorArgb), CircleShape)
                .then(
                    if (selected) {
                        Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    } else {
                        Modifier
                    },
                ),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = presetLabel(preset),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun presetLabel(preset: ThemePreset): String = when (preset) {
    ThemePreset.Ocean -> stringResource(R.string.settings_theme_preset_ocean)
    ThemePreset.Jungle -> stringResource(R.string.settings_theme_preset_jungle)
    ThemePreset.Sunset -> stringResource(R.string.settings_theme_preset_sunset)
    ThemePreset.Berry -> stringResource(R.string.settings_theme_preset_berry)
    ThemePreset.Desert -> stringResource(R.string.settings_theme_preset_desert)
    ThemePreset.Rose -> stringResource(R.string.settings_theme_preset_rose)
    ThemePreset.Slate -> stringResource(R.string.settings_theme_preset_slate)
    ThemePreset.Mono -> stringResource(R.string.settings_theme_preset_mono)
}

@Preview(showBackground = true)
@Composable
private fun PresetThemeGridOceanSelectedPreview() {
    BodyTrackerTheme {
        PresetThemeGrid(
            selectedPreset = ThemePreset.Ocean,
            onPresetSelected = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PresetThemeGridBerrySelectedPreview() {
    BodyTrackerTheme {
        PresetThemeGrid(
            selectedPreset = ThemePreset.Berry,
            onPresetSelected = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
