package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = description,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onActionClick,
                modifier = Modifier.height(48.dp)
            ) {
                Text(actionText)
            }
        }
    }
}

@Composable
fun EmptyGroupsState(onCreateGroup: () -> Unit) {
    EmptyState(
        icon = Icons.Default.Group,
        title = "Chưa có nhóm nào",
        description = "Tạo nhóm đầu tiên để bắt đầu làm việc cùng nhau",
        actionText = "Tạo nhóm mới",
        onActionClick = onCreateGroup
    )
}

@Composable
fun EmptyTasksState(onCreateTask: () -> Unit) {
    EmptyState(
        icon = Icons.Default.Assignment,
        title = "Chưa có nhiệm vụ",
        description = "Thêm nhiệm vụ mới để theo dõi công việc",
        actionText = "Thêm nhiệm vụ",
        onActionClick = onCreateTask
    )
}

@Composable
fun EmptyNotificationsState() {
    EmptyState(
        icon = Icons.Default.NotificationsNone,
        title = "Chưa có thông báo",
        description = "Bạn sẽ nhận được thông báo về hoạt động nhóm ở đây"
    )
}

@Composable
fun EmptyMessagesState() {
    EmptyState(
        icon = Icons.Default.ChatBubbleOutline,
        title = "Chưa có tin nhắn",
        description = "Bắt đầu cuộc trò chuyện với nhóm của bạn"
    )
}

@Composable
fun EmptySearchState() {
    EmptyState(
        icon = Icons.Default.SearchOff,
        title = "Không tìm thấy kết quả",
        description = "Thử tìm kiếm với từ khóa khác"
    )
}

@Composable
fun NoInternetState(onRetry: () -> Unit) {
    EmptyState(
        icon = Icons.Default.CloudOff,
        title = "Không có kết nối",
        description = "Vui lòng kiểm tra kết nối internet và thử lại",
        actionText = "Thử lại",
        onActionClick = onRetry
    )
}
