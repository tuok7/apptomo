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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
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
    var showGroupInfo by remember { mutableStateOf(false) }
    var showBackgroundPicker by remember { mutableStateOf(false) }
    var selectedBackground by remember { mutableIntStateOf(0) }
    var hasError by remember { mutableStateOf(false) }
    
    LaunchedEffect(groupId) {
        try {
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
    
    if (showGroupInfo) {
        GroupInfoScreen(
            groupName = selectedGroup?.name ?: "",
            members = members,
            assignments = assignments,
            isLoading = uiState.isLoading,
            onBackClick = { showGroupInfo = false },
            onAddMemberClick = onAddMemberClick,
            onAddAssignmentClick = onAddAssignmentClick,
            onAssignmentClick = onAssignmentClick
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            Text(
                                text = selectedGroup?.name ?: "Nhóm chat",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${members.size} thành viên",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showBackgroundPicker = true }) {
                            Icon(Icons.Default.Palette, contentDescription = "Đổi nền")
                        }
                        IconButton(onClick = { showGroupInfo = true }) {
                            Icon(Icons.Default.Info, contentDescription = "Thông tin nhóm")
                        }
                    }
                )
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
                Box(modifier = Modifier.padding(padding)) {
                    GroupChatContent(
                        groupName = selectedGroup?.name ?: "",
                        backgroundIndex = selectedBackground
                    )
                }
            }
            
            // Background picker dialog
            if (showBackgroundPicker) {
                BackgroundPickerDialog(
                    currentBackground = selectedBackground,
                    onBackgroundSelected = { 
                        selectedBackground = it
                        showBackgroundPicker = false
                    },
                    onDismiss = { showBackgroundPicker = false }
                )
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
fun GroupChatContent(groupName: String, backgroundIndex: Int = 0) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userPreferences = remember { com.example.myapplication.data.preferences.UserPreferences(context) }
    val currentUserName = remember { userPreferences.getFullName().ifEmpty { "Bạn" } }
    
    var messageText by remember { mutableStateOf("") }
    var messages by remember { 
        mutableStateOf(listOf<MessageItem>())
    }
    
    val backgroundColor = when (backgroundIndex) {
        0 -> Color(0xFFF5F5F5) // Xám nhạt (mặc định)
        1 -> Color(0xFFE3F2FD) // Xanh dương nhạt
        2 -> Color(0xFFFCE4EC) // Hồng nhạt
        3 -> Color(0xFFF1F8E9) // Xanh lá nhạt
        4 -> Color(0xFFFFF3E0) // Cam nhạt
        5 -> Color(0xFFE8EAF6) // Tím nhạt
        6 -> Color(0xFFE0F2F1) // Xanh ngọc nhạt
        7 -> Color(0xFFFFF9C4) // Vàng nhạt
        else -> Color(0xFFF5F5F5)
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Messages list
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(backgroundColor)
        ) {
            if (messages.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Chưa có tin nhắn nào",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hãy bắt đầu cuộc trò chuyện với nhóm",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        MessageBubble(message)
                    }
                }
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
                                id = System.currentTimeMillis(),
                                senderName = currentUserName,
                                content = messageText.trim(),
                                time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                                isCurrentUser = true
                            )
                            messages = messages + newMessage
                            messageText = ""
                        }
                    },
                    enabled = messageText.isNotBlank(),
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (messageText.isNotBlank()) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (messageText.isNotBlank()) 
                            Color.White 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Gửi"
                    )
                }
            }
        }
    }
}

@Composable
fun BackgroundPickerDialog(
    currentBackground: Int,
    onBackgroundSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val backgrounds = listOf(
        0 to "Xám nhạt" to Color(0xFFF5F5F5),
        1 to "Xanh dương" to Color(0xFFE3F2FD),
        2 to "Hồng" to Color(0xFFFCE4EC),
        3 to "Xanh lá" to Color(0xFFF1F8E9),
        4 to "Cam" to Color(0xFFFFF3E0),
        5 to "Tím" to Color(0xFFE8EAF6),
        6 to "Xanh ngọc" to Color(0xFFE0F2F1),
        7 to "Vàng" to Color(0xFFFFF9C4)
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Chọn nền chat",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(backgrounds) { (indexAndName, color) ->
                    val (index, name) = indexAndName
                    Surface(
                        onClick = { onBackgroundSelected(index) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = color,
                        border = if (currentBackground == index) {
                            androidx.compose.foundation.BorderStroke(
                                3.dp,
                                MaterialTheme.colorScheme.primary
                            )
                        } else {
                            androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color.Gray.copy(alpha = 0.3f)
                            )
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (currentBackground == index) {
                                    androidx.compose.ui.text.font.FontWeight.Bold
                                } else {
                                    androidx.compose.ui.text.font.FontWeight.Normal
                                }
                            )
                            if (currentBackground == index) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Đã chọn",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupInfoScreen(
    groupName: String,
    members: List<MemberData>,
    assignments: List<AssignmentData>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onAddMemberClick: () -> Unit,
    onAddAssignmentClick: () -> Unit,
    onAssignmentClick: (Long) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var showQRDialog by remember { mutableStateOf(false) }
    var showPendingRequests by remember { mutableStateOf(false) }
    
    // Mock pending requests
    val pendingRequests = remember {
        mutableStateListOf(
            PendingRequest(1, "Nguyễn Văn A", "nguyenvana@gmail.com"),
            PendingRequest(2, "Trần Thị B", "tranthib@gmail.com")
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông tin nhóm") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    // Pending requests badge
                    if (pendingRequests.isNotEmpty()) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text("${pendingRequests.size}")
                                }
                            }
                        ) {
                            IconButton(onClick = { showPendingRequests = true }) {
                                Icon(Icons.Default.PersonAdd, contentDescription = "Yêu cầu chờ duyệt")
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) onAddMemberClick()
                    else onAddAssignmentClick()
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Group header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
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
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = groupName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Text(
                        text = "${members.size} thành viên",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Invite buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showInviteDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Link,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Link mời")
                        }
                        
                        OutlinedButton(
                            onClick = { showQRDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.QrCode,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("QR Code")
                        }
                    }
                }
            }
            
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
            }
            
            when (selectedTab) {
                0 -> MembersList(members, isLoading)
                1 -> AssignmentsList(assignments, isLoading, onAssignmentClick)
            }
        }
        
        // Invite link dialog
        if (showInviteDialog) {
            InviteLinkDialog(
                groupName = groupName,
                onDismiss = { showInviteDialog = false }
            )
        }
        
        // QR code dialog
        if (showQRDialog) {
            QRCodeDialog(
                groupName = groupName,
                onDismiss = { showQRDialog = false }
            )
        }
        
        // Pending requests dialog
        if (showPendingRequests) {
            PendingRequestsDialog(
                requests = pendingRequests,
                onApprove = { request ->
                    pendingRequests.remove(request)
                    // TODO: Add to group members
                },
                onReject = { request ->
                    pendingRequests.remove(request)
                },
                onDismiss = { showPendingRequests = false }
            )
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

data class PendingRequest(
    val id: Long,
    val name: String,
    val email: String
)

@Composable
fun InviteLinkDialog(
    groupName: String,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val inviteLink = "https://myapp.com/join/group/${groupName.hashCode()}"
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Link,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Link mời nhóm",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Chia sẻ link này để mời người khác vào nhóm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = inviteLink,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                            maxLines = 2
                        )
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("invite_link", inviteLink)
                                clipboard.setPrimaryClip(clip)
                                android.widget.Toast.makeText(context, "Đã sao chép link", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Sao chép"
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "⚠️ Người tham gia cần được trưởng nhóm duyệt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val shareIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, "Tham gia nhóm $groupName: $inviteLink")
                        type = "text/plain"
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Chia sẻ link"))
                }
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chia sẻ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
fun QRCodeDialog(
    groupName: String,
    onDismiss: () -> Unit
) {
    val inviteCode = groupName.hashCode().toString()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.QrCode,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "QR Code nhóm",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Quét mã QR để tham gia nhóm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // QR Code placeholder
                Surface(
                    modifier = Modifier.size(200.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.QrCode,
                                contentDescription = null,
                                modifier = Modifier.size(120.dp),
                                tint = Color.Black
                            )
                            Text(
                                text = "Mã: $inviteCode",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Black
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "⚠️ Người tham gia cần được trưởng nhóm duyệt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
fun PendingRequestsDialog(
    requests: List<PendingRequest>,
    onApprove: (PendingRequest) -> Unit,
    onReject: (PendingRequest) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Yêu cầu tham gia (${requests.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        },
        text = {
            if (requests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không có yêu cầu nào",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(requests, key = { it.id }) { request ->
                        PendingRequestCard(
                            request = request,
                            onApprove = { onApprove(request) },
                            onReject = { onReject(request) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
fun PendingRequestCard(
    request: PendingRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text(
                    text = request.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onApprove,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Duyệt",
                        tint = Color.White
                    )
                }
                
                IconButton(
                    onClick = onReject,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Từ chối",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
