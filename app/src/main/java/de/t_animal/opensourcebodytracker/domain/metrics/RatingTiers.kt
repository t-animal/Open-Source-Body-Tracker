package de.t_animal.opensourcebodytracker.domain.metrics

import de.t_animal.opensourcebodytracker.core.model.RatingLabel
import de.t_animal.opensourcebodytracker.core.model.RatingSeverity
import de.t_animal.opensourcebodytracker.core.model.Sex

internal data class RatingTier(
    val upToExclusive: Double?,
    val label: RatingLabel,
    val severity: RatingSeverity,
)

internal object RatingTiers {
    val bmi: List<RatingTier> = listOf(
        RatingTier(16.0, RatingLabel.SevereUnderweight, RatingSeverity.Severe),
        RatingTier(18.5, RatingLabel.Underweight, RatingSeverity.Poor),
        RatingTier(25.0, RatingLabel.Normal, RatingSeverity.Good),
        RatingTier(30.0, RatingLabel.Overweight, RatingSeverity.Fair),
        RatingTier(35.0, RatingLabel.ObeseClassI, RatingSeverity.Poor),
        RatingTier(40.0, RatingLabel.ObeseClassII, RatingSeverity.Poor),
        RatingTier(null, RatingLabel.ObeseClassIII, RatingSeverity.Severe),
    )

    private val maleBodyFat: List<RatingTier> = listOf(
        RatingTier(3.0, RatingLabel.DangerouslyLow, RatingSeverity.Severe),
        RatingTier(6.0, RatingLabel.EssentialFat, RatingSeverity.Poor),
        RatingTier(14.0, RatingLabel.Athletic, RatingSeverity.Good),
        RatingTier(18.0, RatingLabel.Fit, RatingSeverity.Good),
        RatingTier(25.0, RatingLabel.Acceptable, RatingSeverity.Fair),
        RatingTier(null, RatingLabel.Obese, RatingSeverity.Poor),
    )

    private val femaleBodyFat: List<RatingTier> = listOf(
        RatingTier(10.0, RatingLabel.DangerouslyLow, RatingSeverity.Severe),
        RatingTier(14.0, RatingLabel.EssentialFat, RatingSeverity.Poor),
        RatingTier(21.0, RatingLabel.Athletic, RatingSeverity.Good),
        RatingTier(25.0, RatingLabel.Fit, RatingSeverity.Good),
        RatingTier(32.0, RatingLabel.Acceptable, RatingSeverity.Fair),
        RatingTier(null, RatingLabel.Obese, RatingSeverity.Poor),
    )

    fun bodyFat(sex: Sex): List<RatingTier> = when (sex) {
        Sex.Male -> maleBodyFat
        Sex.Female -> femaleBodyFat
    }

    private val maleWaistHipRatio: List<RatingTier> = listOf(
        RatingTier(0.90, RatingLabel.LowRisk, RatingSeverity.Good),
        RatingTier(1.00, RatingLabel.ModerateRisk, RatingSeverity.Fair),
        RatingTier(1.10, RatingLabel.HighRisk, RatingSeverity.Poor),
        RatingTier(null, RatingLabel.VeryHighRisk, RatingSeverity.Severe),
    )

    private val femaleWaistHipRatio: List<RatingTier> = listOf(
        RatingTier(0.80, RatingLabel.LowRisk, RatingSeverity.Good),
        RatingTier(0.86, RatingLabel.ModerateRisk, RatingSeverity.Fair),
        RatingTier(0.95, RatingLabel.HighRisk, RatingSeverity.Poor),
        RatingTier(null, RatingLabel.VeryHighRisk, RatingSeverity.Severe),
    )

    fun waistHipRatio(sex: Sex): List<RatingTier> = when (sex) {
        Sex.Male -> maleWaistHipRatio
        Sex.Female -> femaleWaistHipRatio
    }

    val waistHeightRatio: List<RatingTier> = listOf(
        RatingTier(0.40, RatingLabel.UnderweightRisk, RatingSeverity.Fair),
        RatingTier(0.50, RatingLabel.Healthy, RatingSeverity.Good),
        RatingTier(0.60, RatingLabel.IncreasedRisk, RatingSeverity.Fair),
        RatingTier(0.70, RatingLabel.HighRisk, RatingSeverity.Poor),
        RatingTier(null, RatingLabel.VeryHighRisk, RatingSeverity.Severe),
    )
}