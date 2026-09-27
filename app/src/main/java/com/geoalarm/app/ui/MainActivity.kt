package com.geoalarm.app.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geoalarm.app.GeoAlarmApplication
import com.geoalarm.app.R
import com.geoalarm.app.ui.navigation.GeoAlarmNavHost
import com.geoalarm.app.ui.settings.SettingsViewModel
import com.geoalarm.app.ui.theme.GeoAlarmTheme
import com.geoalarm.app.util.PermissionUtils

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as GeoAlarmApplication

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = GenericViewModelFactory { SettingsViewModel(app.serviceLocator) },
            )
            val settings by settingsViewModel.settings.collectAsState()

            GeoAlarmTheme(themeMode = settings.themeMode, accentColor = Color(settings.accentColorArgb)) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PermissionsGate {
                        GeoAlarmNavHost(app.serviceLocator)
                    }
                }
            }
        }
    }
}

/** Blocks navigation to the app content until location (and, on 33+, notification)
 *  permissions are resolved — geofencing and the lock-screen alert can't work without them. */
@Composable
private fun PermissionsGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var foregroundGranted by remember { mutableStateOf(PermissionUtils.hasFineLocation(context)) }
    var backgroundGranted by remember { mutableStateOf(PermissionUtils.hasBackgroundLocation(context)) }

    val foregroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        foregroundGranted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }
    val backgroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> backgroundGranted = granted }

    when {
        !foregroundGranted -> PermissionRationale {
            val permissions = buildList {
                add(Manifest.permission.ACCESS_FINE_LOCATION)
                add(Manifest.permission.ACCESS_COARSE_LOCATION)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
            }
            foregroundLauncher.launch(permissions.toTypedArray())
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !backgroundGranted -> PermissionRationale {
            backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        else -> content()
    }
}

@Composable
private fun PermissionRationale(onRequest: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.permission_rationale_location), textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRequest) { Text(stringResource(R.string.permission_grant)) }
    }
}
