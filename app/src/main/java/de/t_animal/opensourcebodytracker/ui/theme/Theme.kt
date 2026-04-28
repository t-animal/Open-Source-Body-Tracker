package de.t_animal.opensourcebodytracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme
import de.t_animal.opensourcebodytracker.core.model.ThemePaletteStyle
import de.t_animal.opensourcebodytracker.core.model.ThemePreference

private val LightColors = lightColorScheme()
private val DarkColors = darkColorScheme()

@Composable
fun BodyTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    themePreference: ThemePreference = ThemePreference.SystemDefault,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when (val pref = themePreference) {
        is ThemePreference.SystemDefault -> when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            darkTheme -> DarkColors
            else -> LightColors
        }
        is ThemePreference.Preset -> rememberDynamicColorScheme(
            seedColor = Color(pref.preset.seedColorArgb),
            isDark = darkTheme,
            style = pref.preset.paletteStyle.toPaletteStyle(),
        )
        is ThemePreference.Custom -> rememberDynamicColorScheme(
            seedColor = Color(pref.seedColorArgb),
            isDark = darkTheme,
            style = pref.paletteStyle.toPaletteStyle(),
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

fun ThemePaletteStyle.toPaletteStyle(): PaletteStyle = when (this) {
    ThemePaletteStyle.TonalSpot -> PaletteStyle.TonalSpot
    ThemePaletteStyle.Vibrant -> PaletteStyle.Vibrant
    ThemePaletteStyle.Expressive -> PaletteStyle.Expressive
    ThemePaletteStyle.Monochrome -> PaletteStyle.Monochrome
}
