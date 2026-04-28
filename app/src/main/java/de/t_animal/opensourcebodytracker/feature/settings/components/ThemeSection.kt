package de.t_animal.opensourcebodytracker.feature.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.ThemePaletteStyle
import de.t_animal.opensourcebodytracker.core.model.ThemePreference
import de.t_animal.opensourcebodytracker.core.model.ThemePreset
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

private enum class ThemeMode { System, Preset, Custom }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSection(
    themePreference: ThemePreference,
    onThemePreferenceChanged: (ThemePreference) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showColorPicker by remember { mutableStateOf(false) }
    val selectedMode = when (themePreference) {
        is ThemePreference.SystemDefault -> ThemeMode.System
        is ThemePreference.Preset -> ThemeMode.Preset
        is ThemePreference.Custom -> ThemeMode.Custom
    }

    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = stringResource(R.string.settings_theme_title),
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(Modifier.height(8.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ThemeMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = ThemeMode.entries.size,
                    ),
                    onClick = {
                        when (mode) {
                            ThemeMode.System ->
                                onThemePreferenceChanged(ThemePreference.SystemDefault)
                            ThemeMode.Preset ->
                                if (themePreference !is ThemePreference.Preset) {
                                    onThemePreferenceChanged(ThemePreference.Preset(ThemePreset.Ocean))
                                }
                            ThemeMode.Custom ->
                                if (themePreference !is ThemePreference.Custom) {
                                    onThemePreferenceChanged(
                                        ThemePreference.Custom(
                                            seedColorArgb = ThemePreset.Ocean.seedColorArgb,
                                            paletteStyle = ThemePaletteStyle.TonalSpot,
                                        ),
                                    )
                                }
                        }
                    },
                    selected = mode == selectedMode,
                ) {
                    Text(
                        text = when (mode) {
                            ThemeMode.System -> stringResource(R.string.settings_theme_mode_system)
                            ThemeMode.Preset -> stringResource(R.string.settings_theme_mode_preset)
                            ThemeMode.Custom -> stringResource(R.string.settings_theme_mode_custom)
                        },
                    )
                }
            }
        }

        when (val pref = themePreference) {
            is ThemePreference.SystemDefault -> {}
            is ThemePreference.Preset -> {
                Spacer(Modifier.height(12.dp))
                PresetThemeGrid(
                    selectedPreset = pref.preset,
                    onPresetSelected = { onThemePreferenceChanged(ThemePreference.Preset(it)) },
                )
                Spacer(Modifier.height(12.dp))
                ThemePreviewComponent(
                    seedColor = Color(pref.preset.seedColorArgb),
                    paletteStyle = pref.preset.paletteStyle,
                )
            }
            is ThemePreference.Custom -> {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showColorPicker = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(pref.seedColorArgb), RoundedCornerShape(4.dp)),
                    )
                    Text(
                        text = paletteStyleLabel(pref.paletteStyle) +
                            "  •  #%06X".format(pref.seedColorArgb and 0x00FFFFFF),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.cd_settings_theme_edit_custom),
                    )
                }
                Spacer(Modifier.height(12.dp))
                ThemePreviewComponent(
                    seedColor = Color(pref.seedColorArgb),
                    paletteStyle = pref.paletteStyle,
                )
            }
        }
    }

    if (showColorPicker) {
        val currentPref = themePreference
        val initialColor = if (currentPref is ThemePreference.Custom) {
            Color(currentPref.seedColorArgb)
        } else {
            Color(ThemePreset.Ocean.seedColorArgb)
        }
        val initialStyle = if (currentPref is ThemePreference.Custom) {
            currentPref.paletteStyle
        } else {
            ThemePaletteStyle.TonalSpot
        }
        CustomThemePickerDialog(
            initialColor = initialColor,
            initialPaletteStyle = initialStyle,
            onDismiss = { showColorPicker = false },
            onApply = { color, style ->
                onThemePreferenceChanged(
                    ThemePreference.Custom(
                        seedColorArgb = color.toArgb(),
                        paletteStyle = style,
                    ),
                )
                showColorPicker = false
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemeSectionSystemPreview() {
    BodyTrackerTheme {
        ThemeSection(
            themePreference = ThemePreference.SystemDefault,
            onThemePreferenceChanged = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemeSectionPresetPreview() {
    BodyTrackerTheme {
        ThemeSection(
            themePreference = ThemePreference.Preset(ThemePreset.Berry),
            onThemePreferenceChanged = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemeSectionCustomPreview() {
    BodyTrackerTheme {
        ThemeSection(
            themePreference = ThemePreference.Custom(
                seedColorArgb = 0xFF0077B6.toInt(),
                paletteStyle = ThemePaletteStyle.Vibrant,
            ),
            onThemePreferenceChanged = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
