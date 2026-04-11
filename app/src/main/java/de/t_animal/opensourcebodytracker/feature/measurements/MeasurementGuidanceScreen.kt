package de.t_animal.opensourcebodytracker.feature.measurements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.fullName
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.guidanceResId
import de.t_animal.opensourcebodytracker.ui.components.SecondaryScreenScaffold
import de.t_animal.opensourcebodytracker.ui.helpers.styledStringResource

@Composable
fun MeasurementGuidanceRoute(onNavigateBack: () -> Unit) {
    val expandedItems = remember { mutableStateSetOf<MeasuredBodyMetric>() }

    SecondaryScreenScaffold(
        title = stringResource(R.string.measurement_guidance_screen_title),
        onNavigateBack = onNavigateBack,
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(MeasuredBodyMetric.entries) { metric ->
                MeasurementGuidanceAccordionItem(
                    metric = metric,
                    expanded = metric in expandedItems,
                    onToggle = {
                        if (metric in expandedItems) expandedItems.remove(metric)
                        else expandedItems.add(metric)
                    },
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun MeasurementGuidanceAccordionItem(
    metric: MeasuredBodyMetric,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = metric.fullName(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
            )
        }
        AnimatedVisibility(visible = expanded) {
            Text(
                text = styledStringResource(metric.guidanceResId()),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            )
        }
    }
}
