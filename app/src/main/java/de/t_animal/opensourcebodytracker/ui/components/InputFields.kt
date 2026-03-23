package de.t_animal.opensourcebodytracker.ui.components

import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import de.t_animal.opensourcebodytracker.R
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateInputField(
    label: String,
    valueText: String,
    selectedDateMillis: Long?,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    pickButtonText: String? = null,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    // Our app stores/works with epoch millis in the device timezone. Material3 DatePicker expects
    // UTC-based millis that represent a calendar date. Convert in/out to avoid off-by-one errors.
    val selectedLocalDate = remember(valueText, selectedDateMillis) {
        when {
            selectedDateMillis != null -> epochMillisToLocalDateInSystemZone(selectedDateMillis)
            valueText.isBlank() -> null
            else -> runCatching { LocalDate.parse(valueText.trim()) }.getOrNull()
        }
    }

    val initialSelectedDateMillisUtc = remember(selectedLocalDate) {
        selectedLocalDate
            ?.atStartOfDay(ZoneOffset.UTC)
            ?.toInstant()
            ?.toEpochMilli()
    }

    val datePickerState = key(initialSelectedDateMillisUtc) {
        androidx.compose.material3.rememberDatePickerState(initialSelectedDateMillis = initialSelectedDateMillisUtc)
    }

    val displayText = remember(valueText, selectedLocalDate) {
        when {
            selectedLocalDate != null -> formatLocalDateToLocalizedNumericDate(selectedLocalDate)
            valueText.isBlank() -> ""
            else -> valueText
        }
    }

    OutlinedTextField(
        value = displayText,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        singleLine = true,
        modifier = modifier.pointerInput(selectedDateMillis) {
            awaitEachGesture {
                awaitFirstDown(pass = PointerEventPass.Initial)
                val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                if (upEvent != null) {
                    showDatePicker = true
                }
            }
        },
        trailingIcon = {
            TextButton(onClick = { showDatePicker = true }) {
                Text(pickButtonText ?: stringResource(R.string.common_pick))
            }
        },
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selected = datePickerState.selectedDateMillis
                        if (selected != null) {
                            val pickedDate = Instant.ofEpochMilli(selected)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            val localEpochMillis = pickedDate
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli()
                            onDateSelected(localEpochMillis)
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.common_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun epochMillisToLocalDateInSystemZone(epochMillis: Long): LocalDate {
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

internal fun formatEpochMillisToLocalizedNumericDate(epochMillis: Long): String {
    val localDate = epochMillisToLocalDateInSystemZone(epochMillis)
    return formatLocalDateToLocalizedNumericDate(localDate)
}

internal fun formatLocalDateToLocalizedNumericDate(date: LocalDate): String {
    val locale = Locale.getDefault()
    val pattern = (DateFormat.getInstanceForSkeleton("yMd", locale) as SimpleDateFormat)
        .toPattern()
        .replace(Regex("y+")) { match -> if (match.value.length >= 4) match.value else "yyyy" }
        .replace(Regex("M+")) { match -> if (match.value.length >= 2) match.value else "MM" }
        .replace(Regex("d+")) { match -> if (match.value.length >= 2) match.value else "dd" }

    val formatter = DateTimeFormatter.ofPattern(pattern, locale)
    return formatter.format(date)
}

@Composable
fun DecimalNumberInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(filterDecimalInputLocalized(it)) },
        label = { Text(label) },
        singleLine = true,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = imeAction,
        ),
    )
}

@Composable
fun IntegerNumberInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter { c -> c.isDigit() }) },
        label = { Text(label) },
        singleLine = true,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = imeAction,
        ),
    )
}

private fun filterDecimalInputLocalized(raw: String): String {
    if (raw.isBlank()) return ""

    val decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator

    val out = StringBuilder(raw.length)
    var seenSeparator = false
    var decimalDigits = 0

    for (c in raw) {
        when {
            c.isDigit() -> {
                if (seenSeparator) {
                    if (decimalDigits < 2) {
                        out.append(c)
                        decimalDigits++
                    }
                } else {
                    out.append(c)
                }
            }
            (c == '.' || c == ',' || c == decimalSeparator) && !seenSeparator -> {
                if (out.isEmpty()) out.append('0')
                out.append(decimalSeparator)
                seenSeparator = true
            }
        }
    }

    return out.toString()
}
