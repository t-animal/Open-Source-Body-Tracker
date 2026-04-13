package de.t_animal.opensourcebodytracker.feature.measurements.helpers

import androidx.annotation.StringRes
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.RatingLabel
import de.t_animal.opensourcebodytracker.core.model.RatingSeverity
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.domain.metrics.RatingTier
import de.t_animal.opensourcebodytracker.domain.metrics.RatingTiers
import kotlin.math.pow

internal data class RatingTableEntry(
    val label: RatingLabel,
    val severity: RatingSeverity,
    val rangeText: String,
    @param:StringRes val descriptionRes: Int,
)

internal data class RatingTableSection(
    @param:StringRes val titleRes: Int,
    val entries: List<RatingTableEntry>,
)

private fun List<RatingTier>.toTableEntries(
    decimals: Int,
    suffix: String = "",
    descriptionResForTier: (RatingTier) -> Int,
): List<RatingTableEntry> {
    val fmt = "%.${decimals}f"
    val step = 10.0.pow(-decimals)
    return mapIndexed { index, tier ->
        val rangeText = when {
            index == 0 -> "< ${fmt.format(tier.upToExclusive!!)}$suffix"
            tier.upToExclusive == null -> "\u2265 ${fmt.format(this[index - 1].upToExclusive!!)}$suffix"
            else -> {
                val low = this[index - 1].upToExclusive!!
                val high = tier.upToExclusive - step
                "${fmt.format(low)}\u2013${fmt.format(high)}$suffix"
            }
        }
        RatingTableEntry(tier.label, tier.severity, rangeText, descriptionResForTier(tier))
    }
}

private fun bmiDescriptionRes(label: RatingLabel): Int = when (label) {
    RatingLabel.SevereUnderweight -> R.string.rating_desc_bmi_severe_underweight
    RatingLabel.Underweight -> R.string.rating_desc_bmi_underweight
    RatingLabel.Normal -> R.string.rating_desc_bmi_normal
    RatingLabel.Overweight -> R.string.rating_desc_bmi_overweight
    RatingLabel.ObeseClassI -> R.string.rating_desc_bmi_obese_class_i
    RatingLabel.ObeseClassII -> R.string.rating_desc_bmi_obese_class_ii
    RatingLabel.ObeseClassIII -> R.string.rating_desc_bmi_obese_class_iii
    else -> error("Unexpected BMI label: $label")
}

private fun bodyFatDescriptionRes(sex: Sex, label: RatingLabel): Int = when (sex) {
    Sex.Male -> when (label) {
        RatingLabel.DangerouslyLow -> R.string.rating_desc_bodyfat_male_dangerously_low
        RatingLabel.EssentialFat -> R.string.rating_desc_bodyfat_male_essential_fat
        RatingLabel.Athletic -> R.string.rating_desc_bodyfat_male_athletic
        RatingLabel.Fit -> R.string.rating_desc_bodyfat_male_fit
        RatingLabel.Acceptable -> R.string.rating_desc_bodyfat_male_acceptable
        RatingLabel.Obese -> R.string.rating_desc_bodyfat_male_high_body_fat
        else -> error("Unexpected male body fat label: $label")
    }
    Sex.Female -> when (label) {
        RatingLabel.DangerouslyLow -> R.string.rating_desc_bodyfat_female_dangerously_low
        RatingLabel.EssentialFat -> R.string.rating_desc_bodyfat_female_essential_fat
        RatingLabel.Athletic -> R.string.rating_desc_bodyfat_female_athletic
        RatingLabel.Fit -> R.string.rating_desc_bodyfat_female_fit
        RatingLabel.Acceptable -> R.string.rating_desc_bodyfat_female_acceptable
        RatingLabel.Obese -> R.string.rating_desc_bodyfat_female_high_body_fat
        else -> error("Unexpected female body fat label: $label")
    }
}

private fun waistHipRatioDescriptionRes(sex: Sex, label: RatingLabel): Int = when (sex) {
    Sex.Male -> when (label) {
        RatingLabel.LowRisk -> R.string.rating_desc_whr_male_low_risk
        RatingLabel.ModerateRisk -> R.string.rating_desc_whr_male_moderate_risk
        RatingLabel.HighRisk -> R.string.rating_desc_whr_male_high_risk
        RatingLabel.VeryHighRisk -> R.string.rating_desc_whr_male_very_high_risk
        else -> error("Unexpected male WHR label: $label")
    }
    Sex.Female -> when (label) {
        RatingLabel.LowRisk -> R.string.rating_desc_whr_female_low_risk
        RatingLabel.ModerateRisk -> R.string.rating_desc_whr_female_moderate_risk
        RatingLabel.HighRisk -> R.string.rating_desc_whr_female_high_risk
        RatingLabel.VeryHighRisk -> R.string.rating_desc_whr_female_very_high_risk
        else -> error("Unexpected female WHR label: $label")
    }
}

private fun waistHeightRatioDescriptionRes(label: RatingLabel): Int = when (label) {
    RatingLabel.UnderweightRisk -> R.string.rating_desc_whtr_underweight_risk
    RatingLabel.Healthy -> R.string.rating_desc_whtr_healthy
    RatingLabel.IncreasedRisk -> R.string.rating_desc_whtr_increased_risk
    RatingLabel.HighRisk -> R.string.rating_desc_whtr_high_risk
    RatingLabel.VeryHighRisk -> R.string.rating_desc_whtr_very_high_risk
    else -> error("Unexpected WHtR label: $label")
}

internal object RatingTableData {
    fun forBmi(): List<RatingTableEntry> =
        RatingTiers.bmi.toTableEntries(decimals = 1) { tier -> bmiDescriptionRes(tier.label) }

    fun forBodyFat(sex: Sex): List<RatingTableEntry> =
        RatingTiers.bodyFat(sex).toTableEntries(decimals = 1, suffix = "%") { tier ->
            bodyFatDescriptionRes(sex, tier.label)
        }

    fun forWaistHipRatio(sex: Sex): List<RatingTableEntry> =
        RatingTiers.waistHipRatio(sex).toTableEntries(decimals = 2) { tier ->
            waistHipRatioDescriptionRes(sex, tier.label)
        }

    fun forWaistHeightRatio(): List<RatingTableEntry> =
        RatingTiers.waistHeightRatio.toTableEntries(decimals = 2) { tier ->
            waistHeightRatioDescriptionRes(tier.label)
        }

    fun allTables(): List<RatingTableSection> = listOf(
        RatingTableSection(R.string.metric_fullname_bmi, forBmi()),
        RatingTableSection(R.string.health_rating_section_body_fat_male, forBodyFat(Sex.Male)),
        RatingTableSection(R.string.health_rating_section_body_fat_female, forBodyFat(Sex.Female)),
        RatingTableSection(R.string.health_rating_section_whr_male, forWaistHipRatio(Sex.Male)),
        RatingTableSection(R.string.health_rating_section_whr_female, forWaistHipRatio(Sex.Female)),
        RatingTableSection(R.string.metric_fullname_whtr, forWaistHeightRatio()),
    )
}