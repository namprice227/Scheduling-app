package com.example.scheduler.domain.usecase

import com.example.scheduler.domain.model.FocusTimerState

class FocusTimerUseCase {
    fun start(targetMinutes: Int): FocusTimerState = FocusTimerState(
        isRunning = true,
        remainingSeconds = targetMinutes * 60,
        targetMinutes = targetMinutes
    )

    fun tick(previous: FocusTimerState): FocusTimerState =
        if (!previous.isRunning || previous.remainingSeconds <= 0) previous
        else previous.copy(remainingSeconds = previous.remainingSeconds - 1)

    fun complete(previous: FocusTimerState): FocusTimerState = previous.copy(
        isRunning = false,
        remainingSeconds = 0,
        completedSessions = previous.completedSessions + 1
    )

    fun stop(previous: FocusTimerState): FocusTimerState = previous.copy(
        isRunning = false,
        remainingSeconds = 0
    )
}
