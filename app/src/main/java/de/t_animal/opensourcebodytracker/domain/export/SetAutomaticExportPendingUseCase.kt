package de.t_animal.opensourcebodytracker.domain.export

import de.t_animal.opensourcebodytracker.data.settings.ExportSettingsRepository
import javax.inject.Inject

class SetAutomaticExportPendingUseCase @Inject constructor(
    private val exportSettingsRepository: ExportSettingsRepository,
) {
    suspend operator fun invoke(pending: Boolean) {
        exportSettingsRepository.updateSettings { it.copy(automaticExportPending = pending) }
    }
}
