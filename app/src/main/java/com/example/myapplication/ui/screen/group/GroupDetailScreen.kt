package com.example.myapplication.ui.screen.group

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.api.AssignmentData
import com.example.myapplication.data.api.MemberData
import com.example.myapplication.ui.viewmodel.GroupViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    viewModel: GroupViewModel,
    groupId: Long,
    onBackClick: () -> Unit,
    onAddMemberClick: () -> Unit,
    onAddAssignmentClick: () -> Unit,
    onAssignmentClick: (Long) -> Unit
) {
    val selectedGroup by viewModel.selectedGroup.collectAsState()
    val members by viewModel.members.collectAsState()
    val assignments by viewModel.assignments.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(groupId) {
        try {
            val group = viewModel.groups.value.find { it.id == groupId }
            if (group != null) {
                viewModel.selectGroup(group)
            }
        } catch (e: Exception) {
            android.util.Log.e("GroupDetailScreen", "Error loading group: ${e.message}", e)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết nhóm") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Group Header
            item {
                GroupHeader(
                    groupName = selectedGroup?.name ?: "Nhóm học tập",
                    memberCount = members.size,
                    members = members
                )
            }
            
            // Action Buttons
            item {
                ActionButtons()
            }
            
            // Tab Navigation
            item {
                TabNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    assignmentCount = assignments.size
                )
            }
            
            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Bài tập Tab
                    items(assignments, key = { it.id }) { assignment ->
                        AssignmentCard(
                            assignment = assignment,
                            onClick = { onAssignmentClick(assignment.id) }
                        )
                    }
                    if (assignments.isEmpty()) {
                        item {
                            EmptyAssignmentsCard(onAddClick = onAddAssignmentClick)
                        }
                    }
                }
                1 -> {
                    // Tài liệu Tab
                    item {
                        DocumentsSection()
                    }
                }
                2 -> {
                    // Thảo luận Tab
                    item {
                        DiscussionSection()
                    }
                }
            }
        }
    }
}

@Composable
fun GroupHeader(
    groupName: String,
    memberCount: Int,
    members: List<MemberData>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF8B5CF6)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Group Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Group Name
            Text(
                text = groupName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = "$memberCount thành viên",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Member Avatars
            if (members.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy((-8).dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(members.take(6)) { member ->
                        MemberAvatar(member = member)
                    }
                    if (members.size > 6) {
                        item {
                            MoreMembersIndicator(count = members.size - 6)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemberAvatar(member: MemberData) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                ),
                shape = CircleShape
            )
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = member.fullName.take(1).uppercase(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun MoreMembersIndicator(count: Int) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                Color.White.copy(alpha = 0.2f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+$count",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun ActionButtons() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { /* Navigate to chat */ },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8B5CF6)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.Chat,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Nói bạn")
        }
        
        OutlinedButton(
            onClick = { /* Open management */ },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6))
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color(0xFF8B5CF6)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Quản lý",
                color = Color(0xFF8B5CF6)
            )
        }
    }
}

@Composable
fun TabNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    assignmentCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            TabItem(
                text = "Bài tập",
                count = assignmentCount,
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )
            TabItem(
                text = "Tài liệu",
                count = 3, // Mock count
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
            TabItem(
                text = "Thảo luận",
                count = 12, // Mock count
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TabItem(
    text: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFF8B5CF6) else Color.Transparent
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun AssignmentCard(
    assignment: AssignmentData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = assignment.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hạn: ${assignment.dueDate ?: "Không có"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = getStatusColor(assignment.status).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = getStatusText(assignment.status),
                        style = MaterialTheme.typography.labelSmall,
                        color = getStatusColor(assignment.status),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress Bar
            val progress = when (assignment.status.lowercase()) {
                "completed" -> 1.0f
                "in_progress" -> 0.6f
                else -> 0.0f
            }
            
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tiến độ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B5CF6)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF8B5CF6),
                    trackColor = Color(0xFF8B5CF6).copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun EmptyAssignmentsCard(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAddClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF8B5CF6).copy(alpha = 0.05f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFF8B5CF6).copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Assignment,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF8B5CF6).copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Chưa có bài tập nào",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B5CF6)
            )
            Text(
                text = "Nhấn để thêm bài tập mới",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DocumentsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Tài liệu nhóm",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Mock documents
            val documents = listOf(
                "Bài giảng Chương 1.pdf" to Icons.Default.PictureAsPdf,
                "Slide thuyết trình.pptx" to Icons.Default.Slideshow,
                "Tài liệu tham khảo.docx" to Icons.Default.Description
            )
            
            documents.forEach { (fileName, icon) ->
                DocumentItem(fileName = fileName, icon = icon)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Add document button
            Surface(
                onClick = { /* Add document */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF8B5CF6).copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Thêm tài liệu",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8B5CF6)
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentItem(
    fileName: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Open document */ }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color(0xFF8B5CF6)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Đã tải lên 2 ngày trước",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Default.Download,
            contentDescription = "Tải xuống",
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DiscussionSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Thảo luận gần đây",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { /* View all */ }) {
                    Text(
                        text = "Xem tất cả",
                        color = Color(0xFF8B5CF6)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Mock messages
            val messages = listOf(
                "Nguyễn Văn A" to "Mọi người đã làm bài tập chưa?",
                "Trần Thị B" to "Mình đã hoàn thành rồi, khó quá!",
                "Lê Văn C" to "Có ai giải thích câu 3 được không?"
            )
            
            messages.forEach { (sender, message) ->
                DiscussionItem(sender = sender, message = message)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Join discussion button
            Surface(
                onClick = { /* Join discussion */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF8B5CF6)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Chat,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tham gia thảo luận",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun DiscussionItem(sender: String, message: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = sender.take(1),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = sender,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun getStatusText(status: String): String = when (status.lowercase()) {
    "todo" -> "Chưa làm"
    "in_progress" -> "Đang làm"
    "completed" -> "Hoàn thành"
    else -> status
}

fun getStatusColor(status: String): Color = when (status.lowercase()) {
    "todo" -> Color(0xFFFF9800)
    "in_progress" -> Color(0xFF2196F3)
    "completed" -> Color(0xFF4CAF50)
    else -> Color(0xFF757575)
}

// Legacy functions for compatibility - these can be removed later
@Composable
fun MembersList(members: List<MemberData>, isLoading: Boolean) {
    // Empty implementation for compatibility
}

@Composable
fun AssignmentsList(
    assignments: List<AssignmentData>,
    isLoading: Boolean,
    onAssignmentClick: (Long) -> Unit
) {
    // Empty implementation for compatibility
}