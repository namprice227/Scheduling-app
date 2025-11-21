package com.example.scheduler.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query(
        "SELECT * FROM schedule_entries ORDER BY day_of_week ASC, start_time ASC"
    )
    fun entries(): Flow<List<ScheduleEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<ScheduleEntryEntity>)
}
