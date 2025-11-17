package com.example.scheduler.domain.usecase

import com.example.scheduler.data.model.ScheduleEntry
import com.example.scheduler.domain.model.GymRecommendation
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalTime

class GymRecommendationUseCase {
    fun recommend(
        entries: List<ScheduleEntry>,
        desiredWorkoutMinutes: Int = 60,
        dayOrder: List<DayOfWeek> = DayOfWeek.values().toList()
    ): GymRecommendation? {
        if (entries.isEmpty()) return null
        val buffer = entries.map { it.travelBufferMinutes }.filter { it > 0 }.average()
            .takeIf { !it.isNaN() }
            ?.toInt() ?: 15
        val requiredWindow = desiredWorkoutMinutes + buffer * 2

        val grouped = entries.groupBy { it.dayOfWeek }
        for (day in dayOrder) {
            val dayEntries = grouped[day]?.sortedBy { it.startTime } ?: emptyList()
            val window = findWindow(dayEntries, requiredWindow)
            if (window != null) {
                return GymRecommendation(
                    dayOfWeek = day,
                    startTime = window.first,
                    endTime = window.second,
                    travelBufferMinutes = buffer,
                    confidence = 0.65
                )
            }
        }
        return null
    }

    private fun findWindow(entries: List<ScheduleEntry>, requiredMinutes: Int): Pair<LocalTime, LocalTime>? {
        var cursor = LocalTime.of(6, 0)
        val endOfDay = LocalTime.of(22, 0)
        for (entry in entries) {
            val freeMinutes = Duration.between(cursor, entry.startTime).toMinutes()
            if (freeMinutes >= requiredMinutes) {
                return cursor to cursor.plusMinutes(requiredMinutes.toLong())
            }
            cursor = entry.endTime
        }
        val trailingMinutes = Duration.between(cursor, endOfDay).toMinutes()
        return if (trailingMinutes >= requiredMinutes) cursor to cursor.plusMinutes(requiredMinutes.toLong()) else null
    }
}
