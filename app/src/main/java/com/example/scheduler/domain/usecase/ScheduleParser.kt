package com.example.scheduler.domain.usecase

import com.example.scheduler.ai.NaturalLanguagePlanner
import com.example.scheduler.data.model.ScheduleEntry

class ScheduleParser(
    private val planner: NaturalLanguagePlanner = NaturalLanguagePlanner()
) {
    fun parseMany(input: String): List<ScheduleEntry> = input
        .split("\n")
        .mapNotNull { line -> planner.parse(line) }
}
