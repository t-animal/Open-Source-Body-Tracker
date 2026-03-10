package de.t_animal.opensourcebodytracker.domain.export

data class ExportExecutionCommand(
    val exportToDeviceStorageEnabled: Boolean,
    val exportFolderUri: String?,
    val exportPassword: String,
)

enum class ExportValidationError {
    EnableDeviceStorage,
    SelectFolder,
    EnterPassword,
}

data class ValidatedExportRunCommand(
    val exportFolderUri: String,
    val exportPassword: String,
)

sealed interface ExportExecutionValidationResult {
    data class Valid(
        val command: ValidatedExportRunCommand,
    ) : ExportExecutionValidationResult

    data class Invalid(
        val error: ExportValidationError,
    ) : ExportExecutionValidationResult
}

sealed interface ExportActionError {
    data class Validation(
        val error: ExportValidationError,
    ) : ExportActionError

    data object InvalidFolder : ExportActionError

    data object PermissionDenied : ExportActionError

    data object WriteFailed : ExportActionError

    data object Unknown : ExportActionError
}

sealed interface ExportActionResult {
    data class Success(
        val exportedFileName: String,
    ) : ExportActionResult

    data class Failure(
        val error: ExportActionError,
    ) : ExportActionResult
}

fun ExportExecutionCommand.normalizedFolderUriOrNull(): String? {
    return exportFolderUri?.trim()?.takeIf { it.isNotEmpty() }
}

fun validateExportCommandForSave(
    command: ExportExecutionCommand,
): ExportValidationError? {
    if (!command.exportToDeviceStorageEnabled) {
        return null
    }

    val normalizedFolderUri = command.normalizedFolderUriOrNull()
    return validateEnabledExportFields(command, normalizedFolderUri)
}

fun validateExportExecutionCommand(
    command: ExportExecutionCommand,
): ExportExecutionValidationResult {
    if (!command.exportToDeviceStorageEnabled) {
        return ExportExecutionValidationResult.Invalid(ExportValidationError.EnableDeviceStorage)
    }

    val normalizedFolderUri = command.normalizedFolderUriOrNull()
    val validationError = validateEnabledExportFields(command, normalizedFolderUri)
    if (validationError != null) {
        return ExportExecutionValidationResult.Invalid(validationError)
    }

    return ExportExecutionValidationResult.Valid(
        ValidatedExportRunCommand(
            exportFolderUri = normalizedFolderUri.orEmpty(),
            exportPassword = command.exportPassword,
        ),
    )
}

private fun validateEnabledExportFields(
    command: ExportExecutionCommand,
    normalizedFolderUri: String?,
): ExportValidationError? {
    if (normalizedFolderUri == null) {
        return ExportValidationError.SelectFolder
    }

    if (command.exportPassword.isBlank()) {
        return ExportValidationError.EnterPassword
    }

    return null
}
