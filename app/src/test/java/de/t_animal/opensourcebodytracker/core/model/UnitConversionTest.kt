package de.t_animal.opensourcebodytracker.core.model

import de.t_animal.opensourcebodytracker.ui.helpers.displaySymbol
import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConversionTest {

    @Test
    fun `displaySymbol returns metric symbols for Metric system`() {
        assertEquals("kg", BodyMetricUnit.Kilogram.displaySymbol(UnitSystem.Metric))
        assertEquals("cm", BodyMetricUnit.Centimeter.displaySymbol(UnitSystem.Metric))
        assertEquals("mm", BodyMetricUnit.Millimeter.displaySymbol(UnitSystem.Metric))
        assertEquals("%", BodyMetricUnit.Percent.displaySymbol(UnitSystem.Metric))
        assertEquals("", BodyMetricUnit.Unitless.displaySymbol(UnitSystem.Metric))
    }

    @Test
    fun `displaySymbol returns imperial symbols for Imperial system`() {
        assertEquals("lbs", BodyMetricUnit.Kilogram.displaySymbol(UnitSystem.Imperial))
        assertEquals("in", BodyMetricUnit.Centimeter.displaySymbol(UnitSystem.Imperial))
        assertEquals("mm", BodyMetricUnit.Millimeter.displaySymbol(UnitSystem.Imperial))
        assertEquals("%", BodyMetricUnit.Percent.displaySymbol(UnitSystem.Imperial))
        assertEquals("", BodyMetricUnit.Unitless.displaySymbol(UnitSystem.Imperial))
    }

    @Test
    fun `toDisplayValue is identity for Metric system`() {
        assertEquals(80.0, 80.0.toDisplayValue(BodyMetricUnit.Kilogram, UnitSystem.Metric), 0.0)
        assertEquals(170.0, 170.0.toDisplayValue(BodyMetricUnit.Centimeter, UnitSystem.Metric), 0.0)
    }

    @Test
    fun `toDisplayValue converts kg to lbs for Imperial`() {
        assertEquals(176.37, 80.0.toDisplayValue(BodyMetricUnit.Kilogram, UnitSystem.Imperial), 0.01)
    }

    @Test
    fun `toDisplayValue converts cm to inches for Imperial`() {
        assertEquals(66.93, 170.0.toDisplayValue(BodyMetricUnit.Centimeter, UnitSystem.Imperial), 0.01)
    }

    @Test
    fun `toDisplayValue does not convert mm for Imperial`() {
        assertEquals(12.0, 12.0.toDisplayValue(BodyMetricUnit.Millimeter, UnitSystem.Imperial), 0.0)
    }

    @Test
    fun `toDisplayValue does not convert percent for Imperial`() {
        assertEquals(25.0, 25.0.toDisplayValue(BodyMetricUnit.Percent, UnitSystem.Imperial), 0.0)
    }

    @Test
    fun `toStorageValue is identity for Metric system`() {
        assertEquals(80.0, 80.0.userInputToStorageValue(BodyMetricUnit.Kilogram, UnitSystem.Metric), 0.0)
    }

    @Test
    fun `toStorageValue converts lbs to kg for Imperial`() {
        assertEquals(80.0, 176.37.userInputToStorageValue(BodyMetricUnit.Kilogram, UnitSystem.Imperial), 0.01)
    }

    @Test
    fun `toStorageValue converts inches to cm for Imperial`() {
        assertEquals(170.0, 66.93.userInputToStorageValue(BodyMetricUnit.Centimeter, UnitSystem.Imperial), 0.01)
    }

    @Test
    fun `round-trip kg to lbs and back preserves value`() {
        val originalKg = 75.5
        val lbs = originalKg.toDisplayValue(BodyMetricUnit.Kilogram, UnitSystem.Imperial)
        val backToKg = lbs.userInputToStorageValue(BodyMetricUnit.Kilogram, UnitSystem.Imperial)
        assertEquals(originalKg, backToKg, 1e-10)
    }

    @Test
    fun `round-trip cm to inches and back preserves value`() {
        val originalCm = 90.2
        val inches = originalCm.toDisplayValue(BodyMetricUnit.Centimeter, UnitSystem.Imperial)
        val backToCm = inches.userInputToStorageValue(BodyMetricUnit.Centimeter, UnitSystem.Imperial)
        assertEquals(originalCm, backToCm, 1e-10)
    }

    @Test
    fun `cmToFeetAndInches converts correctly`() {
        val (feet, inches) = cmToFeetAndInches(177.8f)
        assertEquals(5, feet)
        assertEquals(10.0, inches, 0.1)
    }

    @Test
    fun `feetAndInchesToCm converts correctly`() {
        val cm = feetAndInchesToCm(5, 10.0)
        assertEquals(177.8f, cm, 0.1f)
    }

    @Test
    fun `height round-trip feet-inches to cm and back preserves value`() {
        val originalFeet = 6
        val originalInches = 1.5
        val cm = feetAndInchesToCm(originalFeet, originalInches)
        val (feet, inches) = cmToFeetAndInches(cm)
        assertEquals(originalFeet, feet)
        assertEquals(originalInches, inches, 0.01)
    }

    @Test
    fun `toDisplayValue handles zero`() {
        assertEquals(0.0, 0.0.toDisplayValue(BodyMetricUnit.Kilogram, UnitSystem.Imperial), 0.0)
        assertEquals(0.0, 0.0.toDisplayValue(BodyMetricUnit.Centimeter, UnitSystem.Imperial), 0.0)
    }

    @Test
    fun `toStorageValue handles zero`() {
        assertEquals(0.0, 0.0.userInputToStorageValue(BodyMetricUnit.Kilogram, UnitSystem.Imperial), 0.0)
        assertEquals(0.0, 0.0.userInputToStorageValue(BodyMetricUnit.Centimeter, UnitSystem.Imperial), 0.0)
    }
}
