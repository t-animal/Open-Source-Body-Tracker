package de.t_animal.opensourcebodytracker.core.model

enum class ThemePaletteStyle { TonalSpot, Vibrant, Expressive, Monochrome }

enum class ThemePreset(
    val seedColorArgb: Int,
    val paletteStyle: ThemePaletteStyle,
) {
    Ocean(0xFF0077B6.toInt(), ThemePaletteStyle.TonalSpot),
    Jungle(0xFF2D6A4F.toInt(), ThemePaletteStyle.TonalSpot),
    Sunset(0xFFE76F51.toInt(), ThemePaletteStyle.TonalSpot),
    Berry(0xFF7B2D8B.toInt(), ThemePaletteStyle.TonalSpot),
    Desert(0xFFC9A227.toInt(), ThemePaletteStyle.TonalSpot),
    Rose(0xFFE63946.toInt(), ThemePaletteStyle.TonalSpot),
    Slate(0xFF4A6FA5.toInt(), ThemePaletteStyle.TonalSpot),
    Mono(0xFF607D8B.toInt(), ThemePaletteStyle.Monochrome),
}

sealed class ThemePreference {
    data object SystemDefault : ThemePreference()
    data class Preset(val preset: ThemePreset) : ThemePreference()
    data class Custom(val seedColorArgb: Int, val paletteStyle: ThemePaletteStyle) : ThemePreference()
}
