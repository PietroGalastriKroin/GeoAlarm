package com.geoalarm.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.geoalarm.app.di.ServiceLocator
import com.geoalarm.app.ui.GenericViewModelFactory
import com.geoalarm.app.ui.alarmedit.AlarmEditScreen
import com.geoalarm.app.ui.alarmedit.AlarmEditViewModel
import com.geoalarm.app.ui.alarmlist.AlarmListScreen
import com.geoalarm.app.ui.alarmlist.AlarmListViewModel
import com.geoalarm.app.ui.settings.SettingsScreen
import com.geoalarm.app.ui.settings.SettingsViewModel

@Composable
fun GeoAlarmNavHost(serviceLocator: ServiceLocator) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.AlarmList.route) {
        composable(Screen.AlarmList.route) {
            val viewModel: AlarmListViewModel = viewModel(factory = GenericViewModelFactory { AlarmListViewModel(serviceLocator) })
            AlarmListScreen(
                viewModel = viewModel,
                onAddAlarm = { navController.navigate(Screen.AlarmEdit.createRoute(null)) },
                onEditAlarm = { id -> navController.navigate(Screen.AlarmEdit.createRoute(id)) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
            )
        }

        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = viewModel(factory = GenericViewModelFactory { SettingsViewModel(serviceLocator) })
            SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.AlarmEdit.route,
            arguments = listOf(navArgument(Screen.ARG_ALARM_ID) { type = NavType.LongType }),
        ) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getLong(Screen.ARG_ALARM_ID) ?: Screen.NEW_ALARM_ID
            val viewModel: AlarmEditViewModel = viewModel(
                factory = GenericViewModelFactory { AlarmEditViewModel(serviceLocator, alarmId) },
            )
            AlarmEditScreen(viewModel = viewModel, onDone = { navController.popBackStack() })
        }
    }
}
