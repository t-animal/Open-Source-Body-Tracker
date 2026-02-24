# Inverted Body Composition Formulas

This document describes the mathematical inversion of the body composition formulas defined in `FORMULAS.md`.
The goal is to calculate required body measurements (circumferences) that correspond to a specific body composition scenario.

## Core Metrics

Given:
* `leanBodyWeightKg` (Fixed)
* `fatBodyWeightKg` (Fixed for a specific calculation scenario)
* `heightCm` (Fixed)
* `sex` (MALE / FEMALE)

We derive:
* `totalWeightKg` = `leanBodyWeightKg` + `fatBodyWeightKg`
* `targetBodyFatPercentage` = (`fatBodyWeightKg` / `totalWeightKg`) * 100

Since all weight and height inputs are substantial, the **Target Body Fat Percentage** is a constant value for the purpose of these inversions.

---

## 1️⃣ Inverted Navy Method (Calculating Viable Measurements)

We invert the U.S. Navy Body Fat equations to find the set of body measurements that result in the calculated `targetBodyFatPercentage`.

### For Males

**Original Formula:**
$$ BFP = 86.010 \times \log_{10}(waist - neck) - 70.041 \times \log_{10}(height) + 36.76 $$

**Inverted Formula:**
Since BFP and Height are fixed, the term $(waist - neck)$ must equal a constant value $\Delta_{male}$.

1.  Calculate required difference $\Delta_{male}$:
    $$ \Delta_{male} = 10^{\left( \frac{BFP + 70.041 \times \log_{10}(height) - 36.76}{86.010} \right)} $$

2.  **Viable Solutions (Waist & Neck pairs):**
    Any pair of $(neck, waist)$ that satisfies $waist = neck + \Delta_{male}$ is a valid mathematical solution.
    
    To generate meaningful data, we can iterate through a physiological range of **Neck** sizes to find corresponding **Waist** sizes.

    *   **Iterate `neckCm`:** [30.0 .. 50.0] (Step 0.5cm)
    *   **Calculate `waistCm`:** `waistCm` = `neckCm` + $\Delta_{male}$
    *   **Validate:** Check if `waistCm` falls within realistic human bounds.

### For Females

**Original Formula:**
$$ BFP = 163.205 \times \log_{10}(waist + hip - neck) - 97.684 \times \log_{10}(height) - 78.387 $$

**Inverted Formula:**
Since BFP and Height are fixed, the term $(waist + hip - neck)$ must equal a constant value $S_{female}$.

1.  Calculate required sum/diff metric $S_{female}$:
    $$ S_{female} = 10^{\left( \frac{BFP + 97.684 \times \log_{10}(height) + 78.387}{163.205} \right)} $$

2.  **Viable Solutions (Waist, Hip, Neck combinations):**
    There are infinite combinations of $(neck, waist, hip)$ satisfying this equation.
    
    To narrow this down, we typically fix **Neck** (as it varies least with weight change) and potentially **Hip** (as it varies less than waist), or we generate a matrix of options.

    *   **Approach:** Iterate `neckCm` and `hipCm` within likely ranges to find corresponding `waistCm`.
    *   **Constraint:** $waist = S_{female} - hip + neck$

---

## 2️⃣ Inverted Jackson & Pollock 3-Site Skinfold Method

We can also calculate the required skinfold measurements to match the `targetBodyFatPercentage`.

The process involves two steps:
1.  Convert Target Body Fat % to Target Body Density.
2.  Solve the quadratic equation to find the required Sum of Skinfolds ($Sum_{3}$).
3.  Distribute this sum across the 3 measurement sites.

### Step 1: Target Body Density

Using the inverted Siri Equation:
$$ Density_{target} = \frac{495}{TargetBFP + 450} $$

### Step 2: Calculate Required Sum of Skinfolds ($Sum_{3}$)

The the relationship between Body Density and Sum of Skinfolds is quadratic:
$$ Density = \text{Intercept} - (C_1 \times Sum_{3}) + (C_2 \times Sum_{3}^2) - (C_3 \times Age) $$

Rearranging into standard quadratic form $a x^2 + b x + c = 0$ (where $x = Sum_{3}$):
$$ (C_2) \cdot Sum_{3}^2 - (C_1) \cdot Sum_{3} + (\text{Intercept} - (C_3 \times Age) - Density_{target}) = 0 $$

We solve for $Sum_{3}$ using the quadratic formula:
$$ Sum_{3} = \frac{-b \pm \sqrt{b^2 - 4ac}}{2a} $$

**For Males:**
*   $a = 0.0000016$
*   $b = -0.0008267$
*   $c_{base} = 1.10938$
*   $ageFactor = 0.0002574$
*   $c = 1.10938 - (0.0002574 \times Age) - Density_{target}$

**For Females:**
*   $a = 0.0000023$
*   $b = -0.0009929$
*   $c_{base} = 1.0994921$
*   $ageFactor = 0.0001392$
*   $c = 1.0994921 - (0.0001392 \times Age) - Density_{target}$

*Note: Since the coefficient $b$ is negative and the quadratic opens upwards ($a > 0$), we typically look for the smaller positive root (the minus option in $\pm$) as the skinfold sum should be within physiological bounds (roughly 10mm - 20mm for chest, 15-60mm for abdomen, 10-30 for Thigh).*

### Step 3: Viable Skinfold Combinations

Once we have the total required sum ($Sum_{3}$), we can distribute it across the three specific sites for the user's sex.

**Sites:**
*   **Male:** Chest, Abdomen, Thigh
*   **Female:** Triceps, Suprailiac, Thigh

**Distribution Logic:**
There is no "correct" single distribution. However, implies a uniform fat loss/gain.
We can generate a set of viable combinations:
1.  **Equal Distribution:** $Site_1 = Site_2 = Site_3 = Sum_{3} / 3$
2.  **Ranged Iteration:** Similar to the Navy method, we can iterate two sites within typical bounds and calculate the third.
    *   $Site_3 = Sum_{3} - Site_1 - Site_2$
    *   Constraint: $Site_3 \ge 2\text{mm}$ (Minimum measurable skinfold)

### Sensible Ranges (Validation)
When generating viable solutions, ensure:
*   $Sum_{3}$ is positive and real (if $b^2 - 4ac < 0$, the target BFP is mathematically impossible for that Age/Formula).
*   Individual skinfolds > 2mm (caliper limit).
*   Individual skinfolds < 50-70mm (typical max for standard calipers).


## 3️⃣ General Validation & Physiological Constraints

When solving for missing variables, results must be validated against physiological norms to ensure solutions are realistic.

**Navy Method Constraints:**
*   `waistCm` > `neckCm` (Physically waist usually > neck)
*   **Mens:** Waist typically 60cm - 150cm
*   **Womens:** Waist typically 50cm - 150cm
*   **WHR (Waist-Hip Ratio):**
    *   Male: 0.8 - 1.0 (typical)
    *   Female: 0.65 - 0.85 (typical)

**Skinfold Method Constraints:**
*   **Individual Sites:** > 2mm and < 50mm (typical caliper limits)
*   **Sum of 3 Skinfolds:** > 10mm and < 150mm

If calculations result in values outside these ranges, it implies the `targetBodyFatPercentage` might be unattainable with the fixed `leanBodyWeight` and `height` without significant changes to muscle mass or bone structure.

