package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.AppDatabase
import com.example.myapplication.ui.screen.schedule.ScheduleEvent
import com.example.myapplication.ui.screen.schedule.EventType
import com.example.myapplication.ui.screen.schedule.EventStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId

data class ScheduleUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val events: List<ScheduleEvent> = emptyList(),
    val isLoading: Boolean = false
)

class ScheduleViewModel(
    private val database: AppDatabase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()
    
    init {
        loadEvents()
    }
    
    fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        loadEventsForDate(date)
    }
    
    private fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Lấy assignments từ database và chuyển đổi thành events
                database.assignmentDao().getAllAssignments().collect { assignments ->
                    val events = assignments.map { assignment ->
                        ScheduleEvent(
                            id = assignment.id,
                            time = "14:00", // Default time, có thể thêm field time vào Assignment
                            title = assignment.title,
                            subtitle = assignment.description,
                            location = "Online", // Default location
                            type = when (assignment.priority) {
                                com.example.myapplication.data.model.Priority.HIGH -> EventType.ASSIGNMENT
                                com.example.myapplication.data.model.Priority.MEDIUM -> EventType.ASSIGNMENT
                                com.example.myapplication.data.model.Priority.LOW -> EventType.ASSIGNMENT
                            },
                            status = when (assignment.status) {
                                com.example.myapplication.data.model.AssignmentStatus.TODO -> EventStatus.SCHEDULED
                                com.example.myapplication.data.model.AssignmentStatus.IN_PROGRESS -> EventStatus.ONGOING
                                com.example.myapplication.data.model.AssignmentStatus.COMPLETED -> EventStatus.COMPLETED
                            },
                            date = assignment.dueDate?.let { dueDate ->
                                Instant.ofEpochMilli(dueDate)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            } ?: LocalDate.now().plusDays(1)
                        )
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        events = events,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    private fun loadEventsForDate(date: LocalDate) {
        viewModelScope.launch {
            val allEvents = _uiState.value.events
            val eventsForDate = allEvents.filter { it.date == date }
            // Có thể thêm logic filter khác nếu cần
        }
    }
    
    fun addEvent(event: ScheduleEvent) {
        viewModelScope.launch {
            // Logic thêm event mới
            val currentEvents = _uiState.value.events.toMutableList()
            currentEvents.add(event)
            _uiState.value = _uiState.value.copy(events = currentEvents)
        }
    }
    
    fun updateEventStatus(eventId: Long, status: EventStatus) {
        viewModelScope.launch {
            val currentEvents = _uiState.value.events.toMutableList()
            val eventIndex = currentEvents.indexOfFirst { it.id == eventId }
            if (eventIndex != -1) {
                currentEvents[eventIndex] = currentEvents[eventIndex].copy(status = status)
                _uiState.value = _uiState.value.copy(events = currentEvents)
            }
        }
    }
}