package com.example.scheduler.data

import com.example.scheduler.data.local.ScheduleDao
import com.example.scheduler.data.local.ScheduleEntryEntity
import com.example.scheduler.data.model.ScheduleEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ScheduleRepository(private val scheduleDao: ScheduleDao) {

    fun entries(): Flow<List<ScheduleEntry>> =
        scheduleDao.entries().map { list -> list.map(ScheduleEntryEntity::toDomain) }

    suspend fun addAll(newEntries: List<ScheduleEntry>) {
        if (newEntries.isEmpty()) return
        scheduleDao.insertAll(newEntries.map(ScheduleEntry::toEntity))
    }
}

private fun ScheduleEntry.toEntity(): ScheduleEntryEntity = ScheduleEntryEntity(
    title = title,
    dayOfWeek = dayOfWeek,
    startTime = startTime,
    endTime = endTime,
    location = location,
    travelBufferMinutes = travelBufferMinutes
)

private fun ScheduleEntryEntity.toDomain(): ScheduleEntry = ScheduleEntry(
    title = title,
    dayOfWeek = dayOfWeek,
    startTime = startTime,
    endTime = endTime,
    location = location,
    travelBufferMinutes = travelBufferMinutes
)
