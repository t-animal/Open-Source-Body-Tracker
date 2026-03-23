package de.t_animal.opensourcebodytracker.feature.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UnitSystem
import de.t_animal.opensourcebodytracker.ui.components.DateInputField
import de.t_animal.opensourcebodytracker.ui.components.HeightInputField
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun ProfileFormSection(
    sex: Sex?,
    dateOfBirthText: String,
    heightCmText: String,
    unitSystem: UnitSystem,
    onSexChanged: (Sex) -> Unit,
    onDateOfBirthChanged: (String) -> Unit,
    onHeightCmChanged: (String) -> Unit,
) {
    Text(
        text = stringResource(R.string.profile_form_intro),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 12.dp),
    )

    OutlinedCard(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(5.dp),
    ) {
        Text(
            text = stringResource(R.string.profile_label_sex),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
        )
        SexSelectionSection(
            sex = sex,
            onSexChanged = onSexChanged,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    val initialDobMillis = localDateTextToEpochMillisOrNull(dateOfBirthText)
    DateInputField(
        label = stringResource(R.string.profile_label_dob),
        valueText = dateOfBirthText,
        selectedDateMillis = initialDobMillis,
        onDateSelected = { onDateOfBirthChanged(epochMillisToIsoLocalDate(it)) },
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(16.dp))

    HeightInputField(
        heightCmText = heightCmText,
        unitSystem = unitSystem,
        onHeightCmChanged = onHeightCmChanged,
    )
}

@Composable
fun SexSelectionSection(
    sex: Sex?,
    onSexChanged: (Sex) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SexSelectionRow(
            label = stringResource(R.string.profile_sex_male),
            selected = sex == Sex.Male,
            onClick = { onSexChanged(Sex.Male) },
            modifier = Modifier.weight(1f),
        )
        SexSelectionRow(
            label = stringResource(R.string.profile_sex_female),
            selected = sex == Sex.Female,
            onClick = { onSexChanged(Sex.Female) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SexSelectionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.padding(vertical = 4.dp)) {
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
