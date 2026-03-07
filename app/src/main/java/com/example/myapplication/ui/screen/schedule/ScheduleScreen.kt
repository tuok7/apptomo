package com.example.myapplication.ui.screen.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

data class ScheduleEvent(
    val id: Long,
    val time: String,
    val title: String,
    val subtitle: String,
    val location: String,
    val type: EventType,
    val status: EventStatus = EventStatus.SCHEDULED,
    val date: LocalDate
)

enum class EventType(val displayName: String, val color: Color, val icon: ImageVector) {
    MATH("Toán học", Color(0xFF2196F3), Icons.Default.Calculate),
    PHYSICS("Vật lý", Color(0xFF8B5CF6), Icons.Default.Science),
    ASSIGNMENT("Hạn chót bài tập", Color(0xFFFF9800), Icons.Default.Assignment),
    MEETING("Họp nhóm", Color(0xFF4CAF50), Icons.Default.Group),
    EXAM("Kiểm tra", Color(0xFFEF4444), Icons.Default.Quiz)
}

enum class EventStatus {
    SCHEDULED, ONGOING, COMPLETED, CANCELLED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    navController: NavHostController
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = remember { com.example.myapplication.data.database.AppDatabase.getDatabase(context) }
    val viewModel = remember { com.example.myapplication.ui.viewmodel.ScheduleViewModel(database) }
    val uiState by viewModel.uiState.collectAsState()
    
    val currentWeek = remember(uiState.selectedDate) { getWeekDates(uiState.selectedDate) }
    val todayEvents = uiState.events.filter { it.date == uiState.selectedDate }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Lịch trình",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Tìm kiếm",
                            tint = Color(0xFF6B7280)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add new event */ },
                containerColor = Color(0xFF8B5CF6),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm sự kiện")
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Week Calendar
            item {
                WeekCalendar(
                    currentWeek = currentWeek,
                    selectedDate = uiState.selectedDate,
                    onDateSelected = { viewModel.selectDate(it) }
                )
            }
            
            // Today's Schedule Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hôm nay, ${uiState.selectedDate.format(DateTimeFormatter.ofPattern("dd 'Tháng' M"))}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE0E7FF)
                    ) {
                        Text(
                            text = "${todayEvents.size} buổi học",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6366F1),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Loading state
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF8B5CF6))
                    }
                }
            }
            // Events List
            else if (todayEvents.isEmpty()) {
                item {
                    EmptyScheduleCard()
                }
            } else {
                items(todayEvents) { event ->
                    EventCard(
                        event = event,
                        onClick = { /* Navigate to event detail */ },
                        onStatusChange = { status -> viewModel.updateEventStatus(event.id, status) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekCalendar(
    currentWeek: List<LocalDate>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(currentWeek) { date ->
                    WeekDayItem(
                        date = date,
                        isSelected = date == selectedDate,
                        isToday = date == LocalDate.now(),
                        onClick = { onDateSelected(date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekDayItem(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val dayNames = listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
    val dayName = dayNames[date.dayOfWeek.value % 7]
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6B7280),
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    when {
                        isSelected -> Color(0xFF8B5CF6)
                        isToday -> Color(0xFF8B5CF6).copy(alpha = 0.1f)
                        else -> Color.Transparent
                    },
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    isSelected -> Color.White
                    isToday -> Color(0xFF8B5CF6)
                    else -> Color(0xFF1F2937)
                }
            )
        }
    }
}

@Composable
private fun EventCard(
    event: ScheduleEvent,
    onClick: () -> Unit,
    onStatusChange: (EventStatus) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Time
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = event.time,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                
                // Status indicator
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(40.dp)
                        .background(
                            when (event.status) {
                                EventStatus.ONGOING -> Color(0xFF8B5CF6)
                                EventStatus.COMPLETED -> Color(0xFF10B981)
                                EventStatus.CANCELLED -> Color(0xFFEF4444)
                                else -> event.type.color
                            },
                            RoundedCornerShape(2.dp)
                        )
                )
            }
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Event type badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = event.type.color.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = event.type.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = event.type.color,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = event.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280)
                    )
                }
                
                // Status and action
                if (event.status == EventStatus.ONGOING) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF8B5CF6)
                        ) {
                            Text(
                                text = "Tham gia ngay",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Text(
                            text = "10:00 - 11:30",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }
            
            // More options
            IconButton(
                onClick = { /* Show options */ },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Tùy chọn",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyScheduleCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        Color(0xFF8B5CF6).copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.EventAvailable,
                    contentDescription = null,
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Thêm hoạt động",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Không có lịch trình nào cho ngày này",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun getWeekDates(selectedDate: LocalDate): List<LocalDate> {
    val startOfWeek = selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() % 7)
    return (0..6).map { startOfWeek.plusDays(it.toLong()) }
}