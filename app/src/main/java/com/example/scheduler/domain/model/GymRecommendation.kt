package com.example.scheduler.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

data class GymRecommendation(
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val travelBufferMinutes: Int,
    val confidence: Double
)
