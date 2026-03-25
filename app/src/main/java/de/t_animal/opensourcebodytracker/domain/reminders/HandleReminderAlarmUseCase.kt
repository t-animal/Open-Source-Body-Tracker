package de.t_animal.opensourcebodytracker.domain.reminders

import de.t_animal.opensourcebodytracker.data.settings.ReminderSettingsRepository
import de.t_animal.opensourcebodytracker.infra.notifications.ReminderNotificationPoster
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class HandleReminderAlarmUseCase @Inject constructor(
    private val reminderSettingsRepository: ReminderSettingsRepository,
    private val reminderAlarmScheduler: ReminderAlarmScheduler,
    private val reminderNotificationPoster: ReminderNotificationPoster,
) {
    suspend operator fun invoke() {
        val settings = reminderSettingsRepository.settingsFlow.first()

        if (!settings.reminderEnabled || settings.reminderWeekdays.isEmpty()) {
            reminderAlarmScheduler.cancelScheduledReminder()
            return
        }

        reminderNotificationPoster.showReminderNotification()

        // schedule next notification
        reminderAlarmScheduler.syncWithSettings(settings)
    }
}
