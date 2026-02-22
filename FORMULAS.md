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
