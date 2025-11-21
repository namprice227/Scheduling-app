package com.example.scheduler.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalTime

@Entity(
    tableName = "schedule_entries",
    indices = [Index(value = ["day_of_week", "start_time", "title"], unique = true)]
)
data class ScheduleEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    @ColumnInfo(name = "day_of_week") val dayOfWeek: DayOfWeek,
    @ColumnInfo(name = "start_time") val startTime: LocalTime,
    @ColumnInfo(name = "end_time") val endTime: LocalTime,
    val location: String? = null,
    @ColumnInfo(name = "travel_buffer_minutes") val travelBufferMinutes: Int = 0
)
