package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VolumeUp
import com.example.util.SoundEffectHelper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CountdownRepository
import com.example.data.UserPreferencesRepository
import com.example.model.CountdownEvent
import com.example.model.EventCategory
import com.example.notification.NotificationHelper
import com.example.ui.components.CreateCategoryDialog
import com.example.ui.components.WidgetPreviewSection
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DoomsdayRed
import com.example.widget.CountdownWidgetProvider
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditScreen(
    eventId: Long?,
    repository: CountdownRepository,
    userPrefs: UserPreferencesRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val categories by repository.allCategories.collectAsState(initial = emptyList())
    val unlockedItems by userPrefs.unlockedItems.collectAsState()

    var title by remember { mutableStateOf("") }
    var targetTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis() + 86400000L * 30) }
    var selectedCategoryId by remember { mutableStateOf("movies") }
    var selectedThemeId by remember { mutableStateOf("doomsday_red") }
    var selectedFontStyle by remember { mutableStateOf("modern_bold") }
    var backgroundType by remember { mutableStateOf("preset") }
    var backgroundUriOrName by remember { mutableStateOf("bg_doomsday_portal") }
    var accentColorHex by remember { mutableStateOf("#FF2E2E") }
    var secondaryColorHex by remember { mutableStateOf("#FF8C00") }
    var reminderAdvanceMinutes by remember { mutableIntStateOf(1440) }
    var isPinned by remember { mutableStateOf(false) }
    var selectedSoundEffect by remember { mutableStateOf("doomsday_alarm") }
    var notes by remember { mutableStateOf("") }

    var showCreateCategoryDialog by remember { mutableStateOf(false) }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val savedPath = repository.copyImageToInternalStorage(uri)
                if (savedPath != null) {
                    backgroundType = "custom_photo"
                    backgroundUriOrName = savedPath
                    Toast.makeText(context, "Custom photo applied!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Could not load photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Load existing event if editing
    LaunchedEffect(eventId) {
        if (eventId != null && eventId > 0L) {
            val existing = repository.getEventById(eventId)
            if (existing != null) {
                title = existing.title
                targetTimeMillis = existing.targetDateTimeMillis
                selectedCategoryId = existing.categoryId
                selectedThemeId = existing.themeId
                selectedFontStyle = existing.fontStyle
                backgroundType = existing.backgroundType
                backgroundUriOrName = existing.backgroundResourceOrUri
                accentColorHex = existing.accentColorHex
                secondaryColorHex = existing.secondaryColorHex
                reminderAdvanceMinutes = existing.reminderAdvanceMinutes
                isPinned = existing.isPinnedToWidget
                selectedSoundEffect = existing.soundEffect
                notes = existing.notes
            }
        }
    }

    val previewEvent = remember(
        title, targetTimeMillis, selectedCategoryId, selectedThemeId,
        selectedFontStyle, backgroundType, backgroundUriOrName, accentColorHex, isPinned, selectedSoundEffect
    ) {
        CountdownEvent(
            id = eventId ?: 0L,
            title = if (title.isBlank()) "EVENT COUNTDOWN" else title,
            targetDateTimeMillis = targetTimeMillis,
            categoryId = selectedCategoryId,
            themeId = selectedThemeId,
            fontStyle = selectedFontStyle,
            backgroundType = backgroundType,
            backgroundResourceOrUri = backgroundUriOrName,
            accentColorHex = accentColorHex,
            secondaryColorHex = secondaryColorHex,
            reminderAdvanceMinutes = reminderAdvanceMinutes,
            isPinnedToWidget = isPinned,
            soundEffect = selectedSoundEffect,
            notes = notes
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (eventId == null || eventId == 0L) "Create Countdown" else "Edit Countdown",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                Toast.makeText(context, "Please enter event title", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            coroutineScope.launch {
                                val savedId = repository.saveEvent(previewEvent)
                                val finalEvent = previewEvent.copy(id = savedId)

                                // Schedule notification
                                NotificationHelper.scheduleEventReminder(context, finalEvent)

                                // Update home screen widgets
                                CountdownWidgetProvider.updateAllWidgets(context)

                                Toast.makeText(context, "Countdown saved!", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DoomsdayRed),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_event_top_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save")
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Live Interactive Preview Section
            WidgetPreviewSection(event = previewEvent)

            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Event Title") },
                placeholder = { Text("e.g. AVENGERS: DOOMSDAY") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("event_title_input")
            )

            // Category Picker
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = { showCreateCategoryDialog = true },
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Category", fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories.filter { it.id != "all" }, key = { it.id }) { cat ->
                        val isSelected = selectedCategoryId == cat.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text(cat.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DoomsdayRed.copy(alpha = 0.25f),
                                selectedLabelColor = DoomsdayRed
                            )
                        )
                    }
                }
            }

            // Target Date & Time Pickers
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Target Date & Time",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val cal = Calendar.getInstance().apply { timeInMillis = targetTimeMillis }
                    val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Date picker button
                        OutlinedButton(
                            onClick = {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        cal.set(Calendar.YEAR, year)
                                        cal.set(Calendar.MONTH, month)
                                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                        targetTimeMillis = cal.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(dateFormat.format(Date(targetTimeMillis)))
                        }

                        // Time picker button
                        OutlinedButton(
                            onClick = {
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                        cal.set(Calendar.MINUTE, minute)
                                        cal.set(Calendar.SECOND, 0)
                                        targetTimeMillis = cal.timeInMillis
                                    },
                                    cal.get(Calendar.HOUR_OF_DAY),
                                    cal.get(Calendar.MINUTE),
                                    false
                                ).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(timeFormat.format(Date(targetTimeMillis)))
                        }
                    }
                }
            }

            // Background & Photo Upload
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Widget & Screen Background",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            modifier = Modifier.testTag("upload_custom_photo_button")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = DoomsdayRed)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Custom Photo", color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Or choose preset wallpaper:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val presetWallpapers = listOf(
                        "bg_doomsday_portal" to "Doomsday Portal",
                        "bg_cyber_neon" to "Cyber Neon City",
                        "dark_void" to "Midnight Void"
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(presetWallpapers) { (key, label) ->
                            val isSelected = backgroundType == "preset" && backgroundUriOrName == key
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    backgroundType = "preset"
                                    backgroundUriOrName = key
                                },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DoomsdayRed.copy(alpha = 0.25f),
                                    selectedLabelColor = DoomsdayRed
                                )
                            )
                        }
                    }
                }
            }

            // Font Style & Colors
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Countdown Display Style",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val fontStyles = listOf(
                            "modern_bold" to "Modern Sleek",
                            "cinematic" to "Cinematic Display",
                            "digital_led" to "Cyber Digital",
                            "scifi_mono" to "Sci-Fi Tech",
                            "dot_matrix" to "Retro Dot Matrix"
                        )
                        items(fontStyles) { (styleKey, styleLabel) ->
                            FilterChip(
                                selected = selectedFontStyle == styleKey,
                                onClick = { selectedFontStyle = styleKey },
                                label = { Text(styleLabel) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DoomsdayRed.copy(alpha = 0.25f),
                                    selectedLabelColor = DoomsdayRed
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Accent Color:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val colorHexList = listOf(
                        "#FF2E2E", "#00FF66", "#00F0FF", "#FF9900",
                        "#9D4EDD", "#FFFFFF", "#FF007F", "#FFD700"
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(colorHexList) { hex ->
                            val isSelected = accentColorHex == hex
                            val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Red }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { accentColorHex = hex }
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (hex == "#FFFFFF") Color.Black else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Notification Reminders
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = DoomsdayRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Upcoming Reminder Notification",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    val reminderOptions = listOf(
                        0 to "At event time",
                        60 to "1 hour before",
                        1440 to "1 day before",
                        4320 to "3 days before",
                        10080 to "1 week before"
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(reminderOptions) { (mins, label) ->
                            FilterChip(
                                selected = reminderAdvanceMinutes == mins,
                                onClick = { reminderAdvanceMinutes = mins },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DoomsdayRed.copy(alpha = 0.25f),
                                    selectedLabelColor = DoomsdayRed
                                )
                            )
                        }
                    }
                }
            }

            // Zero Countdown Sound Effect Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = DoomsdayRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Zero Countdown Sound Effect",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                SoundEffectHelper.playSoundEffect(selectedSoundEffect)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = DoomsdayRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Sound", fontSize = 11.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(SoundEffectHelper.soundOptions) { (key, label) ->
                            FilterChip(
                                selected = selectedSoundEffect == key,
                                onClick = {
                                    selectedSoundEffect = key
                                    SoundEffectHelper.playSoundEffect(key)
                                },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DoomsdayRed.copy(alpha = 0.25f),
                                    selectedLabelColor = DoomsdayRed
                                )
                            )
                        }
                    }
                }
            }

            // Pin to Home Screen Widget Toggle
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PushPin, contentDescription = null, tint = DoomsdayRed)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Set as Primary Home Widget", fontWeight = FontWeight.Bold)
                            Text(
                                "Show this event on all home screen widgets",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DoomsdayRed,
                            checkedTrackColor = DoomsdayRed.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("pin_widget_switch")
                    )
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes & Details (Optional)") },
                placeholder = { Text("Add tickets, milestones, or storyline details...") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showCreateCategoryDialog) {
        CreateCategoryDialog(
            onDismiss = { showCreateCategoryDialog = false },
            onSaveCategory = { newCategory ->
                coroutineScope.launch {
                    repository.saveCategory(newCategory)
                    selectedCategoryId = newCategory.id
                }
            }
        )
    }
}
