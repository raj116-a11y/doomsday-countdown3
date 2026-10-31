package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.CountdownEvent
import com.example.model.EventCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [CountdownEvent::class, EventCategory::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "doomsday_countdown.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getInstance(context)
                    populateInitialData(database)
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
                val defaultCategories = listOf(
                    EventCategory(id = "all", name = "All Events", iconName = "grid_view", colorHex = "#FFFFFF", isCustom = false),
                    EventCategory(id = "movies", name = "Movies & Series", iconName = "movie", colorHex = "#FF3333", isCustom = false),
                    EventCategory(id = "gaming", name = "Gaming", iconName = "sports_esports", colorHex = "#00FF66", isCustom = false),
                    EventCategory(id = "celebrations", name = "Celebrations", iconName = "cake", colorHex = "#FF007F", isCustom = false),
                    EventCategory(id = "travel", name = "Travel & Trips", iconName = "flight", colorHex = "#00F0FF", isCustom = false),
                    EventCategory(id = "goals", name = "Goals & Milestones", iconName = "flag", colorHex = "#FF9900", isCustom = false)
                )
                database.categoryDao().insertCategories(defaultCategories)

                // Initialize with Avengers: Doomsday matching user's screenshot (47 days, 9 hours, 3 minutes, 50s)
                val now = System.currentTimeMillis()
                val doomsdayTarget = now + (47L * 24 * 60 * 60 * 1000L) + (9L * 60 * 60 * 1000L) + (3L * 60 * 1000L) + 50000L
                val cyberTarget = now + (112L * 24 * 60 * 60 * 1000L) + (14L * 60 * 60 * 1000L)
                val eclipseTarget = now + (205L * 24 * 60 * 60 * 1000L) + (8L * 60 * 60 * 1000L)

                val initialEvents = listOf(
                    CountdownEvent(
                        title = "AVENGERS: DOOMSDAY",
                        targetDateTimeMillis = doomsdayTarget,
                        categoryId = "movies",
                        themeId = "doomsday_red",
                        fontStyle = "dot_matrix",
                        backgroundType = "preset",
                        backgroundResourceOrUri = "bg_doomsday_portal",
                        accentColorHex = "#FF2E2E",
                        secondaryColorHex = "#FF8C00",
                        reminderAdvanceMinutes = 1440,
                        isPinnedToWidget = true,
                        notes = "The Multiverse collides. Earth-616 enters the final countdown."
                    ),
                    CountdownEvent(
                        title = "CYBERPUNK: PROTOCOL ORION",
                        targetDateTimeMillis = cyberTarget,
                        categoryId = "gaming",
                        themeId = "cyber_neon",
                        fontStyle = "digital_led",
                        backgroundType = "preset",
                        backgroundResourceOrUri = "bg_cyber_neon",
                        accentColorHex = "#00F0FF",
                        secondaryColorHex = "#FF007F",
                        reminderAdvanceMinutes = 1440,
                        isPinnedToWidget = false,
                        notes = "Next-gen Night City expansion release."
                    ),
                    CountdownEvent(
                        title = "TOTAL SOLAR ECLIPSE",
                        targetDateTimeMillis = eclipseTarget,
                        categoryId = "goals",
                        themeId = "midnight_oled",
                        fontStyle = "bold_modern",
                        backgroundType = "preset",
                        backgroundResourceOrUri = "bg_doomsday_portal",
                        accentColorHex = "#FFFFFF",
                        secondaryColorHex = "#FF9900",
                        reminderAdvanceMinutes = 4320,
                        isPinnedToWidget = false,
                        notes = "Celestial path of totality observation."
                    )
                )

                for (event in initialEvents) {
                    database.eventDao().insertEvent(event)
                }
            }
        }
    }
}
