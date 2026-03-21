package de.t_animal.opensourcebodytracker.core.model

data class ExportSettings(
    val exportToDeviceStorageEnabled: Boolean = false,
    val exportFolderUri: String? = null,
    val automaticExportEnabled: Boolean = false,
    val automaticExportPending: Boolean = false,
    val lastAutomaticExportError: String? = null,
)
