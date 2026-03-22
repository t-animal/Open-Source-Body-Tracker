package de.t_animal.opensourcebodytracker.core.model

enum class AutomaticExportErrorKey {
    EnableDeviceStorage,
    SelectFolder,
    EnterPassword,
    InvalidFolder,
    PermissionDenied,
    WriteFailed,
    Unknown,
    ;

    companion object {
        fun fromName(name: String): AutomaticExportErrorKey =
            entries.firstOrNull { it.name == name } ?: Unknown
    }
}
