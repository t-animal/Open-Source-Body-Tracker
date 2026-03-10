# Phase 8 – Data Export

This phase introduces the ability for users to **export their data**.  
The export feature serves two main purposes:

- **Creating backups**
- **Transferring data to another device**

Exports will eventually support multiple destinations, but the implementation is introduced **incrementally in several subphases**.

Planned export targets:

- Local device storage
- Google Drive (future phase)

In the initial implementation, **only export to local storage will be supported**.

---

# Overview of Subphases

- **Phase 8.1 – Export Settings UI**
- **Phase 8.2 – Local Export Test File**
- **Phase 8.3 – Encrypted ZIP Export**
- **Phase 8.4 – Automatic Nightly Export**

---

# Phase 8.1 – Export Settings UI

## Goal

Introduce a **settings screen** where users can configure how data exports should work and where exported files should be stored.

In this phase, only the **user interface and folder selection** are implemented.  
No real data export happens yet.

---

# Export Settings Screen

Users can access the export configuration through the **overflow menu entry `Export`** on all main tabs.

Example layout:

```

Export Settings

Export Destination

[ ] Export to Device Storage
[ ] Export to Google Drive (coming later)

Export Folder
[ Select Folder ]

Export Password
[ ******** ]

[ Export Now ]

```

---

# Export Destination

Two export targets are planned:

- Device Storage
- Google Drive

In the **first implementation**, only the **Device Storage option is available**.

```

Export to Device Storage [ ON ]
Export to Google Drive   [ disabled / coming later ]

```

When **Device Storage** is enabled, additional options become available.

---

# Folder Selection

Users can choose the folder where exported files should be stored.

```

Export Folder
[ Select Folder ]

```

Behavior:

- Pressing **Select Folder** opens the Android folder picker.
- The user grants the app permission to write to the folder.
- The selected folder is **persisted** so the user does not need to choose it again.

If the user presses the folder button again, the folder picker opens and the location can be changed.

---

# Export Password

An **input field for a password** is already included in the UI.

```

Export Password
[ ******** ]

```

The password is stored encrypted at rest using an AES-GCM key from Android Keystore,
with ciphertext metadata persisted in app-internal DataStore.

---

# Export Button

The screen includes a manual export button.

```

[ Export Now ]

```

In Phase 8.1 the button is a placeholder and does not perform a real export yet.

---

# Validation

There is a save button that stores the password, folder and enabled/disabled state.

Validation behavior in Phase 8.1:

- If export is enabled and no password is entered, nothing is stored and an inline error is shown.
- If export is enabled and no folder is selected, nothing is stored and an inline error is shown.

---

# Phase 8.2 – Local Export Test File

## Goal

Verify that the selected folder is writable and that the export mechanism works.

When the user presses:

```

Export Now

```

The app will:

1. Access the selected folder
2. Create a simple **text file**
3. Store it in the selected directory

Example file:

```

export_test.txt

```

This phase ensures that:

- Folder permissions work
- The file system integration functions correctly
- Export destinations are valid

No measurement data is exported yet.

---

# Phase 8.3 – Encrypted ZIP Export

## Goal

Implement the **actual data export**.

Pressing **Export Now** will create a **password-protected ZIP archive** containing all user data.

---

# Export Contents

The ZIP archive will include:

### Measurements

All measurement entries exported as **CSV files**.

Example:

```

measurements.csv

```

The CSV contains:

- Timestamp
- All recorded measurement values
- Associated metadata if needed

---

### Images

If measurement photos exist, they are included in a subdirectory.

```

images/
    image-filename-as-stored-in-private-storage.jpg

```

---

# Archive Structure

Example export archive:

```

bodytracker_export.zip

```
measurements.csv

images/
    image-filename-as-stored-in-private-storage.jpg
```

```

---

# Encryption

The ZIP archive is **encrypted using the user-provided password**.

The password entered in the Export Settings screen is used to protect the archive.

---

# Export Location

The ZIP file is saved to the **user-selected export folder**.

Example:

```

/Documents/BodyTrackerExports/
bodytracker_export_2026-03-09.zip

```

If the file exists, it is overridden. Only the two latest exports are kept, the others are deleted.

---

# Phase 8.4 – Automatic Nightly Export

## Goal

Allow automatic backups to occur without user interaction.

---

# Automatic Backup Behavior

Exports are automatically scheduled for:

```

03:00 AM

```

However, an export is only scheduled if **data has changed**.

Examples of changes that trigger a backup:

- A new measurement is recorded
- A measurement is edited
- A photo is added or removed

When such a change occurs, the app schedules an export for **3:00 AM**.

---

# Export Process

The automatic export performs **the same operation as pressing "Export Now"**.

It will:

1. Generate the encrypted ZIP archive
2. Include all measurements and images
3. Save the archive in the configured export folder

---

# Future Extension – Google Drive

In a later phase, the export system will support **Google Drive** as an additional export target.

This will allow:

- Cloud backups
- Cross-device data transfers
- Remote storage redundancy

The architecture is designed so that additional export destinations can be added without changing the export format.

---

# Summary

Phase 8 introduces a **secure data export system** in several stages.

Phase 8.1  
✔ Export configuration UI  
✔ Folder selection  
✔ Export password field  

Phase 8.2  
✔ Test export file written to selected folder  

Phase 8.3  
✔ Encrypted ZIP export  
✔ Measurements exported as CSV  
✔ Images included in archive  

Phase 8.4  
✔ Automatic nightly export at 03:00  
✔ Triggered only when data changes  

The export feature ensures that users can **create backups, migrate devices, and maintain full control of their data while preserving the app's privacy-first design**.
