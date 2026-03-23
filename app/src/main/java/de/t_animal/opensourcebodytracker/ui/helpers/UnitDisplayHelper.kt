package de.t_animal.opensourcebodytracker.ui.helpers

import de.t_animal.opensourcebodytracker.core.model.BodyMetricUnit
import de.t_animal.opensourcebodytracker.core.model.UnitSystem

fun BodyMetricUnit.displaySymbol(unitSystem: UnitSystem): String = when (unitSystem) {
    UnitSystem.Metric -> symbol
    UnitSystem.Imperial -> when (this) {
        BodyMetricUnit.Kilogram -> "lbs"
        BodyMetricUnit.Centimeter -> "in"
        BodyMetricUnit.Millimeter -> symbol
        BodyMetricUnit.Percent -> symbol
        BodyMetricUnit.Unitless -> symbol
    }
}
