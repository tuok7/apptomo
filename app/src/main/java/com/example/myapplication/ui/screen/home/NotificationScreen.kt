package com.example.myapplication.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.*
import kotlinx.coroutines.delay

data class Notification(
    val id: Int,
    val title: String,
    val message: String,
    val time: String,
    val icon: ImageVector,
    val color: Color,
    val isRead: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen() {
    var isLoading by remember { mutableStateOf(true) }
    
    val notifications = listOf(
        Notification(
            1,
            "Nhiệm vụ mới",
            "Bạn được giao nhiệm vụ 'Hoàn thành báo cáo'",
            "5 phút trước",
            Icons.Default.Assignment,
            Color(0xFF2196F3),
            false
        ),
        Notification(
            2,
            "Thành viên mới",
            "Nguyễn Văn A đã tham gia nhóm 'Dự án X'",
            "1 giờ trước",
            Icons.Default.PersonAdd,
            Color(0xFF4CAF50),
            false
        ),
        Notification(
            3,
            "Hoàn thành nhiệm vụ",
            "Trần Thị B đã hoàn thành nhiệm vụ 'Thiết kế UI'",
            "2 giờ trước",
            Icons.Default.CheckCircle,
            Color(0xFFFF9800),
            true
        ),
        Notification(
            4,
            "Nhắc nhở",
            "Nhiệm vụ 'Review code' sắp đến hạn",
            "3 giờ trước",
            Icons.Default.Schedule,
            Color(0xFFF44336),
            true
        ),
        Notification(
            5,
            "Bình luận mới",
            "Lê Văn C đã bình luận trong nhiệm vụ của bạn",
            "1 ngày trước",
            Icons.Default.Comment,
            Color(0xFF9C27B0),
            true
        )
    )
    
    // Simulate loading
    LaunchedEffect(Unit) {
        delay(1200)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Thông báo",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(onClick = { /* TODO: Đánh dấu tất cả đã đọc */ }) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Đánh dấu tất cả đã đọc"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(6) {
                        SkeletonGroupItem()
                    }
                }
            }
            notifications.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    EmptyNotificationsState()
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notifications) { notification ->
                        NotificationItem(notification)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationItem(notification: Notification) {
    Card(
        onClick = { /* TODO: Xử lý click */ },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = notification.icon,
                    contentDescription = null,
                    tint = notification.color,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontSize = 16.sp,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.time,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {}
                }
            }
        }
    }
}
