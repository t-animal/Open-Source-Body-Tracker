# Onboarding

When the app is launched for the very first time, an onboarding flow is shown.  
Its purpose is to allow the user to either explore the app using demo data or create a real profile.

The onboarding is only shown once. After completion, the user is taken to the main application.

---

## Onboarding Flow

### Step 1 – Choose Start Mode

The first screen asks the user how they want to start using the app.

**Options:**

- **Try with Demo Data**
- **Create Profile**

This allows new users to explore the app immediately without entering any data.

---

# Demo Mode

If the user selects **"Try with Demo Data"**, the app automatically creates:

**Profile:**
- Gender: Male
- Height: 180 cm
- Date of Birth: 01.01.1991

Additionally, a set of **fake measurement data** is generated just like on the [FakeDataGeneratorScreen](../app/src/main/java/de/t_animal/opensourcebodytracker/feature/debug/FakeDataGeneratorScreen.kt) (but the default values are used right away, no need to configure it)

After generating the demo data, the app starts normally and opens the **Measurements screen**.

### Demo Mode Banner

While the app is in demo mode, a banner is displayed at the top of the screen:

```

You are currently using demo data.
Reset the app to create your own profile.
[Reset App]

```

Pressing **Reset App** will:

1. Delete all application data
2. Restart the app
3. Show the onboarding screen again

This allows the user to switch from demo mode to a real profile at any time.

---

# Create Profile Flow

If the user selects **"Create Profile"**, a two-step setup process starts.

## Step 1 – Profile Creation

The user is taken to the **Profile Creation Screen**.

The user must provide basic information required for body composition analysis.

Fields include:

- Gender
- Height
- Date of Birth

After completing the profile, the user proceeds to the next step.

---

## Step 2 – Select Analysis Methods

In the second step, the user selects which **analysis methods** they want to use.

This screen corresponds conceptually to the **analysis configuration in the Settings screen**.

Example analysis methods include:

- Navy Body Fat %
- Skinfold Body Fat %

When an analysis method is enabled, all **required measurements** for that analysis are automatically activated by default.

For example:

- Enabling **Navy Body Fat** automatically enables:
  - Waist circumference
  - Neck circumference

A small hint is displayed on the screen:

> Additional measurements can be enabled later in the Settings screen.

This allows the onboarding process to stay simple while still making the configuration flexible later.

---

## Completion

After the analysis methods are selected:

1. The onboarding process is completed
2. The app opens normally
3. The user is taken to the **Measurements screen**

No demo banner is shown in this mode.

---

## Onboarding Conditions

The onboarding screen is shown only if it has not been set up yet (no user data), i.e.:

- The app is started for the first time **OR**
- The user resets the app via the **Reset App** button in demo mode

Otherwise, the app launches directly into the main interface.
```
