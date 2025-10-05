package com.example.respiratoryhealthapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.respiratoryhealthapp.data.model.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reminders: List<Reminder>)

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("SELECT * FROM reminders WHERE id = :id")
    fun getReminderById(id: Long): Flow<Reminder?>

    @Query("SELECT * FROM reminders ORDER BY dateTime ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    // Example: Query to get active reminders, could be useful
    @Query("SELECT * FROM reminders WHERE isActive = 1 ORDER BY dateTime ASC")
    fun getActiveReminders(): Flow<List<Reminder>>

    // You might also want queries for reminders in a certain date range, or by type etc.
} 