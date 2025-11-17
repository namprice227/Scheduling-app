package com.example.scheduler.data

import com.example.scheduler.data.model.ScheduleEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class ScheduleRepository {
    private val entries = MutableStateFlow<List<ScheduleEntry>>(emptyList())

    fun entries(): Flow<List<ScheduleEntry>> = entries

    fun addAll(newEntries: List<ScheduleEntry>) {
        if (newEntries.isEmpty()) return
        entries.value = (entries.value + newEntries)
            .distinctBy { Triple(it.dayOfWeek, it.startTime, it.title) }
    }
}
