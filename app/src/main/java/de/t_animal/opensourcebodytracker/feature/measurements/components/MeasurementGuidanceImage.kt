package de.t_animal.opensourcebodytracker.feature.measurements.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Female
import androidx.compose.material.icons.outlined.Male
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.guidanceImageResId
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.initialGuidanceOrientation
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

enum class GuidanceImageSide { Front, Back }

@Composable
internal fun MeasurementGuidanceImage(
    metric: MeasuredBodyMetric,
    initialSex: Sex?,
    initialOrientation: GuidanceImageSide,
) {
    var selectedSex by remember(metric, initialSex) { mutableStateOf(initialSex ?: Sex.Male) }
    var selectedSide by remember(metric, initialOrientation) { mutableStateOf(initialOrientation) }
    val maleContentDescription = stringResource(R.string.profile_sex_male)
    val femaleContentDescription = stringResource(R.string.profile_sex_female)

    Column(modifier = Modifier.fillMaxWidth()) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = selectedSex == Sex.Male,
                onClick = { selectedSex = Sex.Male },
                modifier = Modifier.semantics { contentDescription = maleContentDescription },
                label = { Text(stringResource(R.string.measurement_guidance_sex_abbreviation_male)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Male,
                        contentDescription = null,
                    )
                },
            )
            FilterChip(
                selected = selectedSex == Sex.Female,
                onClick = { selectedSex = Sex.Female },
                modifier = Modifier.semantics { contentDescription = femaleContentDescription },
                label = { Text(stringResource(R.string.measurement_guidance_sex_abbreviation_female)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Female,
                        contentDescription = null,
                    )
                },
            )
            Spacer(modifier = Modifier.width(4.dp))
            FilterChip(
                selected = selectedSide == GuidanceImageSide.Front,
                onClick = { selectedSide = GuidanceImageSide.Front },
                label = { Text(stringResource(R.string.measurement_guidance_side_front)) },
            )
            FilterChip(
                selected = selectedSide == GuidanceImageSide.Back,
                onClick = { selectedSide = GuidanceImageSide.Back },
                label = { Text(stringResource(R.string.measurement_guidance_side_back)) },
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Image(
            painter = painterResource(metric.guidanceImageResId(selectedSex, selectedSide)),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MeasurementGuidanceImagePreview_MaleFront() {
    BodyTrackerTheme {
        MeasurementGuidanceImage(
            metric = MeasuredBodyMetric.WaistCircumference,
            initialSex = Sex.Male,
            initialOrientation = MeasuredBodyMetric.WaistCircumference.initialGuidanceOrientation(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MeasurementGuidanceImagePreview_FemaleBack() {
    BodyTrackerTheme {
        MeasurementGuidanceImage(
            metric = MeasuredBodyMetric.TricepsSkinfold,
            initialSex = Sex.Female,
            initialOrientation = MeasuredBodyMetric.TricepsSkinfold.initialGuidanceOrientation(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MeasurementGuidanceImagePreview_NullSex() {
    BodyTrackerTheme {
        MeasurementGuidanceImage(
            metric = MeasuredBodyMetric.HipCircumference,
            initialSex = null,
            initialOrientation = MeasuredBodyMetric.HipCircumference.initialGuidanceOrientation(),
        )
    }
}
