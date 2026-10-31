package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.data.AppDatabase
import com.example.data.CountdownRepository
import com.example.data.UserPreferencesRepository
import com.example.notification.NotificationHelper
import com.example.ui.screens.EventDetailScreen
import com.example.ui.screens.EventEditScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.StoreScreen
import com.example.ui.theme.MyApplicationTheme

sealed interface Screen {
    object Home : Screen
    data class Detail(val eventId: Long) : Screen
    data class Edit(val eventId: Long?) : Screen
    object Store : Screen
}

class MainActivity : ComponentActivity() {

    private lateinit var repository: CountdownRepository
    private lateinit var userPrefs: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannel(this)

        val db = AppDatabase.getInstance(this)
        repository = CountdownRepository(db.eventDao(), db.categoryDao(), this)
        userPrefs = UserPreferencesRepository(this)

        val initialEventId = extractEventId(intent)

        setContent {
            val isDarkTheme by userPrefs.isDarkTheme.collectAsState()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                CountdownApp(
                    repository = repository,
                    userPrefs = userPrefs,
                    initialEventId = initialEventId
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun extractEventId(intent: Intent?): Long? {
        if (intent == null) return null
        // From widget click
        if (intent.hasExtra("event_id")) {
            val id = intent.getLongExtra("event_id", 0L)
            if (id > 0) return id
        }
        // From deep link: countdown://event?id=123
        val data = intent.data
        if (data != null && data.scheme == "countdown") {
            val idParam = data.getQueryParameter("id")
            val parsedId = idParam?.toLongOrNull()
            if (parsedId != null && parsedId > 0) return parsedId
        }
        return null
    }
}

@Composable
fun CountdownApp(
    repository: CountdownRepository,
    userPrefs: UserPreferencesRepository,
    initialEventId: Long?
) {
    var currentScreen by remember {
        mutableStateOf<Screen>(
            if (initialEventId != null && initialEventId > 0L) {
                Screen.Detail(initialEventId)
            } else {
                Screen.Home
            }
        )
    }

    // Permission launcher for Android 13+ notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Graceful handling */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    when (val screen = currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                repository = repository,
                userPrefs = userPrefs,
                onNavigateToDetail = { eventId ->
                    currentScreen = Screen.Detail(eventId)
                },
                onNavigateToCreate = {
                    currentScreen = Screen.Edit(null)
                },
                onNavigateToStore = {
                    currentScreen = Screen.Store
                }
            )
        }

        is Screen.Detail -> {
            BackHandler {
                currentScreen = Screen.Home
            }
            EventDetailScreen(
                eventId = screen.eventId,
                repository = repository,
                onNavigateBack = {
                    currentScreen = Screen.Home
                },
                onNavigateToEdit = { editEventId ->
                    currentScreen = Screen.Edit(editEventId)
                }
            )
        }

        is Screen.Edit -> {
            BackHandler {
                currentScreen = Screen.Home
            }
            EventEditScreen(
                eventId = screen.eventId,
                repository = repository,
                userPrefs = userPrefs,
                onNavigateBack = {
                    currentScreen = Screen.Home
                }
            )
        }

        is Screen.Store -> {
            BackHandler {
                currentScreen = Screen.Home
            }
            StoreScreen(
                userPrefs = userPrefs,
                onNavigateBack = {
                    currentScreen = Screen.Home
                }
            )
        }
    }
}
