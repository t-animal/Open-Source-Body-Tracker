package de.t_animal.opensourcebodytracker.core.util

import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.ZoneId
import kotlin.math.roundToLong

fun parseLocalizedDoubleOrNull(text: String): Double? {
    val trimmed = text.trim()
    if (trimmed.isBlank()) return null
    val decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator
    return trimmed
        .replace(decimalSeparator, '.')
        .replace(',', '.')
        .toDoubleOrNull()
}

fun parseLocalizedFloatOrNull(text: String): Float? {
    return parseLocalizedDoubleOrNull(text)?.toFloat()
}

fun formatDecimalForInput(value: Double, maxDecimalPlaces: Int = 2): String {
    val decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator
    val factor = Math.pow(10.0, maxDecimalPlaces.toDouble())
    val rounded = (value * factor).roundToLong() / factor
    val text = if (rounded == rounded.toLong().toDouble()) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
    return if (decimalSeparator == '.') text else text.replace('.', decimalSeparator)
}

fun formatEpochMillisAsIsoDate(epochMillis: Long): String {
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .toString()
}
