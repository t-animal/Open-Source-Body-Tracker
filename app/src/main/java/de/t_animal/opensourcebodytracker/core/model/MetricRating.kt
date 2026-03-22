package de.t_animal.opensourcebodytracker.core.model

enum class RatingSeverity { Good, Fair, Poor, Severe }

enum class RatingLabel {
    // BMI
    SevereUnderweight,
    Underweight,
    Normal,
    Overweight,
    ObeseClassI,
    ObeseClassII,
    ObeseClassIII,

    // Body Fat
    DangerouslyLow,
    EssentialFat,
    Athletic,
    Fit,
    Acceptable,
    Obese,

    // WHR / WHtR risk
    LowRisk,
    ModerateRisk,
    HighRisk,
    VeryHighRisk,

    // WHtR specific
    UnderweightRisk,
    Healthy,
    IncreasedRisk,
}

data class MetricRating(val label: RatingLabel, val severity: RatingSeverity)
