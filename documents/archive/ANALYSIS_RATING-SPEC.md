# Specification: Improved Derived Metrics Rater

## 1. Goals

Give the user hints for interpreting the derived metrics with:

1. High **medical accuracy**
2. Providing **better risk stratification**
3. Accounting for **dangerously low body fat**
4. Supporting **BMI obesity classes**
5. Maintaining **simple thresholds for performance**

---

# 2. Severity Levels

Existing:

```
Good
Fair
Poor
```

Add:

```
Severe
```

Meaning:

| Severity | Meaning             |
| -------- | ------------------- |
| Good     | healthy range       |
| Fair     | mild risk           |
| Poor     | significant risk    |
| Severe   | strong medical risk |

Rules:

* **Only used when strong evidence of elevated morbidity risk exists**
* Should be **rare but meaningful**

---

# 3. BMI Rating

## Method

```
rateBmi(bmi: Double)
```

## Classification

| BMI       | Label              | Severity |
| --------- | ------------------ | -------- |
| < 16.0    | Severe underweight | Severe   |
| 16.0–18.4 | Underweight        | Poor     |
| 18.5–24.9 | Normal             | Good     |
| 25.0–29.9 | Overweight         | Fair     |
| 30.0–34.9 | Obese Class I      | Poor     |
| 35.0–39.9 | Obese Class II     | Poor     |
| ≥ 40      | Obese Class III    | Severe   |

Reasoning:

* WHO defines obesity classes with increasing risk.
* Class III obesity strongly correlates with mortality and metabolic disease.

Sources:

* World Health Organization BMI classification
* Centers for Disease Control and Prevention obesity definitions

---

# 4. Body Fat Percentage Rating

## Method

```
rateBodyFatPercent(percent: Double, sex: Sex)
```

### Add extremely low body fat category.

Body fat below essential ranges is associated with:

* hormonal disruption
* immune suppression
* fertility issues

Research in exercise physiology literature.

---

## Male Classification

| Body Fat % | Label           | Severity |
| ---------- | --------------- | -------- |
| < 3        | Dangerously low | Severe   |
| 3–5.9      | Essential fat   | Poor     |
| 6–13.9     | Athletes        | Good     |
| 14–17.9    | Fitness         | Good     |
| 18–24.9    | Acceptable      | Fair     |
| ≥ 25       | Obese           | Poor     |

---

## Female Classification

| Body Fat % | Label           | Severity |
| ---------- | --------------- | -------- |
| < 10       | Dangerously low | Severe   |
| 10–13.9    | Essential fat   | Poor     |
| 14–20.9    | Athletes        | Good     |
| 21–24.9    | Fitness         | Good     |
| 25–31.9    | Acceptable      | Fair     |
| ≥ 32       | Obese           | Poor     |

Sources:

* American Council on Exercise classification
* McArdle, Katch & Katch exercise physiology research

---

# 5. Waist-Hip Ratio Rating

## Method

```
rateWaistHipRatio(ratio: Double, sex: Sex)
```

Based on cardiovascular risk research from the World Health Organization and findings from the INTERHEART Study.

---

## Male

| WHR       | Label          | Severity |
| --------- | -------------- | -------- |
| < 0.90    | Low risk       | Good     |
| 0.90–0.99 | Moderate risk  | Fair     |
| 1.00–1.09 | High risk      | Poor     |
| ≥ 1.10    | Very high risk | Severe   |

---

## Female

| WHR       | Label          | Severity |
| --------- | -------------- | -------- |
| < 0.80    | Low risk       | Good     |
| 0.80–0.85 | Moderate risk  | Fair     |
| 0.86–0.94 | High risk      | Poor     |
| ≥ 0.95    | Very high risk | Severe   |

Reasoning:

High WHR strongly correlates with:

* cardiovascular disease
* metabolic syndrome
* mortality

---

# 6. Waist-Height Ratio Rating

## Method

```
rateWaistHeightRatio(ratio: Double)
```

Derived from public health research by Margaret Ashwell.

Core rule:

> Waist circumference should be less than half of height.

---

## Classification

| WHtR      | Label            | Severity |
| --------- | ---------------- | -------- |
| < 0.40    | Underweight risk | Fair     |
| 0.40–0.49 | Healthy          | Good     |
| 0.50–0.59 | Increased risk   | Fair     |
| 0.60–0.69 | High risk        | Poor     |
| ≥ 0.70    | Very high risk   | Severe   |

Reasoning:

Higher ratios strongly predict:

* diabetes
* cardiovascular disease
* mortality

Often outperform BMI in studies.

---

# 7. Validation Rules

The rater must guard against invalid values.

Reject or return null if:

```
BMI < 10 or > 80
BodyFat < 1 or > 70
WHR < 0.4 or > 2
WHtR < 0.2 or > 1
```

These ranges represent realistic biological limits.

---

# 8. Output Consistency Rules

Each rating must include:

```
MetricRating(
    label: String,
    severity: RatingSeverity
)
```

---

# 10. Backwards Compatibility

The class must:

* Accept **DerivedMetrics**
* Return **DerivedMetricRatings**

---

# 11. Optional Future Improvements (Not required now)

Potential additional metrics if added later:

| Metric               | Why                        |
| -------------------- | -------------------------- |
| Waist circumference  | used clinically            |
| Body Adiposity Index | alternative fat estimation |
| Relative Fat Mass    | modern body fat estimator  |

