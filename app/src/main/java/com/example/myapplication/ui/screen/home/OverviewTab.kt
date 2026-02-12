package com.example.myapplication.ui.screen.home

import androidx.compose.foundation.background
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
    val userName = remember { userPreferences.getFullName().ifEmpty { "Người dùng" } }
    
    // Lấy dữ liệu thật từ database
    val groups by database.groupDao().getAllGroups().collectAsState(initial = emptyList())
    val allAssignments by database.assignmentDao().getAllAssignments().collectAsState(initial = emptyList())
    
    // Tính toán thống kê
    val totalGroups = groups.size
    val totalAssignments = allAssignments.size
    val completedAssignments = allAssignments.count { 
        it.status == com.example.myapplication.data.model.AssignmentStatus.COMPLETED 
    }
    val inProgressAssignments = allAssignments.count { 
        it.status == com.example.myapplication.data.model.AssignmentStatus.IN_PROGRESS 
    }
    
    // Lấy nhiệm vụ ưu tiên cao
    val highPriorityTasks = allAssignments
        .filter { it.priority == com.example.myapplication.data.model.Priority.HIGH }
        .sortedBy { it.dueDate }
        .take(3)
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues)
            .background(Brush.verticalGradient(listOf(Color(0xFFE3F2FD), Color(0xFFF5F5F5)))),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { WelcomeCard(userName, totalAssignments, completedAssignments) }
        item { QuickActionsRow(navController) }
        item { Text("Tổng quan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        item { 
            StatsGrid(
                totalGroups = totalGroups,
                totalAssignments = totalAssignments,
                completedAssignments = completedAssignments,
                inProgressAssignments = inProgressAssignments
            )
        }
        
        if (highPriorityTasks.isNotEmpty()) {
            item { 
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Nhiệm vụ ưu tiên cao", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = {}) {
                        Text("Xem tất cả")
                        Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
                    }
                }
            }
            items(highPriorityTasks.size) { index ->
                val assignment = highPriorityTasks[index]
                val taskData = TaskData(
                    title = assignment.title,
                    deadline = assignment.dueDate.toString(),
                    priority = when (assignment.priority) {
                        com.example.myapplication.data.model.Priority.HIGH -> "Cao"
                        com.example.myapplication.data.model.Priority.MEDIUM -> "Trung bình"
                        com.example.myapplication.data.model.Priority.LOW -> "Thấp"
                    },
                    status = when (assignment.status) {
                        com.example.myapplication.data.model.AssignmentStatus.TODO -> "Chưa bắt đầu"
                        com.example.myapplication.data.model.AssignmentStatus.IN_PROGRESS -> "Đang làm"
                        com.example.myapplication.data.model.AssignmentStatus.COMPLETED -> "Hoàn thành"
                    },
                    progress = when (assignment.status) {
                        com.example.myapplication.data.model.AssignmentStatus.TODO -> 0
                        com.example.myapplication.data.model.AssignmentStatus.IN_PROGRESS -> 50
                        com.example.myapplication.data.model.AssignmentStatus.COMPLETED -> 100
                    }
                )
                EnhancedTaskCard(taskData) {
                    navController?.navigate(
                        com.example.myapplication.ui.navigation.Screen.AssignmentDetail.createRoute(assignment.id)
                    )
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Assignment,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Chưa có nhiệm vụ nào",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Text(
                            "Tạo nhiệm vụ mới để bắt đầu",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun WelcomeCard(userName: String, totalTasks: Int, completedTasks: Int) {
    val completionRate = if (totalTasks > 0) (completedTasks * 100 / totalTasks) else 0
    
    // Lấy thời gian hiện tại để hiển thị lời chào phù hợp
    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val greeting = when (currentHour) {
        in 0..11 -> "Chào buổi sáng! ☀️"
        in 12..17 -> "Chào buổi chiều! 👋"
        else -> "Chào buổi tối! 🌙"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF667eea), Color(0xFF764ba2))))
                .padding(24.dp)
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(greeting, style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(userName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Box(
                        modifier = Modifier.size(56.dp).background(Color.White.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickStatChip(Icons.Default.Assignment, totalTasks.toString(), "Nhiệm vụ hôm nay", Modifier.weight(1f))
                    QuickStatChip(Icons.Default.TrendingUp, "$completionRate%", "Hoàn thành", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(navController: androidx.navigation.NavHostController? = null) {
    Column {
        Text("Thao tác nhanh", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            item { 
                QuickActionCard(Icons.Default.Add, "Tạo nhóm", Color(0xFF4CAF50)) {
                    navController?.navigate(com.example.myapplication.ui.navigation.Screen.AddGroup.route)
                }
            }
            item { 
                QuickActionCard(Icons.Default.Login, "Tham gia nhóm", Color(0xFF2196F3)) {
                    navController?.navigate(com.example.myapplication.ui.navigation.Screen.JoinGroup.route)
                }
            }
            item { 
                QuickActionCard(Icons.Default.Assignment, "Thêm nhiệm vụ", Color(0xFFFF9800)) {
                    // TODO: Show group selection dialog then navigate to add assignment
                }
            }
            item { 
                QuickActionCard(Icons.Default.Event, "Lịch họp", Color(0xFF9C27B0)) {
                    // TODO: Navigate to calendar screen
                }
            }
        }
    }
}

@Composable
private fun StatsGrid(
    totalGroups: Int,
    totalAssignments: Int,
    completedAssignments: Int,
    inProgressAssignments: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AnimatedStatCard(Modifier.weight(1f), totalGroups, "Nhóm", Color(0xFF4CAF50), Icons.Default.Group)
            AnimatedStatCard(Modifier.weight(1f), totalAssignments, "Nhiệm vụ", Color(0xFF2196F3), Icons.Default.Assignment)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AnimatedStatCard(Modifier.weight(1f), completedAssignments, "Hoàn thành", Color(0xFFFF9800), Icons.Default.CheckCircle)
            AnimatedStatCard(Modifier.weight(1f), inProgressAssignments, "Đang làm", Color(0xFF9C27B0), Icons.Default.Schedule)
        }
    }
}

@Composable
fun AnimatedStatCard(modifier: Modifier, number: Int, label: String, color: Color, icon: ImageVector) {
    var animatedNumber by remember { mutableIntStateOf(0) }
    LaunchedEffect(number) {
        for (i in 0..number) {
            animatedNumber = i
            delay(30)
        }
    }
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(64.dp)
                    .background(Brush.radialGradient(listOf(color.copy(alpha = 0.2f), color.copy(alpha = 0.05f))), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(animatedNumber.toString(), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}
