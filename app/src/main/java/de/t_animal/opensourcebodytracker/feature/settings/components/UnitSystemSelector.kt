package de.t_animal.opensourcebodytracker.feature.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.UnitSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitSystemSelector(
    unitSystem: UnitSystem,
    onUnitSystemChanged: (UnitSystem) -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = stringResource(R.string.settings_item_units),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.profile_units_intro),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            UnitSystem.entries.forEachIndexed { index, system ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = UnitSystem.entries.size,
                    ),
                    onClick = { onUnitSystemChanged(system) },
                    selected = system == unitSystem,
                ) {
                    Text(
                        text = when (system) {
                            UnitSystem.Metric -> stringResource(R.string.settings_unit_metric)
                            UnitSystem.Imperial -> stringResource(R.string.settings_unit_imperial)
                        },
                    )
                }
            }
        }
    }
}
