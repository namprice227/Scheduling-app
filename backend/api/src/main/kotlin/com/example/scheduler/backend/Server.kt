package com.example.scheduler.backend

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.DayOfWeek
import java.util.Locale

private val logger = KotlinLogging.logger {}

fun main() {
    embeddedServer(Netty, port = 8080) {
        configureDatabase()
        configureRouting()
    }.start(wait = true)
}

private fun Application.configureDatabase() {
    Database.connect("jdbc:sqlite:activities.db", driver = "org.sqlite.JDBC")
    transaction {
        SchemaUtils.create(Activities)
    }
    logger.info { "Database initialized with activities table" }
}

private fun Application.configureRouting() {
    install(CallLogging)
    install(ContentNegotiation) {
        jackson()
    }

    routing {
        post("/activities/bulk") {
            val payload = runCatching { call.receive<List<ActivityPayload>>() }
                .getOrElse {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid JSON payload: ${it.message}"))
                    return@post
                }

            if (payload.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Payload must contain at least one activity"))
                return@post
            }

            val normalized = payload.mapIndexed { index, request ->
                val dayValue = request.dayOfWeek.toDayValueOrNull()
                    ?: run {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Invalid dayOfWeek at index $index: ${request.dayOfWeek}")
                        )
                        return@post
                    }

                request to dayValue
            }

            val inserted = transaction {
                normalized.sumOf { (request, dayValue) ->
                    Activities.insert { row ->
                        row[title] = request.title
                        row[dayOfWeek] = dayValue
                        row[startTime] = request.startTime
                        row[endTime] = request.endTime
                        row[location] = request.location
                        row[travelBufferMinutes] = request.travelBufferMinutes ?: 0
                    }
                    1
                }
            }

            call.respond(HttpStatusCode.Created, BulkInsertResponse(inserted))
        }
    }
}

data class ActivityPayload(
    val title: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val location: String? = null,
    val travelBufferMinutes: Int? = null
)

data class BulkInsertResponse(val inserted: Int)

data class ErrorResponse(val message: String)

private fun String.toDayValueOrNull(): Int? = runCatching {
    DayOfWeek.valueOf(uppercase()).value
}.recoverCatching {
    DayOfWeek.valueOf(capitalizeFirst().uppercase()).value
}.getOrNull()

private fun String.capitalizeFirst(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

object Activities : org.jetbrains.exposed.dao.id.IntIdTable("activities") {
    val title = varchar("title", length = 255)
    val dayOfWeek = integer("day_of_week")
    val startTime = varchar("start_time", length = 10)
    val endTime = varchar("end_time", length = 10)
    val location = varchar("location", length = 255).nullable()
    val travelBufferMinutes = integer("travel_buffer_minutes")
}
