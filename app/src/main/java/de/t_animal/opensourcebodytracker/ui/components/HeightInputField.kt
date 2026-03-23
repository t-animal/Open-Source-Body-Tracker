package de.t_animal.opensourcebodytracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.UnitSystem
import de.t_animal.opensourcebodytracker.core.model.cmToFeetAndInches
import de.t_animal.opensourcebodytracker.core.model.feetAndInchesToCm
import de.t_animal.opensourcebodytracker.core.util.formatDecimalForInput
import de.t_animal.opensourcebodytracker.core.util.parseLocalizedDoubleOrNull
import de.t_animal.opensourcebodytracker.core.util.parseLocalizedFloatOrNull
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@Composable
fun HeightInputField(
    heightCmText: String,
    unitSystem: UnitSystem,
    onHeightCmChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if(unitSystem == UnitSystem.Metric) {
        DecimalNumberInputField(
            label = stringResource(R.string.profile_label_height),
            value = heightCmText,
            onValueChange = onHeightCmChanged,
            modifier = modifier.fillMaxWidth(),
            imeAction = ImeAction.Done,
        )
    } else {
        val initialFeetAndInches = remember(heightCmText) {
            val cm = parseLocalizedFloatOrNull(heightCmText)
            if (cm != null && cm > 0f) {
                val (feet, inches) = cmToFeetAndInches(cm)
                feet.toString() to formatDecimalForInput(inches)
            } else {
                "" to ""
            }
        }

        var feetText by remember(heightCmText) { mutableStateOf(initialFeetAndInches.first) }
        var inchesText by remember(heightCmText) { mutableStateOf(initialFeetAndInches.second) }

        fun reportCmChange(feet: String, inches: String) {
            val feetVal = feet.trim().toIntOrNull()
            val inchesVal = parseLocalizedDoubleOrNull(inches)
            if (feetVal != null && inchesVal != null) {
                val cm = feetAndInchesToCm(feetVal, inchesVal)
                onHeightCmChanged(formatDecimalForInput(cm.toDouble()))
            }
        }

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IntegerNumberInputField(
                label = stringResource(R.string.profile_label_height_feet),
                value = feetText,
                onValueChange = { newFeet ->
                    feetText = newFeet
                    reportCmChange(newFeet, inchesText)
                },
                modifier = Modifier.weight(1f),
                imeAction = ImeAction.Next,
            )
            DecimalNumberInputField(
                label = stringResource(R.string.profile_label_height_inches),
                value = inchesText,
                onValueChange = { newInches ->
                    inchesText = newInches
                    reportCmChange(feetText, newInches)
                },
                modifier = Modifier.weight(1f),
                imeAction = ImeAction.Done,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HeightInputFieldPreview_Metric() {
    BodyTrackerTheme {
        HeightInputField(
            heightCmText = "180",
            unitSystem = UnitSystem.Metric,
            onHeightCmChanged = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HeightInputFieldPreview_Imperial() {
    BodyTrackerTheme {
        HeightInputField(
            heightCmText = "180",
            unitSystem = UnitSystem.Imperial,
            onHeightCmChanged = {},
        )
    }
}
