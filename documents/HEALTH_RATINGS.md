# Health Ratings

Each derived metric is rated into a severity level. Ratings are sex-specific where applicable. Values outside the plausible range return no rating (null).

Severity levels: **Good**, **Fair**, **Poor**, **Severe** with the following meanings:
  - Good → low risk
  - Fair → mild deviation or emerging risk
  - Poor → clear elevated risk
  - Severe → high or urgent risk

---

## BMI

Plausible range: 10–80

| Range | Label | Severity | What this means |
|-------|-------|----------|------------------|
| < 16.0 | Severe Underweight | Severe | This rating is associated with very low body weight and increased risk of weakness, low energy, and impaired immune function. BMI does not account for muscle mass or fat distribution, though. |
| 16.0–18.4 | Underweight | Poor | This rating is associated with low body weight and may increase the risk of fatigue, nutrient deficiencies, and reduced physical resilience. BMI does not account for muscle mass or fat distribution, though. |
| 18.5–24.9 | Normal | Good | This rating is associated with a healthy weight range and lower risk of weight-related health problems. BMI does not account for muscle mass or fat distribution, though. |
| 25.0–29.9 | Overweight | Fair | This rating is associated with increased body weight and may increase health risks over time, especially for heart and metabolic health. BMI does not account for muscle mass or fat distribution, though. |
| 30.0–34.9 | Obese Class I | Poor | This rating is associated with higher body weight and increased risk of conditions such as heart disease and type 2 diabetes. BMI does not account for muscle mass or fat distribution, though. |
| 35.0–39.9 | Obese Class II | Poor | This rating is associated with significantly increased body weight and a higher likelihood of serious health conditions. BMI does not account for muscle mass or fat distribution, though. |
| >= 40.0 | Obese Class III | Severe | This rating is associated with very high body weight and a substantially increased risk of severe health complications. BMI does not account for muscle mass or fat distribution, though. |

---

## Body Fat % (Male)

Plausible range: 1–70%. Applied to both Navy and Skinfold results.

| Range | Label | Severity | What this means |
|-------|-------|----------|------------------|
| < 3.0 | Dangerously Low | Severe | This rating is associated with extremely low body fat and increased risk of hormonal imbalance and impaired body function. However, body fat percentage alone does not take fat distribution into account. |
| 3.0–5.9 | Essential Fat | Poor | This rating is associated with very low body fat and may reduce energy availability and physical resilience. However, body fat percentage alone does not take fat distribution into account. |
| 6.0–13.9 | Athletic | Good | This rating is associated with low body fat and is commonly seen in physically trained individuals. |
| 14.0–17.9 | Fit | Good | This rating is associated with a healthy level of body fat and good overall physical condition. |
| 18.0–24.9 | Acceptable | Fair | This rating is associated with a typical body fat range and mild to moderate health risk. |
| >= 25.0 | High Body Fat | Poor | This rating is associated with high body fat and increased risk of metabolic and cardiovascular conditions. However, body fat percentage alone does not take fat distribution into account. |

## Body Fat % (Female)

| Range | Label | Severity | What this means |
|-------|-------|----------|------------------|
| < 10.0 | Dangerously Low | Severe | This rating is associated with extremely low body fat and increased risk of hormonal disruption and menstrual irregularities. However, body fat percentage alone does not take fat distribution into account. |
| 10.0–13.9 | Essential Fat | Poor | This rating is associated with very low body fat and may impact hormone balance and energy levels. However, body fat percentage alone does not take fat distribution into account. |
| 14.0–20.9 | Athletic | Good | This rating is associated with low body fat and strong physical fitness. |
| 21.0–24.9 | Fit | Good | This rating is associated with a healthy and sustainable body fat level. |
| 25.0–31.9 | Acceptable | Fair | This rating is associated with a typical body fat range and mild to moderate health risk. |
| >= 32.0 | High Body Fat | Poor | This rating is associated with high body fat and increased risk of chronic health conditions. However, body fat percentage alone does not take fat distribution into account. |

---

## Waist-Hip Ratio (Male)

Plausible range: 0.4–2.0

| Range | Label | Severity | What this means |
|-------|-------|----------|------------------|
| < 0.90 | Low Risk | Good | This rating is associated with lower abdominal fat and reduced risk of cardiovascular disease. |
| 0.90–0.99 | Moderate Risk | Fair | This rating is associated with moderate abdominal fat and mild increase in health risk. |
| 1.00–1.09 | High Risk | Poor | This rating is associated with higher abdominal fat and increased risk of heart and metabolic conditions. |
| >= 1.10 | Very High Risk | Severe | This rating is associated with high abdominal fat and a significantly increased risk of serious health conditions. |

## Waist-Hip Ratio (Female)

| Range | Label | Severity | What this means |
|-------|-------|----------|------------------|
| < 0.80 | Low Risk | Good | This rating is associated with lower abdominal fat and reduced health risk. |
| 0.80–0.85 | Moderate Risk | Fair | This rating is associated with moderate abdominal fat and mild increase in health risk. |
| 0.86–0.94 | High Risk | Poor | This rating is associated with increased abdominal fat and higher risk of metabolic and cardiovascular conditions. |
| >= 0.95 | Very High Risk | Severe | This rating is associated with high abdominal fat and a significantly increased risk of serious health issues. |

---

## Waist-Height Ratio (both sexes)

Plausible range: 0.2–1.0

| Range | Label | Severity | What this means |
|-------|-------|----------|------------------|
| < 0.40 | Underweight Risk | Fair | This rating is associated with a low waist size relative to height and might indicate low body fat or underweight status. However, health risk cannot be determined from this measure alone. |
| 0.40–0.49 | Healthy | Good | This rating is associated with a healthy waist size and lower risk of metabolic disease. |
| 0.50–0.59 | Increased Risk | Fair | This rating is associated with increased abdominal fat and mild to moderate health risk. |
| 0.60–0.69 | High Risk | Poor | This rating is associated with high abdominal fat and increased likelihood of health problems. |
| >= 0.70 | Very High Risk | Severe | This rating is associated with very high abdominal fat and a strong link to serious health risks. |

---

## Source File

`domain/metrics/DerivedMetricsRater.kt`