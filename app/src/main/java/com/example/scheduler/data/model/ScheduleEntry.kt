package com.example.scheduler.data.model

import java.time.DayOfWeek
import java.time.LocalTime

data class ScheduleEntry(
    val title: String,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val location: String? = null,
    val travelBufferMinutes: Int = 0
) {
    val durationMinutes: Long = java.time.Duration.between(startTime, endTime).toMinutes()
}
