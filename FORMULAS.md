## Phase 2 – Derived Metrics (Implementation Formulas)

Below are the exact mathematical formulas for all derived metrics introduced in Phase 2.
They are written in an implementation-friendly format so a coding agent can translate them directly into Kotlin.

All formulas assume:

* `weightKg`
* `heightCm`
* `waistCm`
* `hipCm`
* `neckCm`
* `sex` (MALE / FEMALE)

Height must be converted to meters where required.

---

# 1️⃣ Body Mass Index (BMI)

### Formula

```
heightM = heightCm / 100

BMI = weightKg / (heightM * heightM)
```

### Kotlin-like Implementation

```kotlin
val heightM = heightCm / 100.0
val bmi = weightKg / (heightM * heightM)
```

---

# 2️⃣ Estimated Body Fat – Navy Method

Based on the U.S. Navy body fat estimation formula.

### For Males

```
bodyFat% =
86.010 * log10(waistCm - neckCm)
- 70.041 * log10(heightCm)
+ 30.30
```

### For Females

```
bodyFat% =
163.205 * log10(waistCm + hipCm - neckCm)
- 97.684 * log10(heightCm)
- 104.912
```

### Notes

* `log10()` is base-10 logarithm
* All values must be in centimeters
* `(waistCm - neckCm)` and `(waistCm + hipCm - neckCm)` must be > 0

### Kotlin-like Implementation

```kotlin
import kotlin.math.log10

val bodyFat = when (sex) {
    Sex.MALE -> {
        86.010 * log10(waistCm - neckCm) -
        70.041 * log10(heightCm) +
        36.76
    }
    Sex.FEMALE -> {
        163.205 * log10(waistCm + hipCm - neckCm) -
        97.684 * log10(heightCm) -
        78.387
    }
}
```

---

# 3️⃣ Waist–Hip Ratio (WHR)

### Formula

```
WHR = waistCm / hipCm
```

### Kotlin

```kotlin
val whr = waistCm / hipCm
```

---

# 4️⃣ Waist–Height Ratio (WHtR)

### Formula

```
WHtR = waistCm / heightCm
```

### Kotlin

```kotlin
val whtr = waistCm / heightCm
```

---

# 5️⃣ Hip–Height Ratio (HHR)

### Formula

```
HHR = hipCm / heightCm
```

### Kotlin

```kotlin
val hhr = hipCm / heightCm
```

---

## 6️⃣ Jackson & Pollock 3-Site Skinfold Method

This section defines the formulas required to implement the **3-site skinfold body fat estimation** based on the equations developed by Jackson & Pollock.

The method estimates:

1. **Body Density**
2. **Body Fat Percentage** (using Siri equation)

---

### 📌 Required Inputs

* `sex` (MALE / FEMALE)
* `ageYears`
* Skinfold measurements in **millimeters (mm)**

#### Measurement Sites

**Male (3-site):**

* Chest
* Abdomen
* Thigh

**Female (3-site):**

* Triceps
* Suprailiac
* Thigh

---

### 1️⃣ Step 1 – Sum of Skinfolds

```text
sum3 = site1 + site2 + site3
```

Example (male):

```kotlin
val sum3 = chestMm + abdomenMm + thighMm
```

---

### 2️⃣ Step 2 – Body Density Calculation

#### Male Formula

```text
bodyDensity =
1.10938
- (0.0008267 * sum3)
+ (0.0000016 * sum3²)
- (0.0002574 * ageYears)
```

#### Female Formula

```text
bodyDensity =
1.0994921
- (0.0009929 * sum3)
+ (0.0000023 * sum3²)
- (0.0001392 * ageYears)
```

#### Kotlin Implementation

```kotlin
val bodyDensity = when (sex) {
    Sex.MALE -> {
        1.10938 -
        (0.0008267 * sum3) +
        (0.0000016 * sum3 * sum3) -
        (0.0002574 * ageYears)
    }
    Sex.FEMALE -> {
        1.0994921 -
        (0.0009929 * sum3) +
        (0.0000023 * sum3 * sum3) -
        (0.0001392 * ageYears)
    }
}
```

---

### 3️⃣ Step 3 – Convert Body Density to Body Fat %

Using the **Siri Equation**:

```text
bodyFatPercent = (495 / bodyDensity) - 450
```

#### Kotlin

```kotlin
val bodyFatPercent = (495.0 / bodyDensity) - 450.0
```

---

### 📌 Full Calculation Example (Combined)

```kotlin
val sum3 = site1Mm + site2Mm + site3Mm

val bodyDensity = when (sex) {
    Sex.MALE -> {
        1.10938 -
        (0.0008267 * sum3) +
        (0.0000016 * sum3 * sum3) -
        (0.0002574 * ageYears)
    }
    Sex.FEMALE -> {
        1.0994921 -
        (0.0009929 * sum3) +
        (0.0000023 * sum3 * sum3) -
        (0.0001392 * ageYears)
    }
}

val bodyFatPercent = (495.0 / bodyDensity) - 450.0
```

---

### ✅ Validation Rules

Before calculation:

```text
ageYears > 0
each skinfold > 0
sum3 > 0
```

Recommended realistic ranges:

```text
skinfold: 2–50 mm per site
sum3: 6–150 mm
```

---

### 📌 Units & Precision

* Skinfold values must be in **millimeters (mm)**
* Age in **years**
* Output body fat in **percentage**
* Recommended rounding:

  * Body Density → 4 decimal places
  * Body Fat % → 1 decimal place

---

### ⚠️ Important Notes

* Equations are population-based regression models.
* Accuracy depends heavily on correct caliper technique.
* Consistency of measurement site placement is critical.
* Not recommended for extreme obesity or elite bodybuilding without calibration.

---

# ✅ Recommended Validation Rules

Before calculation:

```
heightCm > 0
weightKg > 0
hipCm > 0
waistCm > 0
neckCm > 0
(waistCm - neckCm) > 0  // male navy formula
(waistCm + hipCm - neckCm) > 0  // female navy formula
```

---

# 📌 Optional: Rounding Strategy

For UI display consistency:

```
BMI → 1 decimal place
Body Fat % → 1 decimal place
Ratios → 2 decimal places
```

Example:

```kotlin
fun Double.round(decimals: Int): Double {
    val factor = 10.0.pow(decimals)
    return (this * factor).roundToInt() / factor
}
```

---

These formulas are mathematically complete and directly implementable within the MVVM domain layer.
