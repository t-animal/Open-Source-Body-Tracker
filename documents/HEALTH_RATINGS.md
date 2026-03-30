# Health Ratings

Each derived metric is rated into a severity level. Ratings are sex-specific where applicable. Values outside the plausible range return no rating (null).

Severity levels: **Good**, **Fair**, **Poor**, **Severe**.

---

## BMI

Plausible range: 10–80

| Range | Label | Severity |
|-------|-------|----------|
| < 16.0 | Severe Underweight | Severe |
| 16.0–18.4 | Underweight | Poor |
| 18.5–24.9 | Normal | Good |
| 25.0–29.9 | Overweight | Fair |
| 30.0–34.9 | Obese Class I | Poor |
| 35.0–39.9 | Obese Class II | Poor |
| >= 40.0 | Obese Class III | Severe |

---

## Body Fat % (Male)

Plausible range: 1–70%. Applied to both Navy and Skinfold results.

| Range | Label | Severity |
|-------|-------|----------|
| < 3.0 | Dangerously Low | Severe |
| 3.0–5.9 | Essential Fat | Poor |
| 6.0–13.9 | Athletic | Good |
| 14.0–17.9 | Fit | Good |
| 18.0–24.9 | Acceptable | Fair |
| >= 25.0 | Obese | Poor |

## Body Fat % (Female)

| Range | Label | Severity |
|-------|-------|----------|
| < 10.0 | Dangerously Low | Severe |
| 10.0–13.9 | Essential Fat | Poor |
| 14.0–20.9 | Athletic | Good |
| 21.0–24.9 | Fit | Good |
| 25.0–31.9 | Acceptable | Fair |
| >= 32.0 | Obese | Poor |

---

## Waist-Hip Ratio (Male)

Plausible range: 0.4–2.0

| Range | Label | Severity |
|-------|-------|----------|
| < 0.90 | Low Risk | Good |
| 0.90–0.99 | Moderate Risk | Fair |
| 1.00–1.09 | High Risk | Poor |
| >= 1.10 | Very High Risk | Severe |

## Waist-Hip Ratio (Female)

| Range | Label | Severity |
|-------|-------|----------|
| < 0.80 | Low Risk | Good |
| 0.80–0.85 | Moderate Risk | Fair |
| 0.86–0.94 | High Risk | Poor |
| >= 0.95 | Very High Risk | Severe |

---

## Waist-Height Ratio (both sexes)

Plausible range: 0.2–1.0

| Range | Label | Severity |
|-------|-------|----------|
| < 0.40 | Underweight Risk | Fair |
| 0.40–0.49 | Healthy | Good |
| 0.50–0.59 | Increased Risk | Fair |
| 0.60–0.69 | High Risk | Poor |
| >= 0.70 | Very High Risk | Severe |

---

## Source File

`domain/metrics/DerivedMetricsRater.kt`
