package de.t_animal.opensourcebodytracker.domain.metrics

import de.t_animal.opensourcebodytracker.core.model.DerivedMetrics
import de.t_animal.opensourcebodytracker.core.model.RatingLabel
import de.t_animal.opensourcebodytracker.core.model.RatingSeverity
import de.t_animal.opensourcebodytracker.core.model.Sex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DerivedMetricsRaterTest {
    private val rater = DerivedMetricsRater()

    // region BMI  (thresholds: 16.0 | 18.5 | 25.0 | 30.0 | 35.0 | 40.0, valid: 10..80)

    @Test fun rateBmi_returnsNull_at9d9() = assertNull(rater.rate(Sex.Male, metrics(bmi = 9.9)).bmi)

    @Test fun rateBmi_returnsSevereUnderweight_at10d0() = assertBmi(10.0, RatingLabel.SevereUnderweight, RatingSeverity.Severe)
    @Test fun rateBmi_returnsSevereUnderweight_at15d9() = assertBmi(15.9, RatingLabel.SevereUnderweight, RatingSeverity.Severe)

    @Test fun rateBmi_returnsUnderweight_at16d0() = assertBmi(16.0, RatingLabel.Underweight, RatingSeverity.Poor)
    @Test fun rateBmi_returnsUnderweight_at18d4() = assertBmi(18.4, RatingLabel.Underweight, RatingSeverity.Poor)

    @Test fun rateBmi_returnsNormal_at18d5() = assertBmi(18.5, RatingLabel.Normal, RatingSeverity.Good)
    @Test fun rateBmi_returnsNormal_at24d9() = assertBmi(24.9, RatingLabel.Normal, RatingSeverity.Good)

    @Test fun rateBmi_returnsOverweight_at25d0() = assertBmi(25.0, RatingLabel.Overweight, RatingSeverity.Fair)
    @Test fun rateBmi_returnsOverweight_at29d9() = assertBmi(29.9, RatingLabel.Overweight, RatingSeverity.Fair)

    @Test fun rateBmi_returnsObeseClassI_at30d0() = assertBmi(30.0, RatingLabel.ObeseClassI, RatingSeverity.Poor)
    @Test fun rateBmi_returnsObeseClassI_at34d9() = assertBmi(34.9, RatingLabel.ObeseClassI, RatingSeverity.Poor)

    @Test fun rateBmi_returnsObeseClassII_at35d0() = assertBmi(35.0, RatingLabel.ObeseClassII, RatingSeverity.Poor)
    @Test fun rateBmi_returnsObeseClassII_at39d9() = assertBmi(39.9, RatingLabel.ObeseClassII, RatingSeverity.Poor)

    @Test fun rateBmi_returnsObeseClassIII_at40d0() = assertBmi(40.0, RatingLabel.ObeseClassIII, RatingSeverity.Severe)
    @Test fun rateBmi_returnsObeseClassIII_at80d0() = assertBmi(80.0, RatingLabel.ObeseClassIII, RatingSeverity.Severe)

    @Test fun rateBmi_returnsNull_at80d1() = assertNull(rater.rate(Sex.Male, metrics(bmi = 80.1)).bmi)

    private fun assertBmi(value: Double, label: RatingLabel, severity: RatingSeverity) {
        val result = rater.rate(Sex.Male, metrics(bmi = value)).bmi
        assertEquals(label, result?.label)
        assertEquals(severity, result?.severity)
    }

    // endregion

    // region Body fat % — Male  (thresholds: 3.0 | 6.0 | 14.0 | 18.0 | 25.0, valid: 1..70)

    @Test fun rateBodyFat_male_returnsNull_at0d9() = assertNull(rater.rate(Sex.Male, metrics(navyBodyFatPercent = 0.9)).navyBodyFatPercent)

    @Test fun rateBodyFat_male_returnsDangerouslyLow_at1d0() = assertBodyFat(Sex.Male, 1.0, RatingLabel.DangerouslyLow, RatingSeverity.Severe)
    @Test fun rateBodyFat_male_returnsDangerouslyLow_at2d9() = assertBodyFat(Sex.Male, 2.9, RatingLabel.DangerouslyLow, RatingSeverity.Severe)

    @Test fun rateBodyFat_male_returnsEssentialFat_at3d0() = assertBodyFat(Sex.Male, 3.0, RatingLabel.EssentialFat, RatingSeverity.Poor)
    @Test fun rateBodyFat_male_returnsEssentialFat_at5d9() = assertBodyFat(Sex.Male, 5.9, RatingLabel.EssentialFat, RatingSeverity.Poor)

    @Test fun rateBodyFat_male_returnsAthletic_at6d0() = assertBodyFat(Sex.Male, 6.0, RatingLabel.Athletic, RatingSeverity.Good)
    @Test fun rateBodyFat_male_returnsAthletic_at13d9() = assertBodyFat(Sex.Male, 13.9, RatingLabel.Athletic, RatingSeverity.Good)

    @Test fun rateBodyFat_male_returnsFit_at14d0() = assertBodyFat(Sex.Male, 14.0, RatingLabel.Fit, RatingSeverity.Good)
    @Test fun rateBodyFat_male_returnsFit_at17d9() = assertBodyFat(Sex.Male, 17.9, RatingLabel.Fit, RatingSeverity.Good)

    @Test fun rateBodyFat_male_returnsAcceptable_at18d0() = assertBodyFat(Sex.Male, 18.0, RatingLabel.Acceptable, RatingSeverity.Fair)
    @Test fun rateBodyFat_male_returnsAcceptable_at24d9() = assertBodyFat(Sex.Male, 24.9, RatingLabel.Acceptable, RatingSeverity.Fair)

    @Test fun rateBodyFat_male_returnsObese_at25d0() = assertBodyFat(Sex.Male, 25.0, RatingLabel.Obese, RatingSeverity.Poor)
    @Test fun rateBodyFat_male_returnsObese_at70d0() = assertBodyFat(Sex.Male, 70.0, RatingLabel.Obese, RatingSeverity.Poor)

    @Test fun rateBodyFat_male_returnsNull_at70d1() = assertNull(rater.rate(Sex.Male, metrics(navyBodyFatPercent = 70.1)).navyBodyFatPercent)

    // endregion

    // region Body fat % — Female  (thresholds: 10.0 | 14.0 | 21.0 | 25.0 | 32.0, valid: 1..70)

    @Test fun rateBodyFat_female_returnsNull_at0d9() = assertNull(rater.rate(Sex.Female, metrics(navyBodyFatPercent = 0.9)).navyBodyFatPercent)

    @Test fun rateBodyFat_female_returnsDangerouslyLow_at1d0() = assertBodyFat(Sex.Female, 1.0, RatingLabel.DangerouslyLow, RatingSeverity.Severe)
    @Test fun rateBodyFat_female_returnsDangerouslyLow_at9d9() = assertBodyFat(Sex.Female, 9.9, RatingLabel.DangerouslyLow, RatingSeverity.Severe)

    @Test fun rateBodyFat_female_returnsEssentialFat_at10d0() = assertBodyFat(Sex.Female, 10.0, RatingLabel.EssentialFat, RatingSeverity.Poor)
    @Test fun rateBodyFat_female_returnsEssentialFat_at13d9() = assertBodyFat(Sex.Female, 13.9, RatingLabel.EssentialFat, RatingSeverity.Poor)

    @Test fun rateBodyFat_female_returnsAthletic_at14d0() = assertBodyFat(Sex.Female, 14.0, RatingLabel.Athletic, RatingSeverity.Good)
    @Test fun rateBodyFat_female_returnsAthletic_at20d9() = assertBodyFat(Sex.Female, 20.9, RatingLabel.Athletic, RatingSeverity.Good)

    @Test fun rateBodyFat_female_returnsFit_at21d0() = assertBodyFat(Sex.Female, 21.0, RatingLabel.Fit, RatingSeverity.Good)
    @Test fun rateBodyFat_female_returnsFit_at24d9() = assertBodyFat(Sex.Female, 24.9, RatingLabel.Fit, RatingSeverity.Good)

    @Test fun rateBodyFat_female_returnsAcceptable_at25d0() = assertBodyFat(Sex.Female, 25.0, RatingLabel.Acceptable, RatingSeverity.Fair)
    @Test fun rateBodyFat_female_returnsAcceptable_at31d9() = assertBodyFat(Sex.Female, 31.9, RatingLabel.Acceptable, RatingSeverity.Fair)

    @Test fun rateBodyFat_female_returnsObese_at32d0() = assertBodyFat(Sex.Female, 32.0, RatingLabel.Obese, RatingSeverity.Poor)
    @Test fun rateBodyFat_female_returnsObese_at70d0() = assertBodyFat(Sex.Female, 70.0, RatingLabel.Obese, RatingSeverity.Poor)

    @Test fun rateBodyFat_female_returnsNull_at70d1() = assertNull(rater.rate(Sex.Female, metrics(navyBodyFatPercent = 70.1)).navyBodyFatPercent)

    private fun assertBodyFat(sex: Sex, value: Double, label: RatingLabel, severity: RatingSeverity) {
        val result = rater.rate(sex, metrics(navyBodyFatPercent = value)).navyBodyFatPercent
        assertEquals(label, result?.label)
        assertEquals(severity, result?.severity)
    }

    // endregion

    // region WHR — Male  (thresholds: 0.90 | 1.00 | 1.10, valid: 0.4..2.0)

    @Test fun rateWhr_male_returnsNull_at0d39() = assertNull(rater.rate(Sex.Male, metrics(waistHipRatio = 0.39)).waistHipRatio)

    @Test fun rateWhr_male_returnsLowRisk_at0d40() = assertWhr(Sex.Male, 0.40, RatingLabel.LowRisk, RatingSeverity.Good)
    @Test fun rateWhr_male_returnsLowRisk_at0d89() = assertWhr(Sex.Male, 0.89, RatingLabel.LowRisk, RatingSeverity.Good)

    @Test fun rateWhr_male_returnsModerateRisk_at0d90() = assertWhr(Sex.Male, 0.90, RatingLabel.ModerateRisk, RatingSeverity.Fair)
    @Test fun rateWhr_male_returnsModerateRisk_at0d99() = assertWhr(Sex.Male, 0.99, RatingLabel.ModerateRisk, RatingSeverity.Fair)

    @Test fun rateWhr_male_returnsHighRisk_at1d00() = assertWhr(Sex.Male, 1.00, RatingLabel.HighRisk, RatingSeverity.Poor)
    @Test fun rateWhr_male_returnsHighRisk_at1d09() = assertWhr(Sex.Male, 1.09, RatingLabel.HighRisk, RatingSeverity.Poor)

    @Test fun rateWhr_male_returnsVeryHighRisk_at1d10() = assertWhr(Sex.Male, 1.10, RatingLabel.VeryHighRisk, RatingSeverity.Severe)
    @Test fun rateWhr_male_returnsVeryHighRisk_at2d00() = assertWhr(Sex.Male, 2.00, RatingLabel.VeryHighRisk, RatingSeverity.Severe)

    @Test fun rateWhr_male_returnsNull_at2d01() = assertNull(rater.rate(Sex.Male, metrics(waistHipRatio = 2.01)).waistHipRatio)

    // endregion

    // region WHR — Female  (thresholds: 0.80 | 0.86 | 0.95, valid: 0.4..2.0)

    @Test fun rateWhr_female_returnsNull_at0d39() = assertNull(rater.rate(Sex.Female, metrics(waistHipRatio = 0.39)).waistHipRatio)

    @Test fun rateWhr_female_returnsLowRisk_at0d40() = assertWhr(Sex.Female, 0.40, RatingLabel.LowRisk, RatingSeverity.Good)
    @Test fun rateWhr_female_returnsLowRisk_at0d79() = assertWhr(Sex.Female, 0.79, RatingLabel.LowRisk, RatingSeverity.Good)

    @Test fun rateWhr_female_returnsModerateRisk_at0d80() = assertWhr(Sex.Female, 0.80, RatingLabel.ModerateRisk, RatingSeverity.Fair)
    @Test fun rateWhr_female_returnsModerateRisk_at0d85() = assertWhr(Sex.Female, 0.85, RatingLabel.ModerateRisk, RatingSeverity.Fair)

    @Test fun rateWhr_female_returnsHighRisk_at0d86() = assertWhr(Sex.Female, 0.86, RatingLabel.HighRisk, RatingSeverity.Poor)
    @Test fun rateWhr_female_returnsHighRisk_at0d94() = assertWhr(Sex.Female, 0.94, RatingLabel.HighRisk, RatingSeverity.Poor)

    @Test fun rateWhr_female_returnsVeryHighRisk_at0d95() = assertWhr(Sex.Female, 0.95, RatingLabel.VeryHighRisk, RatingSeverity.Severe)
    @Test fun rateWhr_female_returnsVeryHighRisk_at2d00() = assertWhr(Sex.Female, 2.00, RatingLabel.VeryHighRisk, RatingSeverity.Severe)

    @Test fun rateWhr_female_returnsNull_at2d01() = assertNull(rater.rate(Sex.Female, metrics(waistHipRatio = 2.01)).waistHipRatio)

    private fun assertWhr(sex: Sex, value: Double, label: RatingLabel, severity: RatingSeverity) {
        val result = rater.rate(sex, metrics(waistHipRatio = value)).waistHipRatio
        assertEquals(label, result?.label)
        assertEquals(severity, result?.severity)
    }

    // endregion

    // region WHtR  (thresholds: 0.40 | 0.50 | 0.60 | 0.70, valid: 0.2..1.0)

    @Test fun rateWhtr_returnsNull_at0d19() = assertNull(rater.rate(Sex.Male, metrics(waistHeightRatio = 0.19)).waistHeightRatio)

    @Test fun rateWhtr_returnsUnderweightRisk_at0d20() = assertWhtr(0.20, RatingLabel.UnderweightRisk, RatingSeverity.Fair)
    @Test fun rateWhtr_returnsUnderweightRisk_at0d39() = assertWhtr(0.39, RatingLabel.UnderweightRisk, RatingSeverity.Fair)

    @Test fun rateWhtr_returnsHealthy_at0d40() = assertWhtr(0.40, RatingLabel.Healthy, RatingSeverity.Good)
    @Test fun rateWhtr_returnsHealthy_at0d49() = assertWhtr(0.49, RatingLabel.Healthy, RatingSeverity.Good)

    @Test fun rateWhtr_returnsIncreasedRisk_at0d50() = assertWhtr(0.50, RatingLabel.IncreasedRisk, RatingSeverity.Fair)
    @Test fun rateWhtr_returnsIncreasedRisk_at0d59() = assertWhtr(0.59, RatingLabel.IncreasedRisk, RatingSeverity.Fair)

    @Test fun rateWhtr_returnsHighRisk_at0d60() = assertWhtr(0.60, RatingLabel.HighRisk, RatingSeverity.Poor)
    @Test fun rateWhtr_returnsHighRisk_at0d69() = assertWhtr(0.69, RatingLabel.HighRisk, RatingSeverity.Poor)

    @Test fun rateWhtr_returnsVeryHighRisk_at0d70() = assertWhtr(0.70, RatingLabel.VeryHighRisk, RatingSeverity.Severe)
    @Test fun rateWhtr_returnsVeryHighRisk_at1d00() = assertWhtr(1.00, RatingLabel.VeryHighRisk, RatingSeverity.Severe)

    @Test fun rateWhtr_returnsNull_at1d01() = assertNull(rater.rate(Sex.Male, metrics(waistHeightRatio = 1.01)).waistHeightRatio)

    private fun assertWhtr(value: Double, label: RatingLabel, severity: RatingSeverity) {
        val result = rater.rate(Sex.Male, metrics(waistHeightRatio = value)).waistHeightRatio
        assertEquals(label, result?.label)
        assertEquals(severity, result?.severity)
    }

    // endregion

    private fun metrics(
        bmi: Double? = null,
        navyBodyFatPercent: Double? = null,
        skinfold3SiteBodyFatPercent: Double? = null,
        waistHipRatio: Double? = null,
        waistHeightRatio: Double? = null,
    ) = DerivedMetrics(
        bmi = bmi,
        navyBodyFatPercent = navyBodyFatPercent,
        skinfold3SiteBodyFatPercent = skinfold3SiteBodyFatPercent,
        waistHipRatio = waistHipRatio,
        waistHeightRatio = waistHeightRatio,
    )
}
