package de.t_animal.opensourcebodytracker.core.model

enum class RatingSeverity { Good, Fair, Poor, Severe }

data class MetricRating(val label: String, val severity: RatingSeverity)
