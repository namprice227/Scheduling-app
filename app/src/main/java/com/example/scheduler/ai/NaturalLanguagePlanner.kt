package com.example.scheduler.ai

import com.example.scheduler.data.model.ScheduleEntry
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.Locale

/**
 * Very small deterministic parser that turns beginner-friendly text like
 * "Monday gym 7-8pm, travel 20m" into a structured [ScheduleEntry].
 * It is intentionally forgiving so we can mock the behavior of the future AI stack.
 */
class NaturalLanguagePlanner {
    private val dayRegex = Regex("(?i)(monday|tuesday|wednesday|thursday|friday|saturday|sunday)")
    private val timeRegex = Regex("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?", RegexOption.IGNORE_CASE)
    private val travelRegex = Regex("travel\\s*(\\d{1,3})\\s*(m|min|minutes)?", RegexOption.IGNORE_CASE)

    fun parse(input: String): ScheduleEntry? {
        val day = dayRegex.find(input)?.value?.let { toDayOfWeek(it) } ?: return null
        val times = timeRegex.findAll(input).toList()
        if (times.isEmpty()) return null

        val (start, end) = if (times.size >= 2) {
            parseTime(times[0].value) to parseTime(times[1].value)
        } else {
            val startTime = parseTime(times[0].value)
            startTime to startTime.plusMinutes(60)
        }

        val title = input.replace(dayRegex, "", ignoreCase = true)
            .replace(timeRegex, "")
            .replace(travelRegex, "")
            .replace("-", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .ifEmpty { "Task" }

        val travel = travelRegex.find(input)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0

        return ScheduleEntry(
            title = title.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            dayOfWeek = day,
            startTime = start,
            endTime = end,
            travelBufferMinutes = travel
        )
    }

    private fun parseTime(raw: String): LocalTime {
        val match = timeRegex.matchEntire(raw.trim())
        val hour = match?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
        val minute = match?.groupValues?.getOrNull(2)?.toIntOrNull() ?: 0
        val suffix = match?.groupValues?.getOrNull(3)
        val normalizedHour = when {
            suffix == null -> hour
            suffix.equals("am", true) -> if (hour == 12) 0 else hour
            suffix.equals("pm", true) -> if (hour == 12) 12 else hour + 12
            else -> hour
        }
        return LocalTime.of(normalizedHour, minute)
    }

    private fun toDayOfWeek(token: String): DayOfWeek = DayOfWeek.valueOf(token.uppercase(Locale.getDefault()))
}
