package de.t_animal.opensourcebodytracker.infra.notifications

object ReminderNotificationContract {
    const val ReminderNotificationId = 4223
    const val OpenAddMeasurementRequestCode = 4223
    const val ReminderAlarmRequestCode = 4224

    const val OpenAddMeasurementScreenAction =
        "de.t_animal.opensourcebodytracker.notification.OPEN_ADD_MEASUREMENT"
    const val OpenAddMeasurementScreen = "notificationOpenAddMeasurement"

    const val ReminderAlarmAction =
        "de.t_animal.opensourcebodytracker.notification.REMINDER_ALARM"
}
