package de.t_animal.opensourcebodytracker.feature.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.materialkolor.rememberDynamicColorScheme
import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.DerivedBodyMetric
import de.t_animal.opensourcebodytracker.core.model.DerivedMetrics
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.ThemePaletteStyle
import de.t_animal.opensourcebodytracker.core.model.UnitSystem
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementListItemUiModel
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementListScreen
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementListUiState
import de.t_animal.opensourcebodytracker.ui.components.MainScreenScaffold
import de.t_animal.opensourcebodytracker.ui.navigation.MainDestination
import de.t_animal.opensourcebodytracker.ui.theme.toPaletteStyle
import kotlin.math.roundToInt

private val PREVIEW_PHONE_WIDTH = 360.dp
private val PREVIEW_PHONE_HEIGHT = 640.dp
private const val PREVIEW_SCALE = 0.5f

private fun Modifier.scaledPhonePreview(): Modifier = layout { measurable, _ ->
    val phoneWidth = PREVIEW_PHONE_WIDTH.roundToPx()
    val phoneHeight = PREVIEW_PHONE_HEIGHT.roundToPx()
    val placeable = measurable.measure(Constraints.fixed(phoneWidth, phoneHeight))
    val displayWidth = (phoneWidth * PREVIEW_SCALE).roundToInt()
    val displayHeight = (phoneHeight * PREVIEW_SCALE).roundToInt()
    layout(displayWidth, displayHeight) {
        placeable.placeWithLayer(0, 0) {
            scaleX = PREVIEW_SCALE
            scaleY = PREVIEW_SCALE
            transformOrigin = TransformOrigin(0f, 0f)
        }
    }
}

private val PREVIEW_MEASUREMENTS = listOf(
    MeasurementListItemUiModel(
        measurement = BodyMeasurement(id = 1, dateEpochMillis = 1_710_000_000_000, weightKg = 80.5),
        derivedMetrics = DerivedMetrics(bmi = 24.7),
    ),
    MeasurementListItemUiModel(
        measurement = BodyMeasurement(id = 2, dateEpochMillis = 1_708_000_000_000, weightKg = 81.2),
        derivedMetrics = DerivedMetrics(bmi = 25.0),
    ),
    MeasurementListItemUiModel(
        measurement = BodyMeasurement(id = 3, dateEpochMillis = 1_706_000_000_000, weightKg = 81.8),
        derivedMetrics = DerivedMetrics(bmi = 25.2),
    ),
    MeasurementListItemUiModel(
        measurement = BodyMeasurement(id = 4, dateEpochMillis = 1_704_000_000_000, weightKg = 82.3),
        derivedMetrics = DerivedMetrics(bmi = 25.4),
    ),
    MeasurementListItemUiModel(
        measurement = BodyMeasurement(id = 5, dateEpochMillis = 1_702_000_000_000, weightKg = 82.9),
        derivedMetrics = DerivedMetrics(bmi = 25.5),
    ),
    MeasurementListItemUiModel(
        measurement = BodyMeasurement(id = 6, dateEpochMillis = 1_700_000_000_000, weightKg = 83.5),
        derivedMetrics = DerivedMetrics(bmi = 25.7),
    ),
)

private val PREVIEW_STATE = MeasurementListUiState.Loaded(
    latestMeasurement = MeasurementListItemUiModel(
        measurement = BodyMeasurement(
            id = 1,
            dateEpochMillis = 1_710_000_000_000,
            weightKg = 80.5,
            waistCircumferenceCm = 86.0,
            hipCircumferenceCm = 95.0,
        ),
        derivedMetrics = DerivedMetrics(
            bmi = 24.7,
            navyBodyFatPercent = 18.3,
            waistHipRatio = 0.91,
            waistHeightRatio = 0.50,
        ),
    ),
    previewMeasurements = PREVIEW_MEASUREMENTS.take(3),
    allMeasurements = PREVIEW_MEASUREMENTS,
    hasMoreMeasurements = false,
    visibleInTableMetrics = listOf(MeasuredBodyMetric.Weight, DerivedBodyMetric.Bmi),
    unitSystem = UnitSystem.Metric,
    userSex = Sex.Male,
    isEmpty = false,
    isDemoMode = false,
)

@Composable
internal fun ThemePreviewComponent(seedColor: Color, paletteStyle: ThemePaletteStyle) {
    val isDark = isSystemInDarkTheme()
    val previewColorScheme = rememberDynamicColorScheme(
        seedColor = seedColor,
        isDark = isDark,
        style = paletteStyle.toPaletteStyle(),
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(8.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        MaterialTheme(colorScheme = previewColorScheme) {
            Box(modifier = Modifier.scaledPhonePreview()) {
                MainScreenScaffold(
                    selectedDestination = MainDestination.Measurements,
                    onMainDestinationSelected = {},
                    onOpenSettings = {},
                    onOpenAbout = {},
                    onOpenMeasurementGuidance = {},
                    onOpenHealthRatingGuide = {},
                ) { contentPadding ->
                    MeasurementListScreen(
                        state = PREVIEW_STATE,
                        onEdit = {},
                        onAdd = {},
                        onOpenMore = {},
                        onResetApp = {},
                        contentPadding = contentPadding,
                    )
                }
            }
        }
    }
}
