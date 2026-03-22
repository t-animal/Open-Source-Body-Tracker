package de.t_animal.opensourcebodytracker.domain.metrics

import de.t_animal.opensourcebodytracker.core.model.DerivedMetricRatings
import de.t_animal.opensourcebodytracker.core.model.DerivedMetrics
import de.t_animal.opensourcebodytracker.core.model.MetricRating
import de.t_animal.opensourcebodytracker.core.model.RatingLabel
import de.t_animal.opensourcebodytracker.core.model.RatingSeverity
import de.t_animal.opensourcebodytracker.core.model.Sex
import javax.inject.Inject

class DerivedMetricsRater @Inject constructor() {
    fun rate(sex: Sex, metrics: DerivedMetrics): DerivedMetricRatings =
        DerivedMetricRatings(
            bmi = metrics.bmi?.let { rateBmi(it) },
            navyBodyFatPercent = metrics.navyBodyFatPercent?.let { rateBodyFatPercent(it, sex) },
            skinfold3SiteBodyFatPercent = metrics.skinfold3SiteBodyFatPercent?.let { rateBodyFatPercent(it, sex) },
            waistHipRatio = metrics.waistHipRatio?.let { rateWaistHipRatio(it, sex) },
            waistHeightRatio = metrics.waistHeightRatio?.let { rateWaistHeightRatio(it) },
        )

    private fun rateBmi(bmi: Double): MetricRating? {
        if (bmi < 10 || bmi > 80) return null
        return when {
            bmi < 16.0 -> MetricRating(RatingLabel.SevereUnderweight, RatingSeverity.Severe)
            bmi < 18.5 -> MetricRating(RatingLabel.Underweight, RatingSeverity.Poor)
            bmi < 25.0 -> MetricRating(RatingLabel.Normal, RatingSeverity.Good)
            bmi < 30.0 -> MetricRating(RatingLabel.Overweight, RatingSeverity.Fair)
            bmi < 35.0 -> MetricRating(RatingLabel.ObeseClassI, RatingSeverity.Poor)
            bmi < 40.0 -> MetricRating(RatingLabel.ObeseClassII, RatingSeverity.Poor)
            else -> MetricRating(RatingLabel.ObeseClassIII, RatingSeverity.Severe)
        }
    }

    private fun rateBodyFatPercent(percent: Double, sex: Sex): MetricRating? {
        if (percent < 1 || percent > 70) return null
        return when (sex) {
            Sex.Male -> when {
                percent < 3.0 -> MetricRating(RatingLabel.DangerouslyLow, RatingSeverity.Severe)
                percent < 6.0 -> MetricRating(RatingLabel.EssentialFat, RatingSeverity.Poor)
                percent < 14.0 -> MetricRating(RatingLabel.Athletic, RatingSeverity.Good)
                percent < 18.0 -> MetricRating(RatingLabel.Fit, RatingSeverity.Good)
                percent < 25.0 -> MetricRating(RatingLabel.Acceptable, RatingSeverity.Fair)
                else -> MetricRating(RatingLabel.Obese, RatingSeverity.Poor)
            }
            Sex.Female -> when {
                percent < 10.0 -> MetricRating(RatingLabel.DangerouslyLow, RatingSeverity.Severe)
                percent < 14.0 -> MetricRating(RatingLabel.EssentialFat, RatingSeverity.Poor)
                percent < 21.0 -> MetricRating(RatingLabel.Athletic, RatingSeverity.Good)
                percent < 25.0 -> MetricRating(RatingLabel.Fit, RatingSeverity.Good)
                percent < 32.0 -> MetricRating(RatingLabel.Acceptable, RatingSeverity.Fair)
                else -> MetricRating(RatingLabel.Obese, RatingSeverity.Poor)
            }
        }
    }

    private fun rateWaistHipRatio(ratio: Double, sex: Sex): MetricRating? {
        if (ratio < 0.4 || ratio > 2) return null
        return when (sex) {
            Sex.Male -> when {
                ratio < 0.90 -> MetricRating(RatingLabel.LowRisk, RatingSeverity.Good)
                ratio < 1.00 -> MetricRating(RatingLabel.ModerateRisk, RatingSeverity.Fair)
                ratio < 1.10 -> MetricRating(RatingLabel.HighRisk, RatingSeverity.Poor)
                else -> MetricRating(RatingLabel.VeryHighRisk, RatingSeverity.Severe)
            }
            Sex.Female -> when {
                ratio < 0.80 -> MetricRating(RatingLabel.LowRisk, RatingSeverity.Good)
                ratio < 0.86 -> MetricRating(RatingLabel.ModerateRisk, RatingSeverity.Fair)
                ratio < 0.95 -> MetricRating(RatingLabel.HighRisk, RatingSeverity.Poor)
                else -> MetricRating(RatingLabel.VeryHighRisk, RatingSeverity.Severe)
            }
        }
    }

    private fun rateWaistHeightRatio(ratio: Double): MetricRating? {
        if (ratio < 0.2 || ratio > 1) return null
        return when {
            ratio < 0.40 -> MetricRating(RatingLabel.UnderweightRisk, RatingSeverity.Fair)
            ratio < 0.50 -> MetricRating(RatingLabel.Healthy, RatingSeverity.Good)
            ratio < 0.60 -> MetricRating(RatingLabel.IncreasedRisk, RatingSeverity.Fair)
            ratio < 0.70 -> MetricRating(RatingLabel.HighRisk, RatingSeverity.Poor)
            else -> MetricRating(RatingLabel.VeryHighRisk, RatingSeverity.Severe)
        }
    }
}
