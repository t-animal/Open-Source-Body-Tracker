package de.t_animal.opensourcebodytracker.feature.measurements.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.ui.components.DecimalNumberInputField

@Composable
fun MetricInputField(
    isVisible: Boolean,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    imeAction: ImeAction,
    addBottomSpacing: Boolean = true,
) {
    if (!isVisible) {
        return
    }

    DecimalNumberInputField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        imeAction = imeAction,
    )

    if (addBottomSpacing) {
        Spacer(modifier = Modifier.height(8.dp))
    }
}
