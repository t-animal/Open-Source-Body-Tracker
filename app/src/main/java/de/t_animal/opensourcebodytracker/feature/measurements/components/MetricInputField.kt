package de.t_animal.opensourcebodytracker.feature.measurements.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.ui.components.DecimalNumberInputField

@Composable
fun MetricInputField(
    isVisible: Boolean,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    imeAction: ImeAction,
    addBottomSpacing: Boolean = true,
    onShowInfo: (() -> Unit)? = null,
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
        trailingIcon = if (onShowInfo != null) {
            {
                IconButton(onClick = onShowInfo) {
                    Icon(
                        imageVector = Icons.Outlined.HelpOutline,
                        contentDescription = stringResource(R.string.cd_metric_info),
                    )
                }
            }
        } else null,
    )

    if (addBottomSpacing) {
        Spacer(modifier = Modifier.height(8.dp))
    }
}
