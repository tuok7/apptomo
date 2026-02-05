package com.example.myapplication.ui.screen.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.api.AssignmentData
import com.example.myapplication.data.api.MemberData
import com.example.myapplication.ui.viewmodel.GroupViewModel
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
    var hasError by remember { mutableStateOf(false) }
    
    LaunchedEffect(groupId) {
        try {
            // Find and select the group from the groups list
            val group = viewModel.groups.value.find { it.id == groupId }
            if (group != null) {
                viewModel.selectGroup(group)
                hasError = false
            } else {
                android.util.Log.e("GroupDetailScreen", "Group not found: $groupId")
                hasError = true
            }
        } catch (e: Exception) {
            android.util.Log.e("GroupDetailScreen", "Error loading group: ${e.message}", e)
            hasError = true
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedGroup?.name ?: "Chi tiết nhóm") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedGroup != null && !hasError) {
                if (selectedTab != 2) {
                    FloatingActionButton(
                        onClick = {
                            try {
                                if (selectedTab == 0) onAddMemberClick()
                                else onAddAssignmentClick()
                            } catch (e: Exception) {
                                android.util.Log.e("GroupDetailScreen", "FAB error: ${e.message}", e)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Thêm")
                    }
                }
            }
        }
    ) { padding ->
        if (hasError || selectedGroup == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator()
                        Text("Đang tải...")
                    } else {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            "Không thể tải thông tin nhóm",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Vui lòng thử lại sau",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = onBackClick) {
                            Text("Quay lại")
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Thành viên (${members.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Bài tập (${assignments.size})") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Nhắn tin") }
                    )
                }
                
                when (selectedTab) {
                    0 -> MembersList(members, uiState.isLoading)
                    1 -> AssignmentsList(assignments, uiState.isLoading, onAssignmentClick)
                    2 -> MessagesTab(selectedGroup?.name ?: "")
                }
            }
        }
    }
}

@Composable
fun MembersList(members: List<MemberData>, isLoading: Boolean) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (members.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Chưa có thành viên nào")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(members, key = { it.id }) { member ->
                MemberCard(member)
            }
        }
    }
}

@Composable
fun MemberCard(member: MemberData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = member.fullName, style = MaterialTheme.typography.titleMedium)
                Text(text = member.email, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = member.role,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun AssignmentsList(
    assignments: List<AssignmentData>,
    isLoading: Boolean,
    onAssignmentClick: (Long) -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (assignments.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Chưa có bài tập nào")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(assignments, key = { it.id }) { assignment ->
                AssignmentCard(
                    assignment = assignment,
                    onClick = { onAssignmentClick(assignment.id) }
                )
            }
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = assignment.title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Hạn: ${assignment.dueDate ?: "Không có"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = { },
                        label = { Text(getStatusText(assignment.status)) }
                    )
                    AssistChip(
                        onClick = { },
                        label = { Text(getPriorityText(assignment.priority)) }
                    )
                }
            }
        }
    }
}

fun getStatusText(status: String): String = when (status.lowercase()) {
    "todo" -> "Chưa làm"
    "in_progress" -> "Đang làm"
    "completed" -> "Hoàn thành"
    else -> status
}

fun getPriorityText(priority: String): String = when (priority.lowercase()) {
    "low" -> "Thấp"
    "medium" -> "Trung bình"
    "high" -> "Cao"
    else -> priority
}

@Composable
fun MessagesTab(groupName: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userPreferences = remember { com.example.myapplication.data.preferences.UserPreferences(context) }
    val currentUserName = remember { userPreferences.getFullName().ifEmpty { "Bạn" } }
    
    var messageText by remember { mutableStateOf("") }
    var messages by remember { 
        mutableStateOf(listOf(
            MessageItem(1, "van quyen", "Chào mọi người!", "11:20", false),
            MessageItem(2, currentUserName, "Xin chào!", "11:21", true),
            MessageItem(3, "van quyen", "Hôm nay chúng ta làm bài tập gì?", "11:22", false),
            MessageItem(4, currentUserName, "Làm bài tập về Android nhé", "11:23", true)
        ))
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageBubble(message)
            }
        }
        
        // Message input
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nhập tin nhắn...") },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3
                )
                
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            val newMessage = MessageItem(
                                id = messages.size + 1L,
                                senderName = currentUserName,
                                content = messageText,
                                time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                                isCurrentUser = true
                            )
                            messages = messages + newMessage
                            messageText = ""
                        }
                    },
                    enabled = messageText.isNotBlank(),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (messageText.isNotBlank()) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Gửi",
                        tint = if (messageText.isNotBlank()) 
                            Color.White 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: MessageItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isCurrentUser) {
            // Avatar for other users
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF4A90E2), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (message.isCurrentUser) Alignment.End else Alignment.Start
        ) {
            if (!message.isCurrentUser) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
            }
            
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (message.isCurrentUser) 16.dp else 4.dp,
                    topEnd = if (message.isCurrentUser) 4.dp else 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                color = if (message.isCurrentUser) 
                    Color(0xFF4A90E2) 
                else 
                    Color.White,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (message.isCurrentUser) Color.White else Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.time,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (message.isCurrentUser) 
                            Color.White.copy(alpha = 0.8f) 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        if (message.isCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // Avatar for current user
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF4CAF50), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

data class MessageItem(
    val id: Long,
    val senderName: String,
    val content: String,
    val time: String,
    val isCurrentUser: Boolean
)
