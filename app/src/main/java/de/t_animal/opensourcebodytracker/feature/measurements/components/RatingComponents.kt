package de.t_animal.opensourcebodytracker.feature.measurements.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.DerivedBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MetricRating
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.RatingTableData
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.RatingTableEntry
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.fullName

@Composable
internal fun RatingDescription(
    entry: RatingTableEntry,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
) {
    val fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Normal
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(entry.label.labelResourceId),
                color = entry.severity.toColor(),
                fontWeight = fontWeight,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = entry.rangeText,
                fontWeight = fontWeight,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(
            text = stringResource(entry.descriptionRes),
            fontWeight = fontWeight,
            style = MaterialTheme.typography.bodySmall,
        )
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
internal fun RatingAccordionItem(
    entry: de.t_animal.opensourcebodytracker.feature.measurements.helpers.RatingTableEntry,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 0.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(entry.label.labelResourceId),
                color = entry.severity.toColor(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = entry.rangeText,
                style = MaterialTheme.typography.bodyMedium,
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = stringResource(entry.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RatingInfoBottomSheet(
    metric: DerivedBodyMetric,
    currentRating: MetricRating?,
    userSex: Sex,
    onDismiss: () -> Unit,
) {
    val entries = when (metric) {
        DerivedBodyMetric.Bmi -> RatingTableData.forBmi()
        DerivedBodyMetric.NavyBodyFatPercent,
        DerivedBodyMetric.SkinfoldBodyFatPercent -> RatingTableData.forBodyFat(userSex)
        DerivedBodyMetric.WaistHipRatio -> RatingTableData.forWaistHipRatio(userSex)
        DerivedBodyMetric.WaistHeightRatio -> RatingTableData.forWaistHeightRatio()
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            val metricName = metric.fullName()
            Text(
                text = metricName,
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.rating_info_sheet_intro, metricName),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.rating_info_sheet_disclaimer),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
            if (currentRating != null) {
                val ratingLabel = stringResource(currentRating.label.labelResourceId)
                Text(
                    text = stringResource(R.string.rating_info_sheet_current_rating, ratingLabel),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            entries.forEach { entry ->
                RatingDescription(
                    entry = entry,
                    highlighted = entry.label == currentRating?.label,
                )
            }
        }
    }
}
