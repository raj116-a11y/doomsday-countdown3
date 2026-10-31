package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.CountdownRepository
import com.example.data.UserPreferencesRepository
import com.example.model.CountdownEvent
import com.example.model.EventCategory
import com.example.ui.components.CreateCategoryDialog
import com.example.ui.components.DotMatrixText
import com.example.ui.components.ShareCountdownDialog
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.DoomsdayRed
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.NeonCyan
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: CountdownRepository,
    userPrefs: UserPreferencesRepository,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToStore: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val allEvents by repository.allEvents.collectAsState(initial = emptyList())
    val categories by repository.allCategories.collectAsState(initial = emptyList())
    val coins by userPrefs.coins.collectAsState()
    val isDarkTheme by userPrefs.isDarkTheme.collectAsState()

    var selectedCategoryId by remember { mutableStateOf("all") }
    var isGroupByCategory by remember { mutableStateOf(false) }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var eventToShare by remember { mutableStateOf<CountdownEvent?>(null) }

    // Live clock ticker so seconds update smoothly in real time
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (isActive) {
            currentTimeMillis = System.currentTimeMillis()
            delay(500L)
        }
    }

    val filteredEvents = remember(allEvents, selectedCategoryId) {
        if (selectedCategoryId == "all") {
            allEvents
        } else {
            allEvents.filter { it.categoryId == selectedCategoryId }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(DoomsdayRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DOOMSDAY",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            fontSize = 18.sp
                        )
                        Text(
                            text = " COUNTDOWN",
                            fontWeight = FontWeight.Light,
                            letterSpacing = 2.sp,
                            fontSize = 18.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    // Global Light/Dark Theme toggle
                    IconButton(
                        onClick = {
                            val next = userPrefs.toggleDarkTheme()
                            Toast.makeText(context, if (next) "Dark Mode Enabled" else "Light Mode Enabled", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("toggle_global_theme")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = if (isDarkTheme) CyberAmber else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Group by category toggle
                    IconButton(
                        onClick = { isGroupByCategory = !isGroupByCategory },
                        modifier = Modifier.testTag("toggle_group_by_category")
                    ) {
                        Icon(
                            imageVector = if (isGroupByCategory) Icons.Default.GridView else Icons.Default.ViewAgenda,
                            contentDescription = "Toggle Grouping",
                            tint = if (isGroupByCategory) DoomsdayRed else Color.White
                        )
                    }

                    // Store / Coins pill button
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, GoldCoin.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .clickable { onNavigateToStore() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("home_coins_pill")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = GoldCoin,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "$coins",
                                fontWeight = FontWeight.Bold,
                                color = GoldCoin,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = "Store",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = DoomsdayRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_event_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Countdown")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Countdown", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category horizontal scroll row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category filter chips
                items(categories, key = { it.id }) { cat ->
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

                // "+ New Category" button
                item {
                    OutlinedButton(
                        onClick = { showCreateCategoryDialog = true },
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Category", fontSize = 12.sp)
                    }
                }
            }

            // Main Content: Grouped by Category OR Standard List
            if (filteredEvents.isEmpty()) {
                EmptyEventsView(onNavigateToCreate)
            } else if (isGroupByCategory) {
                // Grouped by Category View
                GroupedEventsView(
                    allEvents = filteredEvents,
                    categories = categories,
                    currentTimeMillis = currentTimeMillis,
                    onNavigateToDetail = onNavigateToDetail,
                    onShareEvent = { eventToShare = it }
                )
            } else {
                // Standard List with Hero Card for Pinned/Top event
                StandardEventsView(
                    events = filteredEvents,
                    currentTimeMillis = currentTimeMillis,
                    onNavigateToDetail = onNavigateToDetail,
                    onShareEvent = { eventToShare = it }
                )
            }
        }
    }

    if (showCreateCategoryDialog) {
        CreateCategoryDialog(
            onDismiss = { showCreateCategoryDialog = false },
            onSaveCategory = { newCategory ->
                coroutineScope.launch {
                    repository.saveCategory(newCategory)
                    selectedCategoryId = newCategory.id
                    Toast.makeText(context, "Category \"${newCategory.name}\" created!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (eventToShare != null) {
        ShareCountdownDialog(
            event = eventToShare!!,
            onDismiss = { eventToShare = null }
        )
    }
}

@Composable
fun StandardEventsView(
    events: List<CountdownEvent>,
    currentTimeMillis: Long,
    onNavigateToDetail: (Long) -> Unit,
    onShareEvent: (CountdownEvent) -> Unit
) {
    val heroEvent = events.firstOrNull { it.isPinnedToWidget } ?: events.firstOrNull()
    val otherEvents = if (heroEvent != null) events.filter { it.id != heroEvent.id } else events

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Hero Card (Screenshot Match)
        if (heroEvent != null) {
            item {
                Text(
                    text = "FEATURED / HOME WIDGET",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = DoomsdayRed,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                HeroCountdownCard(
                    event = heroEvent,
                    currentTimeMillis = currentTimeMillis,
                    onClick = { onNavigateToDetail(heroEvent.id) },
                    onShare = { onShareEvent(heroEvent) }
                )
            }
        }

        if (otherEvents.isNotEmpty()) {
            item {
                Text(
                    text = "ALL COUNTDOWNS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            items(otherEvents, key = { it.id }) { ev ->
                EventCard(
                    event = ev,
                    currentTimeMillis = currentTimeMillis,
                    onClick = { onNavigateToDetail(ev.id) },
                    onShare = { onShareEvent(ev) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun GroupedEventsView(
    allEvents: List<CountdownEvent>,
    categories: List<EventCategory>,
    currentTimeMillis: Long,
    onNavigateToDetail: (Long) -> Unit,
    onShareEvent: (CountdownEvent) -> Unit
) {
    val grouped = remember(allEvents) {
        allEvents.groupBy { it.categoryId }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        grouped.forEach { (categoryId, eventsList) ->
            val cat = categories.find { it.id == categoryId }
            val catName = cat?.name ?: "General"
            val catColor = try {
                Color(android.graphics.Color.parseColor(cat?.colorHex ?: "#FF3333"))
            } catch (e: Exception) {
                DoomsdayRed
            }

            item(key = "header_$categoryId") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(catColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = catName.uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = catColor,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${eventsList.size})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(eventsList, key = { it.id }) { ev ->
                EventCard(
                    event = ev,
                    currentTimeMillis = currentTimeMillis,
                    onClick = { onNavigateToDetail(ev.id) },
                    onShare = { onShareEvent(ev) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun HeroCountdownCard(
    event: CountdownEvent,
    currentTimeMillis: Long,
    onClick: () -> Unit,
    onShare: () -> Unit
) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(event.accentColorHex))
    } catch (e: Exception) {
        DoomsdayRed
    }

    val diffMillis = (event.targetDateTimeMillis - currentTimeMillis).coerceAtLeast(0)
    val totalSeconds = diffMillis / 1000L
    val days = totalSeconds / (24 * 3600)
    val hours = (totalSeconds % (24 * 3600)) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag("hero_event_card_${event.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, accentColor.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            // Background image
            EventBackgroundImage(event = event)

            // Scrim overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0x99050508),
                                Color(0xBB050508),
                                Color(0xF0050508)
                            )
                        )
                    )
            )

            // Content matching screenshot
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.title.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = accentColor,
                        fontSize = 15.sp,
                        letterSpacing = 2.sp,
                        maxLines = 1
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (event.isPinnedToWidget) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(accentColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.PushPin,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "WIDGET",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        IconButton(
                            onClick = onShare,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Middle Row: Big 047 + 9:03:50
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    val daysStr = String.format("%03d", days.coerceIn(0, 999))
                    if (event.fontStyle == "dot_matrix") {
                        DotMatrixText(
                            text = daysStr,
                            activeColor = Color.White,
                            inactiveColor = Color.White.copy(alpha = 0.08f),
                            dotSize = 5.dp,
                            dotSpacing = 2.dp,
                            charSpacing = 8.dp
                        )
                    } else {
                        val family = when (event.fontStyle) {
                            "digital_led", "scifi_mono" -> FontFamily.Monospace
                            else -> FontFamily.SansSerif
                        }
                        Text(
                            text = daysStr,
                            fontSize = 46.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = family,
                            color = Color.White,
                            letterSpacing = if (event.fontStyle == "cinematic") 3.sp else 1.sp
                        )
                    }

                    Text(
                        text = String.format("%d:%02d:%02d", hours, minutes, seconds),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }

                // Bottom Row: "DAYS"
                Text(
                    text = "DAYS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 3.sp
                )
            }
        }
    }
}

@Composable
fun EventCard(
    event: CountdownEvent,
    currentTimeMillis: Long,
    onClick: () -> Unit,
    onShare: () -> Unit
) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(event.accentColorHex))
    } catch (e: Exception) {
        DoomsdayRed
    }

    val diffMillis = (event.targetDateTimeMillis - currentTimeMillis).coerceAtLeast(0)
    val totalSeconds = diffMillis / 1000L
    val days = totalSeconds / (24 * 3600)
    val hours = (totalSeconds % (24 * 3600)) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val targetDateStr = dateFormat.format(Date(event.targetDateTimeMillis))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("event_card_${event.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            // Background image subtle
            EventBackgroundImage(event = event)

            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xF2101015),
                                Color(0xE0101015),
                                Color(0xC0101015)
                            )
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.title.uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        fontSize = 13.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = targetDateStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                // Middle counter row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = String.format("%03d", days.coerceIn(0, 999)),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                    Text(
                        text = "DAYS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = String.format("%d:%02d:%02d", hours, minutes, seconds),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                // Bottom status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (event.notes.isNotEmpty()) {
                        Text(
                            text = event.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f),
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (event.isPinnedToWidget) {
                        Text(
                            text = "📌 HOME WIDGET",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventBackgroundImage(event: CountdownEvent) {
    if (event.backgroundType == "custom_photo" && event.backgroundResourceOrUri.isNotEmpty()) {
        val file = File(event.backgroundResourceOrUri)
        if (file.exists()) {
            AsyncImage(
                model = file,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            return
        }
    }

    val resId = when (event.backgroundResourceOrUri) {
        "bg_cyber_neon" -> R.drawable.bg_cyber_neon
        "bg_doomsday_portal" -> R.drawable.bg_doomsday_portal
        else -> R.drawable.bg_doomsday_portal
    }
    Image(
        painter = painterResource(id = resId),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun EmptyEventsView(onNavigateToCreate: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = DoomsdayRed,
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = "No Countdowns Yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Create your first countdown for movies, games, birthdays, or doomsday events and pin it to your home screen!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(onClick = onNavigateToCreate) {
                Text("Create Countdown")
            }
        }
    }
}
