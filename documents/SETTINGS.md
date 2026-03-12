# Unified Settings Architecture

The current app contains multiple settings screens that are accessed separately through the overflow menu.  
To improve usability and consistency, all configuration options will be **consolidated into a unified Settings structure**.

The goal is to provide:

- A **single entry point for all configuration**
- A **clear hierarchy of settings**
- Consistent UI patterns across all settings screens

The **debug options in the overflow menu remain unchanged**.

---

# Overview

Instead of accessing configuration screens directly from the overflow menu, there will be **one main Settings screen** with submenus.

Existing settings that will be integrated:

- Profile
- Analysis settings
- Measurement settings
- Measurement visibility settings
- Export settings

Additionally, a new screen will be added:

- About

---

# Settings Entry Point

The overflow menu will now contain:

```
⋮
Settings
About
Debug options (unchanged)
```

Selecting **Settings** opens the **Main Settings Screen**.

---

# Main Settings Screen

The main screen acts as a **navigation hub** for all configuration pages.

Example layout:

```
Settings

Profile
>

Analysis Methods
>

Measurements
>

Measurement Visibility
>

Export
>

About
>
```

Each item opens a dedicated settings screen.

---

# General Settings Screen Behavior

All settings screens follow the same UI structure.

### Top Navigation

Each screen contains a **top navigation bar** with:

- Page title
- Back arrow

```
←  Screen Title
```

Pressing the arrow returns to the previous screen.

### Onboarding Exception

Some screens are reused during onboarding.

In those cases:

- **The back button is not shown**
- Navigation is handled by the onboarding flow

---

# Profile Screen

The profile screen contains the user's basic personal data.

Fields include:

- Gender
- Height
- Date of Birth

### Gender Selection

Gender options are displayed **horizontally**.

Example:

```
Sex

[ Male ]   [ Female ] 
```

This improves clarity and reduces vertical space usage.

---

# Analysis Settings Screen

This screen defines **which body composition analyses are enabled**.

Example analyses:

- Navy Body Fat %
- Skinfold Body Fat %

Enabling an analysis automatically activates the **required measurements**.

### Description Text

At the top of the screen a short explanation is shown:

> Select which body composition analysis methods should be calculated.  
> Enabling an analysis may automatically activate required measurements.

---

# Measurement Settings Screen

This screen controls **which measurements are recorded**.

Measurements required for enabled analyses **cannot be disabled**.

Optional measurements can be toggled on or off.

### Description Text

At the top of the screen:

> Configure which measurements should be collected when recording a measurement entry.  
> Some measurements are required for selected analysis methods and cannot be disabled.

---

# Measurement Visibility Screen

Previously, measurement visibility was part of another settings screen.  
It is now **moved to its own dedicated screen**.

This screen controls **which measurements are visible in tables and analysis views**.

---

## Visible Measurements

Measurements currently being recorded are listed with toggles.

Example:

```
Visible Measurements

[✓] Weight
[✓] Waist
[✓] Hip
[ ] Chest
```

---

## Measurements Not Currently Recorded

Measurements that are **not currently collected** are displayed in a separate section.

Example:

```
Measurements Not Currently Collected

Neck
Chest
Thigh
```

A hint text is displayed above this section:

> These measurements are currently not recorded.  
> They may still appear in the app if historical data exists.

This allows users to **hide measurements they do not actively track** while still preserving historical information.

---

# Export Settings Screen

The export screen manages data backup and transfer.

### Description Text

At the top:

> Configure how your data can be exported for backup or device transfer.

---

## Password Placement

The **export password field is placed at the top of the screen**.

```
Export Password
[ ******** ]
```

The export feature can only be enabled if a password is provided.

If the password field is empty:

- Export options remain disabled
- Export cannot be activated

---

## Export Options

Below the password field:

```
Export Destination

[✓] Export to Device Storage
[ ] Export to Google Drive (coming later)
```

---

## Folder Selection

```
Export Folder
[ Select Folder ]
```

The folder selection is enabled only when export is activated.

---

# About Screen

A new **About screen** provides information about the project.

It contains:

- App description
- Project information
- Links for support and contribution

Example layout:

```
About

Body Measurement Tracker

A privacy-focused body measurement tracking app.

Project
[ GitHub Repository ]

Contact
support@example.com
```

---

## GitHub Link

The GitHub link:

- Opens the project repository in the browser
- Allows users to view the source code or report issues

---

## Contact Email

The contact email is clickable.

When pressed:

- The default **mail client opens**
- A new email draft is created

---

# Settings Screen UX Principle

Every settings screen follows these principles:

- Clear title
- Short explanation text at the top
- Simple toggle-based configuration
- Consistent back navigation

---

# Summary

This phase introduces a **unified settings architecture**.

Key improvements:

✔ Single Settings entry point  
✔ Structured settings navigation  
✔ Dedicated Measurement Visibility screen  
✔ Improved Export configuration logic  
✔ Horizontal gender selection  
✔ Informational text on all settings pages  
✔ New About screen with GitHub and contact email  
✔ Consistent back navigation across settings screens  

The debug options in the overflow menu **remain unchanged**.
