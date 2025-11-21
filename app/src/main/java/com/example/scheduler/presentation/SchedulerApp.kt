package com.example.scheduler.presentation

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulerApp(viewModel: SchedulerViewModel = viewModel(factory = SchedulerViewModel.provideFactory(LocalContext.current.applicationContext as Application))) {
    val state by viewModel.uiState.collectAsState()
    val formatter = DateTimeFormatter.ofPattern("h:mm a")

    LaunchedEffect(Unit) {
        viewModel.evaluateMovement()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("AI Time Coach") })
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Setup your week",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.userInput,
                        onValueChange = viewModel::onUserInputChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Describe Monday plan…") },
                        supportingText = {
                            Text("e.g. Monday classes 8-11am, travel 20m")
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = viewModel::onSubmitSchedule) {
                        Text("Add to planner")
                    }
                    state.parsingError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }

                if (state.scheduleEntries.isNotEmpty()) {
                    item {
                        Text(
                            "Your schedule",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(state.scheduleEntries) { entry ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(entry.title, fontWeight = FontWeight.Bold)
                                val dayLabel = entry.dayOfWeek.name.lowercase(Locale.getDefault())
                                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                                Text("$dayLabel ${entry.startTime.format(formatter)} - ${entry.endTime.format(formatter)}")
                                entry.location?.let { Text(it) }
                            }
                        }
                    }
                }

                state.gymRecommendation?.let { recommendation ->
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Suggested gym session", fontWeight = FontWeight.Bold)
                                val day = recommendation.dayOfWeek.name.lowercase(Locale.getDefault())
                                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                                Text("$day ${recommendation.startTime.format(formatter)}")
                                Text("Includes ${recommendation.travelBufferMinutes} min travel buffer")
                            }
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Movement reminders", fontWeight = FontWeight.Bold)
                            Text(state.movementNudge?.message ?: "You're all caught up. We'll remind you after 45 min of sitting.")
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(onClick = viewModel::evaluateMovement) {
                                    Text("Check now")
                                }
                                OutlinedButton(onClick = viewModel::markMovement) {
                                    Text("I moved")
                                }
                            }
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Focus mode", fontWeight = FontWeight.Bold)
                            Text(viewModel.formatDuration(state.focusTimer.remainingSeconds))
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(onClick = { viewModel.startFocusTimer(state.focusTimer.targetMinutes) }) {
                                    Text(if (state.focusTimer.isRunning) "Running" else "Start ${state.focusTimer.targetMinutes}m")
                                }
                                OutlinedButton(onClick = viewModel::stopFocusTimer) {
                                    Text("Stop")
                                }
                            }
                            Text("Completed sessions: ${state.focusTimer.completedSessions}")
                        }
                    }
                }
            }
        }
    }
}
