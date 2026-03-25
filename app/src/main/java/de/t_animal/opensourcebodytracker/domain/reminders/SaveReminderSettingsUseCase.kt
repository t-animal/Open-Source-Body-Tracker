package de.t_animal.opensourcebodytracker.domain.reminders

import de.t_animal.opensourcebodytracker.core.model.ReminderSettings
import de.t_animal.opensourcebodytracker.data.settings.ReminderSettingsRepository
import javax.inject.Inject

class SaveReminderSettingsUseCase @Inject constructor(
    private val reminderSettingsRepository: ReminderSettingsRepository,
    private val reminderAlarmScheduler: ReminderAlarmScheduler,
) {
    suspend operator fun invoke(settings: ReminderSettings) {
        reminderSettingsRepository.saveSettings(settings)
        reminderAlarmScheduler.syncWithSettings(settings)
    }
}
