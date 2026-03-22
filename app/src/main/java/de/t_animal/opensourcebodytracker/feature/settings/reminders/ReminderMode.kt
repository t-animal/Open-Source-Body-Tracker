package de.t_animal.opensourcebodytracker.feature.settings.reminders

import androidx.annotation.StringRes
import de.t_animal.opensourcebodytracker.R

enum class ReminderMode(
    @StringRes val titleResourceId: Int,
    @StringRes val primaryButtonResourceId: Int,
) {
    Onboarding(
        titleResourceId = R.string.reminder_title_onboarding,
        primaryButtonResourceId = R.string.common_finish,
    ),
    Settings(
        titleResourceId = R.string.reminder_title_settings,
        primaryButtonResourceId = R.string.common_save,
    ),
}
