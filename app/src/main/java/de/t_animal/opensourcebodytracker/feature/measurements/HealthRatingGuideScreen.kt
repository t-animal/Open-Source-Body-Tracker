package de.t_animal.opensourcebodytracker.feature.measurements

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.feature.measurements.components.RatingAccordionItem
import de.t_animal.opensourcebodytracker.feature.measurements.components.RatingDescription
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.RatingTableData
import de.t_animal.opensourcebodytracker.ui.components.SecondaryScreenScaffold

@Composable
fun HealthRatingGuideRoute(onNavigateBack: () -> Unit) {
    HealthRatingGuideScreen(onNavigateBack = onNavigateBack)
}

@Composable
fun HealthRatingGuideScreen(onNavigateBack: () -> Unit) {
    val expandedItems = remember { mutableStateSetOf<de.t_animal.opensourcebodytracker.feature.measurements.helpers.RatingTableEntry>() }

    SecondaryScreenScaffold(
        title = stringResource(R.string.health_rating_guide_title),
        onNavigateBack = onNavigateBack,
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            RatingTableData.allTables().forEach { section ->
                item {
                    Text(
                        text = stringResource(section.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
                items(section.entries) { entry ->
                    RatingAccordionItem(
                        entry = entry,
                        expanded = entry in expandedItems,
                        onToggle = {
                            if (entry in expandedItems) expandedItems.remove(entry)
                            else expandedItems.add(entry)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}
