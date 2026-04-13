package de.t_animal.opensourcebodytracker.domain.metrics

import de.t_animal.opensourcebodytracker.core.model.DerivedMetricRatings
import de.t_animal.opensourcebodytracker.core.model.DerivedMetrics
import de.t_animal.opensourcebodytracker.core.model.MetricRating
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
        return rate(RatingTiers.bmi, bmi)
    }

    private fun rateBodyFatPercent(percent: Double, sex: Sex): MetricRating? {
        if (percent < 1 || percent > 70) return null
        return rate(RatingTiers.bodyFat(sex), percent)
    }

    private fun rateWaistHipRatio(ratio: Double, sex: Sex): MetricRating? {
        if (ratio < 0.4 || ratio > 2) return null
        return rate(RatingTiers.waistHipRatio(sex), ratio)
    }

    private fun rateWaistHeightRatio(ratio: Double): MetricRating? {
        if (ratio < 0.2 || ratio > 1) return null
        return rate(RatingTiers.waistHeightRatio, ratio)
    }
    
    private fun rate(tiers: List<RatingTier>, value: Double): MetricRating {
        val tier = tiers.first { it.upToExclusive == null || value < it.upToExclusive }
        return MetricRating(tier.label, tier.severity)
    }
}
