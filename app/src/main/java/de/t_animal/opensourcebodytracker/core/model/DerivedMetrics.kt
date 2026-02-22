package de.t_animal.opensourcebodytracker.core.model

data class DerivedMetrics(
    val bmi: Double? = null,
    val bodyFatPercent: Double? = null,
    val waistHipRatio: Double? = null,
    val waistHeightRatio: Double? = null,
    val hipHeightRatio: Double? = null,
)
