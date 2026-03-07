package de.t_animal.opensourcebodytracker.feature.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.ui.components.DateInputField
import de.t_animal.opensourcebodytracker.ui.components.DecimalNumberInputField
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun ProfileFormSection(
    sex: Sex?,
    dateOfBirthText: String,
    heightCmText: String,
    onSexChanged: (Sex) -> Unit,
    onDateOfBirthChanged: (String) -> Unit,
    onHeightChanged: (String) -> Unit,
) {
    Text(text = "Sex", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))

    SexSelectionSection(
        sex = sex,
        onSexChanged = onSexChanged,
    )

    Spacer(modifier = Modifier.height(16.dp))

    val initialDobMillis = localDateTextToEpochMillisOrNull(dateOfBirthText)
    DateInputField(
        label = "Date of birth",
        valueText = dateOfBirthText,
        selectedDateMillis = initialDobMillis,
        onDateSelected = { onDateOfBirthChanged(epochMillisToIsoLocalDate(it)) },
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(16.dp))

    DecimalNumberInputField(
        label = "Height (cm)",
        value = heightCmText,
        onValueChange = onHeightChanged,
        modifier = Modifier.fillMaxWidth(),
        imeAction = ImeAction.Done,
    )
}

@Composable
fun SexSelectionSection(
    sex: Sex?,
    onSexChanged: (Sex) -> Unit,
) {
    Column {
        SexSelectionRow(
            label = "Male",
            selected = sex == Sex.Male,
            onClick = { onSexChanged(Sex.Male) },
        )
        SexSelectionRow(
            label = "Female",
            selected = sex == Sex.Female,
            onClick = { onSexChanged(Sex.Female) },
        )
    }
}

@Composable
private fun SexSelectionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = label,
            modifier = Modifier
                .padding(start = 8.dp)
                .align(Alignment.CenterVertically),
        )
    }
}

private fun localDateTextToEpochMillisOrNull(text: String): Long? {
    val date = runCatching { LocalDate.parse(text.trim()) }.getOrNull() ?: return null
    return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun epochMillisToIsoLocalDate(epochMillis: Long): String {
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .toString()
}