package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.EventCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM event_categories ORDER BY isCustom ASC, name ASC")
    fun getAllCategories(): Flow<List<EventCategory>>

    @Query("SELECT * FROM event_categories WHERE id = :id")
    suspend fun getCategoryById(id: String): EventCategory?

    @Query("SELECT * FROM event_categories")
    suspend fun getAllCategoriesList(): List<EventCategory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: EventCategory)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategories(categories: List<EventCategory>)

    @Update
    suspend fun updateCategory(category: EventCategory)

    @Delete
    suspend fun deleteCategory(category: EventCategory)

    @Query("DELETE FROM event_categories WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustomCategoryById(id: String)
}
