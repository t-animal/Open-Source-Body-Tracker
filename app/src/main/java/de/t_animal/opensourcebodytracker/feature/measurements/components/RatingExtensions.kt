package de.t_animal.opensourcebodytracker.feature.measurements.components

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.RatingLabel
import de.t_animal.opensourcebodytracker.core.model.RatingSeverity

internal val RatingLabel.labelResourceId: Int
    @StringRes get() = when (this) {
        RatingLabel.SevereUnderweight -> R.string.rating_severe_underweight
        RatingLabel.Underweight -> R.string.rating_underweight
        RatingLabel.Normal -> R.string.rating_normal
        RatingLabel.Overweight -> R.string.rating_overweight
        RatingLabel.ObeseClassI -> R.string.rating_obese_class_i
        RatingLabel.ObeseClassII -> R.string.rating_obese_class_ii
        RatingLabel.ObeseClassIII -> R.string.rating_obese_class_iii
        RatingLabel.DangerouslyLow -> R.string.rating_dangerously_low
        RatingLabel.EssentialFat -> R.string.rating_essential_fat
        RatingLabel.Athletic -> R.string.rating_athletic
        RatingLabel.Fit -> R.string.rating_fit
        RatingLabel.Acceptable -> R.string.rating_acceptable
        RatingLabel.Obese -> R.string.rating_obese
        RatingLabel.LowRisk -> R.string.rating_low_risk
        RatingLabel.ModerateRisk -> R.string.rating_moderate_risk
        RatingLabel.HighRisk -> R.string.rating_high_risk
        RatingLabel.VeryHighRisk -> R.string.rating_very_high_risk
        RatingLabel.UnderweightRisk -> R.string.rating_underweight_risk
        RatingLabel.Healthy -> R.string.rating_healthy
        RatingLabel.IncreasedRisk -> R.string.rating_increased_risk
    }

@Composable
internal fun RatingSeverity.toColor(): Color = when (this) {
    RatingSeverity.Good -> MaterialTheme.colorScheme.tertiary
    RatingSeverity.Fair -> MaterialTheme.colorScheme.secondary
    RatingSeverity.Poor -> MaterialTheme.colorScheme.error
    RatingSeverity.Severe -> MaterialTheme.colorScheme.error
}
