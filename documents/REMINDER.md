# Phase 7 – Measurement Reminders (Notifications)

This phase introduces **reminders** that help users remember to regularly record measurements.
The feature is implemented in multiple stages to gradually introduce configuration and system integration.

Overview of Subphases:

* **Phase 7.1 – Reminder Configuration UI**
* **Phase 7.2 – Manual Test Notification**
* **Phase 7.3 – Scheduled OS Notifications**

# Phase 7.1 – Reminder Configuration UI

## Goal

Provide a settings screen where users can configure reminders for measurement tracking.

This phase introduces the **UI and configuration logic only**.
No notifications are triggered yet.

## Entry Point

A new menu entry is added to the **overflow menu (three-dot menu)** on the Measurements screen.

```
⋮
Profile
Settings
Reminders
```

Selecting **Reminders** opens the **Reminder Settings Screen**.

## Reminder Settings Screen

The screen allows the user to:

* Enable or disable reminders
* Select the weekdays when reminders should occur
* Select the reminder time
* Save or discard changes

### Layout Structure

```
Reminder Settings

[ Toggle ] Enable Reminders

Weekdays
[ M ] [ T ] [ W ] [ T ] [ F ] [ S ] [ S ]

Time
[ 20:00 ]

[ Save ]      [ Back ]
```

## Enable / Disable Toggle

At the top of the screen:

```
Enable Reminders  [ ON / OFF ]
```

### Behavior

If **disabled**:

* Weekday selection becomes disabled
* Time picker becomes disabled
* No reminders will be scheduled

If **enabled**:

* Weekdays and time become configurable

## Weekday Selection

Weekdays are displayed using **Material Design Filter Chips**.

Each chip contains the **first letter of the weekday**.

```
[M] [T] [W] [T] [F] [S] [S]
```

Where:

* M → Monday
* T → Tuesday
* W → Wednesday
* T → Thursday
* F → Friday
* S → Saturday
* S → Sunday

### Behavior

* Chips can be toggled individually
* Multiple days can be selected
* At least one day should be selected when reminders are enabled

## Time Selection

Users can choose the time of day when the reminder should be triggered.

A standard **Android Time Picker** is used.

Example:

```
Reminder Time
[ 20:00 ]
```

The selected time will later be used to schedule notifications.

## Save / Back Behavior

### Save Button

Pressing **Save** will:

1. Store reminder configuration
2. Return to the previous screen

### Back Button

Pressing **Back**:

* Leaves the screen immediately
* **Does not save changes**

No confirmation dialog is required.

## Phase 7.2 – Manual Test Notification

### Goal

Introduce a simple notification to verify that the notification system works correctly.

The notification is triggered **manually**, not by schedule.

## Entry Point

A new entry is added to the overflow menu:

```
⋮
Profile
Settings
Reminders
Trigger Reminder
```

Selecting **Trigger Reminder** immediately shows a notification.

On Android 13+:

* If notification permission is missing, no permission dialog is opened in this phase.
* The app shows a message that notifications are disabled.

## Notification Content

Title:

```
Measurement Reminder
```

Body:

```
Don't forget to record your measurements.
```

## Notification Behavior

When the user taps the notification:

* The app opens
* The **Add Measurement screen** is shown

This allows the user to immediately record a new measurement.

# Phase 7.3 – Scheduled System Notifications

## Goal

Connect the reminder configuration to the **operating system's scheduling mechanism**.

Notifications will be triggered automatically according to the configured settings.

## Scheduling Logic

Notifications should be scheduled based on:

* Enabled reminder toggle
* Selected weekdays
* Selected time

Example configuration:

```
Enabled: YES
Days: Monday, Wednesday, Friday
Time: 20:00
```

The OS should schedule notifications for:

* Monday 20:00
* Wednesday 20:00
* Friday 20:00

## Notification Behavior

When triggered:

* A system notification appears
* The content matches the manual notification from Phase 7.2

```
Title: Measurement Reminder
Text: Don't forget to record your measurements.
```

When the user taps the notification:

* The app opens
* The **Add Measurement screen** is displayed

## Disabling Reminders

If the user disables reminders:

```
Enable Reminders → OFF
```

Then:

* All scheduled notifications must be **cancelled**
* The app must **not trigger further reminders**

## Data Model Example

```kotlin
data class ReminderSettings(
    val enabled: Boolean,
    val weekdays: Set<DayOfWeek>,
    val time: LocalTime
)
```

## UX Goals

* Encourage consistent measurement tracking
* Keep reminder configuration simple
* Allow flexible schedules
* Avoid intrusive behavior
* Integrate with Android system notification mechanisms

# Summary

Phase 7 introduces reminders in three steps:

Phase 7.1
✔ Reminder configuration UI
✔ Enable toggle
✔ Weekday selection using chips
✔ Time picker
✔ Save / discard behavior

Phase 7.2
✔ Manual test notification
✔ Overflow menu trigger
✔ Deep link to Add Measurement screen

Phase 7.3
✔ Scheduled notifications
✔ OS-level scheduling
✔ Automatic cancellation when disabled
