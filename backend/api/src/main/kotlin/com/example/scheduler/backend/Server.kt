package com.example.scheduler.backend

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.Locale
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

private val dayRegex = Regex("(?i)(monday|tuesday|wednesday|thursday|friday|saturday|sunday)")
private val timeRegex = Regex("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?", RegexOption.IGNORE_CASE)
private val travelRegex = Regex("travel\\s*(\\d{1,3})\\s*(m|min|minutes)?", RegexOption.IGNORE_CASE)

fun main() {
    embeddedServer(Netty, port = 8080) { module() }.start(wait = true)
}

fun Application.module() {
    configurePlugins()
    configureDatabase()
    routing {
        post("/activities/bulk") {
            val payload = runCatching { call.receive<List<ActivityRequest>>() }
                .getOrElse {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid JSON payload"))
                    return@post
                }

            if (payload.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Payload must not be empty"))
                return@post
            }

            val activities = mutableListOf<Activity>()
            payload.forEachIndexed { index, request ->
                val result = request.toDomain(index)
                if (result.error != null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to result.error))
                    return@post
                }
                result.activity?.let { activities.add(it) }
            }

            val inserted = insertActivities(activities)
            call.respond(HttpStatusCode.Created, mapOf("inserted" to inserted))
        }

        post("/activities/parse") {
            val request = runCatching { call.receive<ParseActivitiesRequest>() }
                .getOrElse {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid JSON payload"))
                    return@post
                }

            val lines = request.lines?.filter { it.isNotBlank() } ?: emptyList()
            if (lines.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "No activities provided"))
                return@post
            }

            val parser = NaturalLanguageParser()
            val parsedResults = lines.mapIndexed { index, line ->
                parser.parse(line)?.let { ParsedActivityResult(index, it) }
                    ?: ParsedActivityResult(index, null, "Could not understand activity description")
            }

            val successful = parsedResults.mapNotNull { it.activity }
            val errors = parsedResults.filter { it.error != null }
                .map { ParsingError(index = it.index, message = it.error ?: "Unknown error") }

            val inserted = if (request.persist != false) insertActivities(successful) else 0

            call.respond(
                HttpStatusCode.OK,
                ParsedActivitiesResponse(
                    activities = successful.map(Activity::toResponse),
                    errors = errors,
                    inserted = inserted
                )
            )
        }
    }
}

private fun ActivityRequest.toDomain(index: Int): ActivityValidationResult {
    val day = toDayOfWeek(dayOfWeek)
        ?: return ActivityValidationResult(error = "Invalid dayOfWeek at index $index: $dayOfWeek")

    val start = parseTime(startTime)
        ?: return ActivityValidationResult(error = "Invalid startTime at index $index: $startTime")

    val end = parseTime(endTime)
        ?: return ActivityValidationResult(error = "Invalid endTime at index $index: $endTime")

    return ActivityValidationResult(
        activity = Activity(
            title = title.trim(),
            dayOfWeek = day,
            startTime = start,
            endTime = end,
            location = location?.takeIf { it.isNotBlank() },
            travelBufferMinutes = travelBufferMinutes ?: 0
        )
    )
}

private fun Activity.toResponse(): ActivityResponse = ActivityResponse(
    title = title,
    dayOfWeek = dayOfWeek.name,
    startTime = startTime.toString(),
    endTime = endTime.toString(),
    location = location,
    travelBufferMinutes = travelBufferMinutes
)

private fun parseTime(raw: String?): LocalTime? {
    val match = raw?.trim()?.let { token -> timeRegex.matchEntire(token) } ?: return null
    val hour = match.groupValues.getOrNull(1)?.toIntOrNull() ?: return null
    val minute = match.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() }?.toIntOrNull() ?: 0
    val suffix = match.groupValues.getOrNull(3)
    val normalizedHour = when {
        suffix.isNullOrBlank() -> hour
        suffix.equals("am", ignoreCase = true) -> if (hour == 12) 0 else hour
        suffix.equals("pm", ignoreCase = true) -> if (hour == 12) 12 else hour + 12
        else -> hour
    }
    if (normalizedHour !in 0..23 || minute !in 0..59) return null
    return LocalTime.of(normalizedHour, minute)
}

private fun toDayOfWeek(raw: String?): DayOfWeek? = raw?.let {
    runCatching { DayOfWeek.valueOf(it.uppercase(Locale.getDefault())) }.getOrNull()
}

private fun Application.configurePlugins() {
    install(ContentNegotiation) {
        jackson()
    }
}

private fun configureDatabase() {
    Database.connect("jdbc:sqlite:activities.db", driver = "org.sqlite.JDBC")
    TransactionManager.defaultDatabase?.let { db ->
        transaction(db) {
            SchemaUtils.create(ActivitiesTable)
        }
    }
}

private fun insertActivities(activities: List<Activity>): Int {
    if (activities.isEmpty()) return 0
    return transaction {
        activities.forEach { activity ->
            ActivitiesTable.insert { row ->
                row[title] = activity.title
                row[dayOfWeek] = activity.dayOfWeek.name.uppercase()
                row[startTime] = activity.startTime.toString()
                row[endTime] = activity.endTime.toString()
                row[location] = activity.location
                row[travelBufferMinutes] = activity.travelBufferMinutes
            }
        }
        activities.size
    }
}

private object ActivitiesTable : Table("activities") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", length = 255)
    val dayOfWeek = varchar("dayOfWeek", length = 16)
    val startTime = varchar("startTime", length = 5)
    val endTime = varchar("endTime", length = 5)
    val location = varchar("location", length = 255).nullable()
    val travelBufferMinutes = integer("travelBufferMinutes").default(0)

    override val primaryKey = PrimaryKey(id)
}

private data class ActivityRequest(
    val title: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val location: String? = null,
    val travelBufferMinutes: Int? = 0
)

private data class Activity(
    val title: String,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val location: String? = null,
    val travelBufferMinutes: Int = 0
)

private data class ActivityResponse(
    val title: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val location: String?,
    val travelBufferMinutes: Int
)

private data class ActivityValidationResult(
    val activity: Activity? = null,
    val error: String? = null
)

private data class ParseActivitiesRequest(
    val lines: List<String>?,
    val persist: Boolean? = true
)

private data class ParsedActivitiesResponse(
    val activities: List<ActivityResponse>,
    val errors: List<ParsingError>,
    val inserted: Int
)

private data class ParsingError(
    val index: Int,
    val message: String
)

private data class ParsedActivityResult(
    val index: Int,
    val activity: Activity?,
    val error: String? = null
)

private class NaturalLanguageParser {
    fun parse(input: String): Activity? {
        val day = dayRegex.find(input)?.value?.let { toDayOfWeek(it) } ?: return null
        val times = timeRegex.findAll(input).toList()
        if (times.isEmpty()) return null

        val (start, end) = if (times.size >= 2) {
            parseTime(times[0].value) to parseTime(times[1].value)
        } else {
            val startTime = parseTime(times[0].value)
            startTime to startTime?.plusMinutes(60)
        }

        if (start == null || end == null) return null

        val title = input.replace(dayRegex, "")
            .replace(timeRegex, "")
            .replace(travelRegex, "")
            .replace("-", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .ifEmpty { "Task" }

        val travel = travelRegex.find(input)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0

        return Activity(
            title = title.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
            },
            dayOfWeek = day,
            startTime = start,
            endTime = end,
            location = null,
            travelBufferMinutes = travel
        )
    }
}
