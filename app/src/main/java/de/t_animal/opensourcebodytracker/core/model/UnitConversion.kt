package de.t_animal.opensourcebodytracker.core.model

private const val KG_TO_LBS = 2.20462
private const val CM_TO_IN = 1.0 / 2.54
private const val IN_PER_FOOT = 12

fun Double.toDisplayValue(unit: BodyMetricUnit, unitSystem: UnitSystem): Double =
    if (unitSystem == UnitSystem.Metric) {
        this // app uses metric unit system internally
    } else {
        when (unit) {
            BodyMetricUnit.Kilogram -> this * KG_TO_LBS
            BodyMetricUnit.Centimeter -> this * CM_TO_IN
            else -> this
        }
    }

fun Double.userInputToStorageValue(unit: BodyMetricUnit, unitSystem: UnitSystem): Double =
    if (unitSystem == UnitSystem.Metric) {
        this // app uses metric unit system internally
    } else {
        when (unit) {
            BodyMetricUnit.Kilogram -> this / KG_TO_LBS
            BodyMetricUnit.Centimeter -> this / CM_TO_IN
            else -> this
        }
    }

fun cmToFeetAndInches(cm: Float): Pair<Int, Double> {
    val totalInches = cm / 2.54
    val feet = (totalInches / IN_PER_FOOT).toInt()
    val inches = totalInches - (feet * IN_PER_FOOT)
    return feet to inches
}

fun feetAndInchesToCm(feet: Int, inches: Double): Float {
    return ((feet * IN_PER_FOOT + inches) * 2.54).toFloat()
}
