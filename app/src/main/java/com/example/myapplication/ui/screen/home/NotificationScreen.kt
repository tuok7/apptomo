package com.example.myapplication.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tất cả", "Chưa đọc")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Text(
                            "Thông báo",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    actions = {
                        TextButton(onClick = { /* TODO: Đọc tất cả */ }) {
                            Text(
                                "Đọc tất cả",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF2196F3)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFE3F2FD),
                        titleContentColor = Color(0xFF1976D2)
                    )
                )
                
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFFE3F2FD),
                    contentColor = Color(0xFF2196F3)
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Text(
                                    title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                ) 
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section: HÔM NAY
                item {
                    Text(
                        text = "HÔM NAY",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                // Notification items will be added here by user
                // Example structure:
                item {
                    NotificationCard(
                        icon = Icons.Default.Assignment,
                        iconColor = Color(0xFF2196F3),
                        iconBackground = Color(0xFFE3F2FD),
                        title = "Bạn có một task mới được giao",
                        message = "Thiết kế giao diện cho module quản lý thông báo của ứng dụng nhóm.",
                        time = "2 phút trước",
                        isRead = false,
                        showBadge = true
                    )
                }
                
                item {
                    NotificationCard(
                        icon = Icons.Default.Warning,
                        iconColor = Color(0xFFF44336),
                        iconBackground = Color(0xFFFFEBEE),
                        title = "Deadline môn Triết học còn 2 tiếng",
                        message = "Nhắc nhở: Nộp bài tiểu luận cuối kỳ trước 12:00 PM hôm nay.",
                        time = "Khẩn cấp",
                        isRead = false,
                        isUrgent = true
                    )
                }
                
                item {
                    NotificationCard(
                        icon = Icons.Default.Person,
                        iconColor = Color(0xFFFF9800),
                        iconBackground = Color(0xFFFFF3E0),
                        title = "Nguyễn Văn A vừa gửi tài liệu mới",
                        message = "Tài liệu tham khảo chương 3.pdf",
                        time = "45 phút trước",
                        isRead = true
                    )
                }
                
                // Section: HÔM QUA
                item {
                    Text(
                        text = "HÔM QUA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                
                item {
                    NotificationCard(
                        icon = Icons.Default.Group,
                        iconColor = Color(0xFF9C27B0),
                        iconBackground = Color(0xFFF3E5F5),
                        title = "Họp nhóm Dự án 1",
                        message = "Buổi họp đã kết thúc. Xem lại biên bản cuộc họp trong mục tài liệu.",
                        time = "1 ngày trước",
                        isRead = true
                    )
                }
                
                item {
                    NotificationCard(
                        icon = Icons.Default.Person,
                        iconColor = Color(0xFFFF9800),
                        iconBackground = Color(0xFFFFF3E0),
                        title = "Lê Thị B đã phản hồi bình luận của bạn",
                        message = "\"Ý tưởng này rất hay, mình sẽ cập nhật...",
                        time = "1 ngày trước",
                        isRead = true
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCard(
    icon: ImageVector,
    iconColor: Color,
    iconBackground: Color,
    title: String,
    message: String,
    time: String,
    isRead: Boolean = false,
    showBadge: Boolean = false,
    isUrgent: Boolean = false
) {
    Card(
        onClick = { /* TODO: Handle click */ },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUrgent) Color(0xFFFFEBEE) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isRead) FontWeight.Normal else FontWeight.Bold,
                        color = if (isUrgent) Color(0xFFF44336) else Color.Black,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (showBadge) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF2196F3), CircleShape)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (isUrgent) Color(0xFFF44336) else Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isUrgent) Color(0xFFF44336) else Color.Gray
                    )
                }
            }
        }
    }
}
