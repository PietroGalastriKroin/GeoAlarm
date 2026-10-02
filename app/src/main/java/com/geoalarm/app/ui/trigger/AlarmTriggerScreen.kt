package com.geoalarm.app.ui.trigger

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.geoalarm.app.R
import com.geoalarm.app.domain.model.DismissStyle
import com.geoalarm.app.domain.model.SnoozeType
import com.geoalarm.app.util.CurrentLocationFetcher
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun AlarmTriggerScreen(
    viewModel: AlarmTriggerViewModel,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
) {
    val alarm by viewModel.alarm.collectAsState()
    val distance by viewModel.distanceMeters.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(alarm?.showDistance) {
        if (alarm?.showDistance != true) return@LaunchedEffect
        while (true) {
            CurrentLocationFetcher.fetch(context)?.let { (lat, lon) -> viewModel.onLocationUpdate(lat, lon) }
            delay(5_000)
        }
    }

    val currentAlarm = alarm ?: return

    Box(modifier = Modifier.fillMaxSize().background(currentAlarm.backgroundColorArgb?.let { Color(it) } ?: Color(0xFF1A1B2E))) {
        if (currentAlarm.backgroundImageUri != null) {
            AsyncImage(
                model = currentAlarm.backgroundImageUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 48.dp)) {
                if (currentAlarm.showClock) {
                    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
                    LaunchedEffect(Unit) {
                        while (true) {
                            now = System.currentTimeMillis()
                            delay(1_000)
                        }
                    }
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(now)),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White,
                    )
                    Spacer(Modifier.height(16.dp))
                }
                Icon(Icons.Filled.Alarm, contentDescription = null, tint = Color.White, modifier = Modifier.height(40.dp))
                Spacer(Modifier.height(8.dp))
                Text(currentAlarm.title, color = Color.White, style = MaterialTheme.typography.titleLarge)
                Text(currentAlarm.message, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyLarge)
                if (currentAlarm.showDistance && distance != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.trigger_distance_away, formatDistance(distance!!)),
                        color = Color.White.copy(alpha = 0.75f),
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 24.dp)) {
                DismissControl(dismissStyle = currentAlarm.dismissStyle, onDismiss = onDismiss)
                if (currentAlarm.snoozeType != SnoozeType.NONE) {
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = onSnooze) {
                        Text(stringResource(R.string.trigger_snooze), color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun DismissControl(dismissStyle: DismissStyle, onDismiss: () -> Unit) {
    when (dismissStyle) {
        DismissStyle.BUTTON -> Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.trigger_dismiss))
        }
        DismissStyle.SWIPE -> SwipeToDismissControl(onDismiss)
        DismissStyle.HOLD -> HoldToDismissControl(onDismiss)
    }
}

@Composable
private fun SwipeToDismissControl(onDismiss: () -> Unit) {
    var dragPx by remember { mutableFloatStateOf(0f) }
    val maxDragPx = 260f
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.trigger_swipe_to_dismiss), color = Color.White.copy(alpha = 0.8f))
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = 0.15f)),
        ) {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .height(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragPx = (dragPx + dragAmount.x).coerceIn(0f, maxDragPx)
                            },
                            onDragEnd = {
                                if (dragPx >= maxDragPx * 0.85f) onDismiss() else dragPx = 0f
                            },
                        )
                    },
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = dragPx.dp)
                        .height(56.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent),
                ) {
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HoldToDismissControl(onDismiss: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(150, easing = LinearEasing), label = "hold")

    LaunchedEffect(pressed) {
        if (pressed) {
            val steps = 30
            repeat(steps) {
                delay(100)
                progress = (it + 1) / steps.toFloat()
            }
            if (progress >= 1f) onDismiss()
        } else {
            progress = 0f
        }
    }

    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(progress = { animatedProgress }, modifier = Modifier.height(96.dp), color = Color.White)
        Button(
            onClick = {},
            interactionSource = interactionSource,
            modifier = Modifier.height(80.dp),
        ) {
            Text(stringResource(R.string.trigger_hold_to_dismiss), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

private fun formatDistance(meters: Double): String =
    if (meters >= 1000) "%.1f km".format(meters / 1000) else "${meters.roundToInt()} m"
