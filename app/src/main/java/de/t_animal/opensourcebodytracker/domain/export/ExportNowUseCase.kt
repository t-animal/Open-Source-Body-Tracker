package de.t_animal.opensourcebodytracker.domain.export

interface ExportNowUseCase {
    suspend operator fun invoke(command: ExportExecutionCommand): ExportActionResult
}