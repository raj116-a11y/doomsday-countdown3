package com.example.data

import android.content.Context
import android.net.Uri
import com.example.model.CountdownEvent
import com.example.model.EventCategory
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class CountdownRepository(
    private val eventDao: EventDao,
    private val categoryDao: CategoryDao,
    private val context: Context
) {
    val allEvents: Flow<List<CountdownEvent>> = eventDao.getAllEvents()
    val allCategories: Flow<List<EventCategory>> = categoryDao.getAllCategories()

    fun getEventsByCategory(categoryId: String): Flow<List<CountdownEvent>> {
        return if (categoryId == "all") {
            eventDao.getAllEvents()
        } else {
            eventDao.getEventsByCategory(categoryId)
        }
    }

    suspend fun getEventById(id: Long): CountdownEvent? = eventDao.getEventById(id)

    suspend fun getPinnedOrNearestEvent(): CountdownEvent? {
        val pinned = eventDao.getPinnedEvent()
        if (pinned != null) return pinned
        return eventDao.getNearestEvent()
    }

    suspend fun saveEvent(event: CountdownEvent): Long {
        return if (event.id == 0L) {
            val insertedId = eventDao.insertEvent(event)
            if (event.isPinnedToWidget) {
                eventDao.setPinnedEvent(insertedId)
            }
            insertedId
        } else {
            eventDao.updateEvent(event)
            if (event.isPinnedToWidget) {
                eventDao.setPinnedEvent(event.id)
            }
            event.id
        }
    }

    suspend fun deleteEvent(event: CountdownEvent) {
        eventDao.deleteEvent(event)
    }

    suspend fun deleteEventById(id: Long) {
        eventDao.deleteEventById(id)
    }

    suspend fun setPinnedEvent(eventId: Long) {
        eventDao.setPinnedEvent(eventId)
    }

    suspend fun saveCategory(category: EventCategory) {
        categoryDao.insertCategory(category)
    }

    suspend fun deleteCustomCategory(categoryId: String) {
        categoryDao.deleteCustomCategoryById(categoryId)
    }

    /**
     * Copy user-selected image from content Uri into internal persistent storage
     * so widgets and offline views have permanent access without permission expiry.
     */
    suspend fun copyImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val customImagesDir = File(context.filesDir, "custom_wallpapers")
            if (!customImagesDir.exists()) {
                customImagesDir.mkdirs()
            }
            val fileName = "custom_bg_${UUID.randomUUID()}.jpg"
            val targetFile = File(customImagesDir, fileName)
            FileOutputStream(targetFile).use { output ->
                inputStream.copyTo(output)
            }
            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
