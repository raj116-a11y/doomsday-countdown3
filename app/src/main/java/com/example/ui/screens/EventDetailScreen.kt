package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import com.example.util.SoundEffectHelper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CountdownRepository
import com.example.model.CountdownEvent
import com.example.ui.components.DotMatrixText
import com.example.ui.components.ShareCountdownDialog
import com.example.ui.components.WidgetPreviewSection
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DoomsdayRed
import com.example.ui.theme.NeonCyan
import com.example.widget.CountdownWidgetProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: Long,
    repository: CountdownRepository,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var event by remember { mutableStateOf<CountdownEvent?>(null) }
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var showShareDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var hasPlayedZeroSound by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) {
        event = repository.getEventById(eventId)
    }

    // High frequency ticker for smooth countdown
    LaunchedEffect(Unit) {
        while (isActive) {
            currentTimeMillis = System.currentTimeMillis()
            delay(100L) // 10 ticks per second for smooth countdown
        }
    }

    if (event == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading countdown...", color = Color.White)
        }
        return
    }

    val currentEvent = event!!
    val accentColor = try {
        Color(android.graphics.Color.parseColor(currentEvent.accentColorHex))
    } catch (e: Exception) {
        DoomsdayRed
    }

    val diffMillis = (currentEvent.targetDateTimeMillis - currentTimeMillis).coerceAtLeast(0)
    val totalSeconds = diffMillis / 1000L
    val days = totalSeconds / (24 * 3600)
    val hours = (totalSeconds % (24 * 3600)) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val milliseconds = (diffMillis % 1000) / 100

    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm:ss a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(currentEvent.targetDateTimeMillis))
    val formattedTime = timeFormat.format(Date(currentEvent.targetDateTimeMillis))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentEvent.title,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                    IconButton(onClick = { onNavigateToEdit(currentEvent.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    }
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Hero Countdown Display
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    accentColor.copy(alpha = 0.12f),
                                    DarkSurface
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Title header
                        Text(
                            text = currentEvent.title.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Large Days Counter
                        val daysStr = String.format("%03d", days.coerceIn(0, 999))
                        if (currentEvent.fontStyle == "dot_matrix") {
                            DotMatrixText(
                                text = daysStr,
                                activeColor = Color.White,
                                inactiveColor = Color.White.copy(alpha = 0.08f),
                                dotSize = 6.dp,
                                dotSpacing = 2.dp,
                                charSpacing = 10.dp
                            )
                        } else {
                            val family = when (currentEvent.fontStyle) {
                                "digital_led", "scifi_mono" -> FontFamily.Monospace
                                else -> FontFamily.SansSerif
                            }
                            Text(
                                text = daysStr,
                                fontSize = 58.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = family,
                                color = Color.White,
                                letterSpacing = if (currentEvent.fontStyle == "cinematic") 4.sp else 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "DAYS REMAINING",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 4.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Hours : Minutes : Seconds : Millis
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TimeUnitBox(value = String.format("%02d", hours), label = "HRS", accent = accentColor)
                            Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            TimeUnitBox(value = String.format("%02d", minutes), label = "MIN", accent = accentColor)
                            Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            TimeUnitBox(value = String.format("%02d", seconds), label = "SEC", accent = accentColor)
                            Text(".", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            TimeUnitBox(value = "$milliseconds", label = "MS", accent = accentColor)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Date & Time stamp
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Target: $formattedDate at $formattedTime",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Quick Pin / Unpin as Primary Home Widget Button
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = null,
                            tint = if (currentEvent.isPinnedToWidget) DoomsdayRed else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (currentEvent.isPinnedToWidget) "Active Primary Widget" else "Set as Primary Widget",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (currentEvent.isPinnedToWidget) "Displaying on home screen" else "Tap to feature on home screen",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!currentEvent.isPinnedToWidget) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    repository.setPinnedEvent(currentEvent.id)
                                    event = repository.getEventById(currentEvent.id)
                                    CountdownWidgetProvider.updateAllWidgets(context)
                                    Toast.makeText(context, "Pinned to home screen widgets!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DoomsdayRed),
                            modifier = Modifier.testTag("set_primary_widget_button")
                        ) {
                            Text("Pin")
                        }
                    }
                }
            }

            // Zero Countdown Sound Effect card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = accentColor
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            val soundName = SoundEffectHelper.soundOptions.find { it.first == currentEvent.soundEffect }?.second ?: "Default Alarm"
                            Text("Zero Alert: $soundName", fontWeight = FontWeight.Bold)
                            Text(
                                "Sound effect when countdown finishes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = {
                            SoundEffectHelper.playSoundEffect(currentEvent.soundEffect)
                            Toast.makeText(context, "Playing zero countdown sound", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Play", fontSize = 11.sp, color = Color.White)
                    }
                }
            }

            // Live Home Screen Widget Preview & Pin
            WidgetPreviewSection(event = currentEvent)

            // Notes card if available
            if (currentEvent.notes.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Notes & Mission Intel",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentEvent.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Share & Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showShareDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DoomsdayRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("detail_share_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Event")
                }

                FilledTonalButton(
                    onClick = { onNavigateToEdit(currentEvent.id) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Customize")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showShareDialog) {
        ShareCountdownDialog(
            event = currentEvent,
            onDismiss = { showShareDialog = false }
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Countdown?") },
            text = { Text("Are you sure you want to remove \"${currentEvent.title}\"? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository.deleteEvent(currentEvent)
                            CountdownWidgetProvider.updateAllWidgets(context)
                            Toast.makeText(context, "Countdown deleted", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun TimeUnitBox(
    value: String,
    label: String,
    accent: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurfaceVariant)
                .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
