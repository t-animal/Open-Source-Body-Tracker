# Derived Metric Formulas

All derived metrics are computed at runtime by `DerivedMetricsCalculator`. They are never stored — recalculated from raw measurements and user profile on each display.

All inputs are metric: weight in kg, lengths in cm, skinfolds in mm, age in years.

---

## BMI (Body Mass Index)

```
BMI = weightKg / (heightCm / 100)^2
```

Returns `null` if weight or height is missing or <= 0.

---

## Navy Body Fat %

Based on the U.S. Navy circumference-based estimation formula.

### Male

```
bodyFat% = 86.010 * log10(waistCm - neckCm)
         - 70.041 * log10(heightCm)
         + 30.30
```

Requires: `(waistCm - neckCm) > 0`

### Female

```
bodyFat% = 163.205 * log10(waistCm + hipCm - neckCm)
         -  97.684 * log10(heightCm)
         - 104.912
```

Requires: `(waistCm + hipCm - neckCm) > 0`, hip circumference provided.

Returns `null` if any required input is missing or <= 0.

---

## Waist-Hip Ratio (WHR)

```
WHR = waistCm / hipCm
```

Returns `null` if either is missing or <= 0.

---

## Waist-Height Ratio (WHtR)

```
WHtR = waistCm / heightCm
```

Returns `null` if waist is missing or <= 0, or height <= 0.

---

## Jackson & Pollock 3-Site Skinfold

Estimates body density from three skinfold sites, then converts to body fat % via the Siri equation.

### Step 1: Sum of 3 Skinfolds

| Sex | Sites (mm) |
|-----|-----------|
| Male | Chest + Abdomen + Thigh |
| Female | Triceps + Suprailiac + Thigh |

### Step 2: Body Density

**Male:**
```
bodyDensity = 1.10938
            - 0.0008267 * sum3
            + 0.0000016 * sum3^2
            - 0.0002574 * ageYears
```

**Female:**
```
bodyDensity = 1.0994921
            - 0.0009929 * sum3
            + 0.0000023 * sum3^2
            - 0.0001392 * ageYears
```

### Step 3: Siri Equation

```
bodyFat% = (495 / bodyDensity) - 450
```

Returns `null` if age is missing/<=0, any required skinfold is missing/<=0, or body density <=0.

---

## Validation Rules

All formulas require their inputs to be > 0. Null inputs produce null outputs (metrics remain nullable).

## Rounding (UI display)

| Metric | Decimal places |
|--------|---------------|
| BMI | 1 |
| Body Fat % | 1 |
| WHR, WHtR | 2 |

## Source File

`domain/metrics/DerivedMetricsCalculator.kt`
