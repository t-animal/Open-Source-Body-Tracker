package de.t_animal.opensourcebodytracker.core.util

import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.ZoneId

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

fun formatDecimalForInput(value: Double): String {
    val decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator
    val text = value.toString()
    return if (decimalSeparator == '.') text else text.replace('.', decimalSeparator)
}

fun formatEpochMillisAsIsoDate(epochMillis: Long): String {
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .toString()
}
