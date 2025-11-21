package com.example.scheduler.data.local

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun dayOfWeekToInt(value: DayOfWeek?): Int? = value?.value

    @TypeConverter
    fun intToDayOfWeek(value: Int?): DayOfWeek? = value?.let(DayOfWeek::of)

    @TypeConverter
    fun localTimeToString(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalTime(value: String?): LocalTime? = value?.let(LocalTime::parse)
}
