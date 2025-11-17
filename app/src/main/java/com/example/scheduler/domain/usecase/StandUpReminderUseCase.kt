package com.example.scheduler.domain.usecase

import com.example.scheduler.domain.model.MovementNudge
import java.time.Duration
import java.time.Instant

class StandUpReminderUseCase {
    fun evaluate(lastMovement: Instant, now: Instant = Instant.now()): MovementNudge? {
        val minutesIdle = Duration.between(lastMovement, now).toMinutes().toInt()
        if (minutesIdle < 45) return null
        val urgency = when {
            minutesIdle >= 90 -> "Time for a longer walk"
            minutesIdle >= 60 -> "Stand, stretch, and hydrate"
            else -> "Quick stand-up break"
        }
        return MovementNudge(
            message = "$urgency — you've been still for $minutesIdle minutes.",
            minutesUntilPrompt = 0
        )
    }
}
