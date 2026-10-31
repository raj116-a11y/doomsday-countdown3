package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.CountdownEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM countdown_events ORDER BY targetDateTimeMillis ASC")
    fun getAllEvents(): Flow<List<CountdownEvent>>

    @Query("SELECT * FROM countdown_events WHERE categoryId = :categoryId ORDER BY targetDateTimeMillis ASC")
    fun getEventsByCategory(categoryId: String): Flow<List<CountdownEvent>>

    @Query("SELECT * FROM countdown_events WHERE id = :id")
    suspend fun getEventById(id: Long): CountdownEvent?

    @Query("SELECT * FROM countdown_events WHERE isPinnedToWidget = 1 LIMIT 1")
    suspend fun getPinnedEvent(): CountdownEvent?

    @Query("SELECT * FROM countdown_events ORDER BY targetDateTimeMillis ASC LIMIT 1")
    suspend fun getNearestEvent(): CountdownEvent?

    @Query("SELECT * FROM countdown_events")
    suspend fun getAllEventsList(): List<CountdownEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CountdownEvent): Long

    @Update
    suspend fun updateEvent(event: CountdownEvent)

    @Delete
    suspend fun deleteEvent(event: CountdownEvent)

    @Query("DELETE FROM countdown_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Query("UPDATE countdown_events SET isPinnedToWidget = CASE WHEN id = :eventId THEN 1 ELSE 0 END")
    suspend fun setPinnedEvent(eventId: Long)
}
