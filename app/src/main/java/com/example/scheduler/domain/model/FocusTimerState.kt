package com.example.scheduler.domain.model

data class FocusTimerState(
    val isRunning: Boolean = false,
    val remainingSeconds: Int = 0,
    val completedSessions: Int = 0,
    val targetMinutes: Int = 25
)
