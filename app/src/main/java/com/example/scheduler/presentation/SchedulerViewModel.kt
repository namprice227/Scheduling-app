package com.example.scheduler.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import com.example.scheduler.data.ScheduleRepository
import com.example.scheduler.data.local.ScheduleDatabase
import com.example.scheduler.data.model.ScheduleEntry
import com.example.scheduler.domain.model.FocusTimerState
import com.example.scheduler.domain.model.GymRecommendation
import com.example.scheduler.domain.model.MovementNudge
import com.example.scheduler.domain.usecase.FocusTimerUseCase
import com.example.scheduler.domain.usecase.GymRecommendationUseCase
import com.example.scheduler.domain.usecase.ScheduleParser
import com.example.scheduler.domain.usecase.StandUpReminderUseCase
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SchedulerViewModel(
    application: Application,
    private val repository: ScheduleRepository = ScheduleRepository(
        ScheduleDatabase.getInstance(application).scheduleDao()
    ),
    private val scheduleParser: ScheduleParser = ScheduleParser(),
    private val gymRecommendationUseCase: GymRecommendationUseCase = GymRecommendationUseCase(),
    private val standUpReminderUseCase: StandUpReminderUseCase = StandUpReminderUseCase(),
    private val focusTimerUseCase: FocusTimerUseCase = FocusTimerUseCase()
) : AndroidViewModel(application) {

    data class UiState(
        val userInput: String = "",
        val scheduleEntries: List<ScheduleEntry> = emptyList(),
        val parsingError: String? = null,
        val gymRecommendation: GymRecommendation? = null,
        val movementNudge: MovementNudge? = null,
        val focusTimer: FocusTimerState = FocusTimerState()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var lastMovement: Instant = Instant.now()
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.entries().collect { entries ->
                _uiState.update { current ->
                    current.copy(
                        scheduleEntries = entries,
                        gymRecommendation = gymRecommendationUseCase.recommend(entries)
                    )
                }
            }
        }
    }

    fun onUserInputChanged(value: String) {
        _uiState.update { it.copy(userInput = value, parsingError = null) }
    }

    fun onSubmitSchedule() {
        val parsed = scheduleParser.parseMany(_uiState.value.userInput)
        if (parsed.isEmpty()) {
            _uiState.update { it.copy(parsingError = "I couldn't understand that. Try adding a day and time.") }
            return
        }
        viewModelScope.launch {
            repository.addAll(parsed)
            _uiState.update { it.copy(userInput = "", parsingError = null) }
        }
    }

    fun markMovement() {
        lastMovement = Instant.now()
        _uiState.update { it.copy(movementNudge = null) }
    }

    fun evaluateMovement() {
        val nudge = standUpReminderUseCase.evaluate(lastMovement)
        _uiState.update { it.copy(movementNudge = nudge) }
    }

    fun startFocusTimer(minutes: Int = 25) {
        if (timerJob?.isActive == true) return
        timerJob = viewModelScope.launch {
            var state = focusTimerUseCase.start(minutes)
            _uiState.update { it.copy(focusTimer = state) }
            while (state.isRunning && state.remainingSeconds > 0) {
                kotlinx.coroutines.delay(1000)
                state = focusTimerUseCase.tick(state)
                _uiState.update { it.copy(focusTimer = state) }
            }
            state = if (state.remainingSeconds <= 0) focusTimerUseCase.complete(state) else focusTimerUseCase.stop(state)
            _uiState.update { it.copy(focusTimer = state) }
        }
    }

    fun stopFocusTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(focusTimer = focusTimerUseCase.stop(it.focusTimer)) }
    }

    fun formatDuration(seconds: Int): String {
        val minutes = TimeUnit.SECONDS.toMinutes(seconds.toLong()).toInt()
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SchedulerViewModel(application)
            }
        }
    }
}
