package com.geoalarm.app.ui.alarmedit

import android.content.Intent
import android.media.RingtoneManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.geoalarm.app.R
import com.geoalarm.app.domain.model.DismissStyle
import com.geoalarm.app.domain.model.GeofenceTransition
import com.geoalarm.app.domain.model.SnoozeType
import com.geoalarm.app.domain.model.VibrationPattern
import com.geoalarm.app.ui.theme.AccentColorSwatches
import com.geoalarm.app.util.CurrentLocationFetcher
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditScreen(
    viewModel: AlarmEditViewModel,
    onDone: () -> Unit,
) {
    val alarm by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showMap by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { onDone() }
    }

    val ringtonePicker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.getParcelableExtra<android.net.Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        viewModel.update { it.copy(soundUri = uri?.toString()) }
    }
    val audioImportPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        viewModel.update { it.copy(soundUri = uri.toString()) }
    }
    val imageImportPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        viewModel.update { it.copy(backgroundImageUri = uri.toString(), backgroundColorArgb = null) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (viewModel.isNew) stringResource(R.string.alarm_list_add) else alarm.title.ifBlank { "—" },
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDone) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
                },
                actions = {
                    if (!viewModel.isNew) {
                        IconButton(onClick = viewModel::delete) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = stringResource(R.string.edit_delete))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SectionCard {
                    OutlinedTextField(
                        value = alarm.title,
                        onValueChange = { title -> viewModel.update { it.copy(title = title) } },
                        label = { Text(stringResource(R.string.edit_title_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = alarm.message,
                        onValueChange = { message -> viewModel.update { it.copy(message = message) } },
                        label = { Text(stringResource(R.string.edit_message_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Alarme ativo", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Switch(checked = alarm.isEnabled, onCheckedChange = { enabled -> viewModel.update { it.copy(isEnabled = enabled) } })
                    }
                }
            }

            item {
                SectionCard(title = stringResource(R.string.edit_section_location)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = "%.5f".format(alarm.latitude),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.setLocation(it, alarm.longitude) } },
                            label = { Text("Latitude") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = "%.5f".format(alarm.longitude),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.setLocation(alarm.latitude, it) } },
                            label = { Text("Longitude") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { showMap = !showMap }, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.edit_pick_on_map))
                        }
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    CurrentLocationFetcher.fetch(context)?.let { (lat, lon) -> viewModel.setLocation(lat, lon) }
                                }
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.edit_use_current_location))
                        }
                    }
                    if (showMap) {
                        LocationPickerMap(
                            initialLatitude = alarm.latitude,
                            initialLongitude = alarm.longitude,
                            onCenterChanged = { lat, lon -> viewModel.setLocation(lat, lon) },
                            modifier = Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(16.dp)),
                        )
                    }
                    LabeledSlider(
                        label = stringResource(R.string.edit_radius),
                        valueText = "${alarm.radiusMeters.toInt()} m",
                        value = alarm.radiusMeters,
                        onValueChange = { r -> viewModel.update { it.copy(radiusMeters = r) } },
                        valueRange = 50f..2000f,
                    )
                    ChipGroup(
                        items = listOf(GeofenceTransition.ENTER, GeofenceTransition.EXIT),
                        isSelected = { it == alarm.transition },
                        onSelect = viewModel::setTransition,
                        label = { stringResource(if (it == GeofenceTransition.ENTER) R.string.alarm_transition_enter else R.string.alarm_transition_exit) },
                    )
                }
            }

            item {
                SectionCard(title = stringResource(R.string.edit_section_schedule)) {
                    ChipGroup(
                        items = DayOfWeek.values().toList(),
                        isSelected = { it in alarm.activeDays },
                        onSelect = viewModel::toggleDay,
                        label = { it.getDisplayName(TextStyle.SHORT, Locale("pt", "BR")).replaceFirstChar(Char::uppercase) },
                    )
                }
            }

            item {
                SectionCard(title = stringResource(R.string.edit_section_sound)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.edit_sound_enabled), modifier = Modifier.weight(1f))
                        Switch(checked = alarm.soundEnabled, onCheckedChange = { v -> viewModel.update { it.copy(soundEnabled = v) } })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                    alarm.soundUri?.let { putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, android.net.Uri.parse(it)) }
                                }
                                ringtonePicker.launch(intent)
                            },
                            modifier = Modifier.weight(1f),
                        ) { Text(stringResource(R.string.edit_choose_ringtone), maxLines = 1) }
                        OutlinedButton(
                            onClick = { audioImportPicker.launch(arrayOf("audio/*")) },
                            modifier = Modifier.weight(1f),
                        ) { Text(stringResource(R.string.edit_import_audio), maxLines = 1) }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.edit_vibration_enabled), modifier = Modifier.weight(1f))
                        Switch(checked = alarm.vibrationEnabled, onCheckedChange = { v -> viewModel.update { it.copy(vibrationEnabled = v) } })
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SubLabel(stringResource(R.string.edit_vibration_pattern))
                        ChipGroup(
                            items = VibrationPattern.entries,
                            isSelected = { it == alarm.vibrationPattern },
                            onSelect = { p -> viewModel.update { it.copy(vibrationPattern = p) } },
                            label = { vibrationPatternLabel(it) },
                        )
                    }

                    LabeledSlider(
                        label = stringResource(R.string.edit_volume_fade_in),
                        valueText = "${alarm.volumeFadeInSeconds}s",
                        value = alarm.volumeFadeInSeconds.toFloat(),
                        onValueChange = { v -> viewModel.update { it.copy(volumeFadeInSeconds = v.toInt()) } },
                        valueRange = 0f..60f,
                        steps = 11,
                    )
                }
            }

            item {
                SectionCard(title = stringResource(R.string.edit_section_snooze)) {
                    ChipGroup(
                        items = SnoozeType.entries,
                        isSelected = { it == alarm.snoozeType },
                        onSelect = { t -> viewModel.update { it.copy(snoozeType = t) } },
                        label = { snoozeTypeLabel(it) },
                    )
                    when (alarm.snoozeType) {
                        SnoozeType.TIME -> LabeledSlider(
                            label = stringResource(R.string.edit_snooze_minutes),
                            valueText = "${alarm.snoozeMinutes} min",
                            value = alarm.snoozeMinutes.toFloat(),
                            onValueChange = { v -> viewModel.update { it.copy(snoozeMinutes = v.toInt()) } },
                            valueRange = 1f..30f,
                        )
                        SnoozeType.DISTANCE -> LabeledSlider(
                            label = stringResource(R.string.edit_snooze_distance),
                            valueText = "${alarm.snoozeDistanceMeters.toInt()} m",
                            value = alarm.snoozeDistanceMeters,
                            onValueChange = { v -> viewModel.update { it.copy(snoozeDistanceMeters = v) } },
                            valueRange = 10f..500f,
                        )
                        SnoozeType.NONE -> Unit
                    }
                }
            }

            item {
                SectionCard(title = stringResource(R.string.edit_section_lockscreen)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SubLabel(stringResource(R.string.edit_dismiss_style))
                        ChipGroup(
                            items = DismissStyle.entries,
                            isSelected = { it == alarm.dismissStyle },
                            onSelect = { s -> viewModel.update { it.copy(dismissStyle = s) } },
                            label = { dismissStyleLabel(it) },
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.edit_show_clock), modifier = Modifier.weight(1f))
                        Switch(checked = alarm.showClock, onCheckedChange = { v -> viewModel.update { it.copy(showClock = v) } })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.edit_show_distance), modifier = Modifier.weight(1f))
                        Switch(checked = alarm.showDistance, onCheckedChange = { v -> viewModel.update { it.copy(showDistance = v) } })
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SubLabel(stringResource(R.string.edit_background))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AccentColorSwatches.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable {
                                            viewModel.update { it.copy(backgroundColorArgb = color.value.toLong(), backgroundImageUri = null) }
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (alarm.backgroundColorArgb == color.value.toLong()) {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                                    }
                                }
                            }
                        }
                        OutlinedButton(onClick = { imageImportPicker.launch(arrayOf("image/*")) }) {
                            Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Escolher imagem de fundo")
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = viewModel::save,
                    modifier = Modifier.fillMaxWidth().height(52.dp).padding(bottom = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(stringResource(R.string.edit_save), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (title != null) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            content()
        }
    }
}

@Composable
private fun SubLabel(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> ChipGroup(
    items: List<T>,
    isSelected: (T) -> Boolean,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items.forEach { item ->
            FilterChip(
                selected = isSelected(item),
                onClick = { onSelect(item) },
                label = { Text(label(item), maxLines = 1) },
            )
        }
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    valueText: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            SubLabel(label)
            Spacer(Modifier.weight(1f))
            Text(valueText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = valueRange, steps = steps)
    }
}

@Composable
private fun vibrationPatternLabel(pattern: VibrationPattern): String = when (pattern) {
    VibrationPattern.NONE -> stringResource(R.string.vibration_none)
    VibrationPattern.CONTINUOUS -> stringResource(R.string.vibration_continuous)
    VibrationPattern.SHORT_PULSE -> stringResource(R.string.vibration_short_pulse)
    VibrationPattern.SOS -> stringResource(R.string.vibration_sos)
    VibrationPattern.HEARTBEAT -> stringResource(R.string.vibration_heartbeat)
}

@Composable
private fun snoozeTypeLabel(type: SnoozeType): String = when (type) {
    SnoozeType.NONE -> stringResource(R.string.snooze_none)
    SnoozeType.TIME -> stringResource(R.string.snooze_time)
    SnoozeType.DISTANCE -> stringResource(R.string.snooze_distance)
}

@Composable
private fun dismissStyleLabel(style: DismissStyle): String = when (style) {
    DismissStyle.BUTTON -> stringResource(R.string.dismiss_style_button)
    DismissStyle.SWIPE -> stringResource(R.string.dismiss_style_swipe)
    DismissStyle.HOLD -> stringResource(R.string.dismiss_style_hold)
}
