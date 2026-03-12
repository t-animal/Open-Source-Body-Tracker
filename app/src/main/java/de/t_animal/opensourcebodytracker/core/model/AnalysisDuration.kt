package de.t_animal.opensourcebodytracker.core.model

enum class AnalysisDuration(
    val label: String,
) {
    OneMonth("1M"),
    ThreeMonths("3M"),
    SixMonths("6M"),
    OneYear("1Y"),
    All("All"),
}
