# Phase 5 – Photo Capture & Progress Comparison

This document defines the final major feature set of the application: **photo capture, storage, browsing, comparison, and animation**.

Due to its size and complexity, this phase is divided into multiple subphases.

---

# 🧭 Overview of Subphases

* **Phase 5.1** – Photo Capture & Secure Storage
* **Phase 5.2** – Optimized Photo Gallery
* **Phase 5.3** – Photo Comparison Mode (2-Image Slider)
* **Phase 5.4** – Compare Mode & Animation Mode (Advanced Interaction)

---

# Phase 5.1 – Photo Capture & Secure Storage

## 🎯 Goal

Allow users to attach a photo to a measurement entry during creation or editing.

---

## 📍 Add/Edit Measurement Screen Changes

### New FAB (Camera)

* Positioned above the **Save** button
* Displays a **camera icon**

### Behavior

When pressed:

1. Launch device camera
2. Capture photo
3. Display photo **below the input fields**
4. Photo is **not persisted immediately**
5. Persist only when user presses **Save**

---

## 🖼 Photo Preview

After capture:

* Displayed below input fields
* Full-width preview
* Clickable → opens large preview dialog
* Dialog can be dismissed

---

## 🗑 Delete Photo

* Small delete icon button above the photo
* Removes photo from current edit state
* Deletion is only persisted when pressing **Save**

---

## 🔐 Storage Requirements

Photos must be stored:

* In **internal app storage**
* Not accessible by other apps
* Not visible in system gallery
* Private file directory (e.g. internal files dir)


---

## 📷 Photos Screen (Phase 5.1 Version)

In this subphase, the Photos tab:

* Displays a list of entries that have a photo
* Shows only:

  * Date
* Sorted newest → oldest

### Behavior

* Tap entry → navigate to Edit screen
* Photo visible there

---

## ASCII Mockup – Initial Photos Screen

```text
+--------------------------------------------------+
| Photos                                          |
+--------------------------------------------------+

2026-02-20
2026-02-15
2026-02-01
```

---

# Phase 5.2 – Optimized Photo Gallery

## 🎯 Goal

Replace date-only list with performant photo gallery.

---

## 📜 Scrollable Photo Feed

* Vertical scroll
* Newest → oldest
* Each item displays:

  * Thumbnail preview
  * Date below image

---

## ⚡ Performance Requirements

* Do NOT load all full-resolution images at once
* Use:

  * Lazy loading
  * Thumbnail generation
  * Efficient memory handling
* Load images on-demand when entering viewport

---

## 🖥 ASCII Mockup – Gallery

```text
+--------------------------------------------------+
| Photos                                          |
+--------------------------------------------------+

[  📷 Thumbnail  ]
2026-02-20

[  📷 Thumbnail  ]
2026-02-15

[  📷 Thumbnail  ]
2026-02-01
```

---

# Phase 5.3 – Photo Comparison Mode (2-Image Selection)

## 🎯 Goal

Allow comparison of exactly two selected photos.

# 🔘 FAB Modes

A FAB button added on Photos screen:

1. **Compare Mode FAB**


---

## 🖱 Selection Behavior

* User presses **Compare Mode FAB** to activate comparison mode
* User taps photos to select
* Maximum: **2 selections**
* If attempting third selection:

  * Show error message
  * Do not allow selection
  * Photos can be unselected by tapping them again

Both fab buttons disappear when selection mode is entered. An exit-mode fab button appears instead

---

## 📌 Selected Thumbnails Area

When photos selected:

* Display small thumbnails at bottom
* Show a **Compare** button

---

## 🖥 ASCII Mockup – Selection Mode

```text
+--------------------------------------------------+
| Photos                                          |
+--------------------------------------------------+

[  📷  ]  ✓
[  📷  ]
[  📷  ]  ✓
[  📷  ]

----------------------------------------------------
[ thumb1 ]  [ thumb2 ]          [ Compare ]
```

---

## 🔁 Compare Screen

When pressing **Compare**:

Open new screen showing:

* Both images overlaid
* Vertical slider in middle
* Dragging slider:

  * Slide right → show left image
  * Slide left → show right image

---

## 🖥 ASCII Mockup – Compare Screen

```text
+--------------------------------------------------+
| Compare                                         |
+--------------------------------------------------+

   Left Date                     Right Date

|*************|-----------|
| Left Photo  | Right     |
|*************|-----------|

        <--- draggable slider --->
```

---

## Display Requirements

* Dates displayed below respective sides
* Smooth slider movement
* No image distortion
* Preserve aspect ratio

---

# Phase 5.4 – Compare Mode & Animation Mode (Advanced)

In this final stage, another selection mode is added.

---

# 🔘 FAB Modes

A second FAB button added on Photos screen:

1. **Compare Mode FAB**
2. **Animate Mode FAB**

Both fab buttons disappear when a mode is entered. An exit-mode fab button appears instead

---

## 🎞 Animate Mode

### Goal

Play multiple photos as animation.

---

## Behavior

1. Activate Animate Mode
2. Select unlimited number of photos
3. Press Play
4. Images displayed sequentially

---

## 🎥 Playback Requirements

* Fixed frame rate
* Frame rate defined by constant in code:

```kotlin
const val ANIMATION_FRAME_RATE = 5 // frames per second
```

* Loop playback
* Smooth transitions
* Preserve aspect ratio

---

## 🖥 ASCII Mockup – Animation Mode

```text
+--------------------------------------------------+
| Photos                                          |
+--------------------------------------------------+

[  📷  ] ✓
[  📷  ] ✓
[  📷  ] ✓
[  📷  ] ✓

----------------------------------------------------
[ 4 selected ]                [ Play ]
```

---

## Animation Screen

```text
+--------------------------------------------------+
| Animation                                       |
+--------------------------------------------------+

                [ Image Display ]

              (Auto switching frames)

Frame Rate: 5 fps
```

---

# 🧠 State Management Considerations

### PhotoState

```kotlin
data class PhotoSelectionState(
    val mode: PhotoMode, // NORMAL, COMPARE, ANIMATE
    val selectedPhotoIds: Set<Long>
)
```

---

# 🔐 Data Integrity Rules

* Photos deleted only on Save on edit measurement screen
* Selection cleared on mode exit
* Animation cannot start with < 2 images
* Compare requires exactly 2 images

---

# 🎯 UX Goals

* Clean visual progress tracking
* Private & secure storage
* High performance
* Clear mode separation
* Minimal accidental actions
* Professional before/after comparison

---

# ✅ Final Summary

✔ Attach photo to measurement
✔ Secure private storage
✔ Preview & delete before save
✔ Photos screen (list → gallery)
✔ Lazy image loading
✔ Compare mode (max 2)
✔ Slider-based overlay comparison
✔ Animation mode (multi-select)
✔ Configurable frame rate
✔ Mode-based FAB interaction

---

This completes the full specification for **Phase 5 – Photo Capture & Comparison System**.
