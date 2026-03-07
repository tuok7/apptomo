package com.example.myapplication.ui.screen.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

data class TaskData(val title: String, val deadline: String, val priority: String, val status: String, val progress: Int)
data class ActivityData(val action: String, val target: String, val time: String, val icon: ImageVector, val color: Color)

@Composable
fun OverviewTab(
    paddingValues: PaddingValues,
    userPreferences: com.example.myapplication.data.preferences.UserPreferences,
    navController: androidx.navigation.NavHostController? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = remember { com.example.myapplication.data.database.AppDatabase.getDatabase(context) }
    val userName = remember { userPreferences.getFullName().ifEmpty { "Alex" } }
    
    // Lấy dữ liệu thật từ database
    val groups by database.groupDao().getAllGroups().collectAsState(initial = emptyList())
    val allAssignments by database.assignmentDao().getAllAssignments().collectAsState(initial = emptyList())
    
    // Tính toán thống kê
    val pendingAssignments = allAssignments.count { 
        it.status == com.example.myapplication.data.model.AssignmentStatus.TODO ||
        it.status == com.example.myapplication.data.model.AssignmentStatus.IN_PROGRESS
    }
    val completedAssignments = allAssignments.count { 
        it.status == com.example.myapplication.data.model.AssignmentStatus.COMPLETED 
    }
    
    // Lấy nhiệm vụ hôm nay
    val todayTasks = allAssignments
        .filter { assignment ->
            // Lọc các task có deadline hôm nay hoặc đang trong tiến trình
            assignment.status != com.example.myapplication.data.model.AssignmentStatus.COMPLETED
        }
        .sortedBy { it.dueDate }
        .take(3)
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Color(0xFFF8FAFC)),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Welcome section
        item { 
            WelcomeSection(userName)
        }
        
        // Stats overview theo mẫu
        item { 
            StatsOverviewSection(pendingAssignments, completedAssignments)
        }
        
        // Weekly progress chart
        item {
            WeeklyProgressChart()
        }
        
        // Today's schedule
        item {
            TodayScheduleSection(todayTasks, navController)
        }
    }
}

@Composable
private fun WelcomeSection(userName: String) {
    Column {
        Text(
            "Chào mừng trở lại, $userName",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF6B7280)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Tổng quan của bạn",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF6366F1).copy(alpha = 0.1f)
            ) {
                Text(
                    "Học kỳ 2",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF6366F1),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun StatsOverviewSection(pendingCount: Int, completedCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Đang chờ card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Đang chờ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    String.format("%02d", pendingCount),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    "+ 2 Mới",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFEF4444)
                )
            }
        }
        
        // Đã hoàn thành card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Đã hoàn thành",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    String.format("%02d", completedCount),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    "+ 5 Hôm nay",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF10B981)
                )
            }
        }
    }
}

@Composable
private fun WeeklyProgressChart() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Tiến độ hàng tuần",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF8B5CF6).copy(alpha = 0.1f)
                ) {
                    Text(
                        "BÀI TẬP",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8B5CF6),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Chart bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val chartData = listOf(40, 30, 80, 25, 60, 45, 35) // Sample data
                val days = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                
                chartData.forEachIndexed { index, value ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height((value * 1.5f).dp)
                                .background(
                                    if (index == 2) Color(0xFF6366F1) else Color(0xFFE5E7EB),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            days[index],
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayScheduleSection(
    todayTasks: List<com.example.myapplication.data.model.Assignment>,
    navController: androidx.navigation.NavHostController?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Hạn chót hôm nay",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                TextButton(
                    onClick = { /* Navigate to full schedule */ }
                ) {
                    Text(
                        "Xem tất cả",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6366F1)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (todayTasks.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Không có nhiệm vụ nào hôm nay",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9CA3AF)
                    )
                }
            } else {
                // Sample schedule items theo mẫu
                ScheduleItem(
                    time = "14:00",
                    title = "Calculus Set 4",
                    subtitle = "Toán học nâng cao • Phòng 302",
                    color = Color(0xFFEF4444),
                    onClick = { }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ScheduleItem(
                    time = "16:30",
                    title = "Báo cáo thi nghiệm: Thấm thấu",
                    subtitle = "Sinh học 101 • Cộng nộp bài",
                    color = Color(0xFF8B5CF6),
                    onClick = { }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ScheduleItem(
                    time = "18:00",
                    title = "Từ vựng tiếng Tây Ban Nha",
                    subtitle = "Ngôn ngữ học • Ôn tập kiểm tra",
                    color = Color(0xFF6B7280),
                    onClick = { }
                )
            }
        }
    }
}

@Composable
private fun ScheduleItem(
    time: String,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Time and color indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                time,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F2937)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(color, CircleShape)
                )
            }
        }
        
        // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F2937)
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280)
            )
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