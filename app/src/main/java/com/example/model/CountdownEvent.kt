package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "countdown_events")
data class CountdownEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetDateTimeMillis: Long,
    val categoryId: String = "movies",
    val themeId: String = "doomsday_red",
    val fontStyle: String = "modern_bold", // "modern_bold", "cinematic", "digital_led", "scifi_mono", "dot_matrix"
    val backgroundType: String = "preset", // "preset", "custom_photo"
    val backgroundResourceOrUri: String = "bg_doomsday_portal",
    val accentColorHex: String = "#FF3333",
    val secondaryColorHex: String = "#FF8800",
    val reminderAdvanceMinutes: Int = 1440, // 1 day before by default
    val isPinnedToWidget: Boolean = false,
    val soundEffect: String = "doomsday_alarm", // "doomsday_alarm", "cosmic_chime", "fanfare", "cyber_beep", "silent"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
