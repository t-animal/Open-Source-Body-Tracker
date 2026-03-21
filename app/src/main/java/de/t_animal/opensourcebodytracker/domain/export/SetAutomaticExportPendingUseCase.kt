package de.t_animal.opensourcebodytracker.domain.export

import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class SetAutomaticExportPendingUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(pending: Boolean) {
        val currentSettings = settingsRepository.settingsFlow.first()
        settingsRepository.saveSettings(
            currentSettings.copy(
                automaticExportPending = pending,
            ),
        )
    }
}
