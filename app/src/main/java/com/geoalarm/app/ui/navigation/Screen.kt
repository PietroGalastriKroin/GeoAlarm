package com.geoalarm.app.ui.navigation

sealed class Screen(val route: String) {
    data object AlarmList : Screen("alarm_list")
    data object Settings : Screen("settings")
    data object AlarmEdit : Screen("alarm_edit/{$ARG_ALARM_ID}") {
        fun createRoute(alarmId: Long?): String = "alarm_edit/${alarmId ?: NEW_ALARM_ID}"
    }

    companion object {
        const val ARG_ALARM_ID = "alarmId"
        const val NEW_ALARM_ID = 0L
    }
}
