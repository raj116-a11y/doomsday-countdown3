package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event_categories")
data class EventCategory(
    @PrimaryKey
    val id: String,
    val name: String,
    val iconName: String = "category",
    val colorHex: String = "#FF3333",
    val isCustom: Boolean = false
)
