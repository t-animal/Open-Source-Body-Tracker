package de.t_animal.opensourcebodytracker.feature.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.ThemePaletteStyle
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomThemePickerDialog(
    initialColor: Color,
    initialPaletteStyle: ThemePaletteStyle,
    onDismiss: () -> Unit,
    onApply: (Color, ThemePaletteStyle) -> Unit,
) {
    var currentColor by remember { mutableStateOf(initialColor) }
    var currentStyle by remember { mutableStateOf(initialPaletteStyle) }
    val controller = rememberColorPickerController()

    LaunchedEffect(Unit) {
        controller.selectByColor(initialColor, fromUser = false)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.settings_theme_picker_title),
                    style = MaterialTheme.typography.titleLarge,
                )

                Spacer(Modifier.height(16.dp))

                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    controller = controller,
                    onColorChanged = { envelope: ColorEnvelope ->
                        if (envelope.fromUser) currentColor = envelope.color
                    },
                )

                Spacer(Modifier.height(8.dp))

                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp),
                    controller = controller,
                )

                Spacer(Modifier.height(16.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ThemePaletteStyle.entries.forEach { style ->
                        FilterChip(
                            selected = style == currentStyle,
                            onClick = { currentStyle = style },
                            label = { Text(paletteStyleLabel(style)) },
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                ThemePreviewComponent(seedColor = currentColor, paletteStyle = currentStyle)

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.common_cancel))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onApply(currentColor, currentStyle) }) {
                        Text(stringResource(R.string.common_apply))
                    }
                }
            }
        }
    }
}

@Composable
internal fun paletteStyleLabel(style: ThemePaletteStyle): String = when (style) {
    ThemePaletteStyle.TonalSpot -> stringResource(R.string.settings_theme_palette_style_tonal_spot)
    ThemePaletteStyle.Vibrant -> stringResource(R.string.settings_theme_palette_style_vibrant)
    ThemePaletteStyle.Expressive -> stringResource(R.string.settings_theme_palette_style_expressive)
    ThemePaletteStyle.Monochrome -> stringResource(R.string.settings_theme_palette_style_monochrome)
}

@Preview
@Composable
private fun CustomThemePickerDialogPreview() {
    BodyTrackerTheme {
        CustomThemePickerDialog(
            initialColor = Color(0xFF0077B6),
            initialPaletteStyle = ThemePaletteStyle.TonalSpot,
            onDismiss = {},
            onApply = { _, _ -> },
        )
    }
}
