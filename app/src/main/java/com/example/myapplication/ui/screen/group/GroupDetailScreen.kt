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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.api.AssignmentData
import com.example.myapplication.data.api.MemberData
import com.example.myapplication.data.model.GroupMessage
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.ui.viewmodel.GroupViewModel
import com.example.myapplication.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    viewModel: GroupViewModel,
    chatViewModel: ChatViewModel,
    groupId: Long,
    onBackClick: () -> Unit,
    onAddMemberClick: () -> Unit,
    onAddAssignmentClick: () -> Unit,
    onAssignmentClick: (Long) -> Unit,
    onChatClick: () -> Unit = {}
) {
    val selectedGroup by viewModel.selectedGroup.collectAsState()
    val members by viewModel.members.collectAsState()
    val assignments by viewModel.assignments.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    // Chat states
    val messages by chatViewModel.messages.collectAsState()
    val isLoadingMessages by chatViewModel.isLoading.collectAsState()
    val isUploading by chatViewModel.isUploading.collectAsState()
    val uploadProgress by chatViewModel.uploadProgress.collectAsState()
    val currentUser by chatViewModel.currentUser.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    var messageText by remember { mutableStateOf("") }
    var replyToMessage by remember { mutableStateOf<GroupMessage?>(null) }
    
    // Search states for chat tab
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(groupId) {
        try {
            val group = viewModel.groups.value.find { it.id == groupId }
            if (group != null) {
                viewModel.selectGroup(group)
                chatViewModel.loadMessages(groupId) // Load chat messages
            }
        } catch (e: Exception) {
            android.util.Log.e("GroupDetailScreen", "Error loading group: ${e.message}", e)
        }
    }
    
    // Auto scroll to bottom when new message arrives in chat tab
    LaunchedEffect(messages.size, selectedTab) {
        if (selectedTab == 2 && messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            // Show chat input only when in chat tab
            if (selectedTab == 2) {
                ChatInputBar(
                    messageText = messageText,
                    onMessageTextChange = { messageText = it },
                    onSendMessage = {
                        if (messageText.isNotBlank()) {
                            chatViewModel.sendMessage(
                                groupId = groupId,
                                content = messageText.trim(),
                                replyToMessageId = replyToMessage?.id
                            )
                            messageText = ""
                            replyToMessage = null
                        }
                    },
                    replyToMessage = replyToMessage,
                    onCancelReply = { replyToMessage = null }
                )
            }
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
                ActionButtons(
                    onChatClick = { selectedTab = 2 } // Switch to chat tab
                )
            }
            
            // Tab Navigation
            item {
                TabNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    assignmentCount = assignments.size,
                    messageCount = messages.size
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
                    // Chat Tab - Thảo luận trực tiếp với đầy đủ tính năng
                    
                    if (showSearchBar) {
                        item {
                            SearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                onSearch = { /* Handle search */ },
                                onClose = { 
                                    showSearchBar = false
                                    searchQuery = ""
                                }
                            )
                        }
                    }
                    
                    // Pinned messages
                    val pinnedMessages = messages.filter { it.isPinned }
                    if (pinnedMessages.isNotEmpty()) {
                        item {
                            PinnedMessagesSection(
                                pinnedMessages = pinnedMessages,
                                onMessageClick = { messageId ->
                                    // Scroll to message
                                    val index = messages.indexOfFirst { it.id == messageId }
                                    if (index >= 0) {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(index)
                                        }
                                    }
                                },
                                onUnpin = { messageId ->
                                    chatViewModel.pinMessage(messageId)
                                }
                            )
                        }
                    }
                    
                    if (isUploading) {
                        item {
                            UploadProgressBar(progress = uploadProgress)
                        }
                    }
                    
                    // Messages
                    items(messages, key = { it.id }) { message ->
                        AdvancedMessageBubble(
                            message = message,
                            isCurrentUser = message.senderId == currentUser?.id,
                            isOnline = true, // You can implement online status tracking
                            onReply = { replyToMessage = message },
                            onEdit = { /* Handle edit */ },
                            onReact = { emoji -> chatViewModel.reactToMessage(message.id, emoji) },
                            onDelete = { chatViewModel.deleteMessage(message.id) },
                            onPin = { chatViewModel.pinMessage(message.id) },
                            onFileClick = { /* Handle file click */ },
                            onAssignmentClick = { /* Handle assignment click */ }
                        )
                    }
                    
                    if (messages.isEmpty() && !isLoadingMessages) {
                        item {
                            EmptyChatCard()
                        }
                    }
                    
                    if (isLoadingMessages) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF8B5CF6))
                            }
                        }
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
fun ActionButtons(onChatClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onChatClick,
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
            Text("Chat nhóm")
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
    assignmentCount: Int,
    messageCount: Int = 0
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
                text = "Chat",
                count = messageCount,
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

// New Chat Components
@Composable
fun ChatInputBar(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    replyToMessage: GroupMessage?,
    onCancelReply: () -> Unit
) {
    var showAttachmentOptions by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    
    Column {
        // Reply indicator
        AnimatedVisibility(
            visible = replyToMessage != null,
            enter = androidx.compose.animation.slideInVertically { it },
            exit = androidx.compose.animation.slideOutVertically { it }
        ) {
            replyToMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Reply,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF8B5CF6)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Trả lời ${message.senderName}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8B5CF6)
                            )
                            Text(
                                text = message.content,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = onCancelReply,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Hủy trả lời",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Attachment options
        AnimatedVisibility(
            visible = showAttachmentOptions,
            enter = androidx.compose.animation.slideInVertically { it },
            exit = androidx.compose.animation.slideOutVertically { it }
        ) {
            AttachmentOptionsRow(
                onImageClick = { 
                    showAttachmentOptions = false
                    // Handle image attachment
                },
                onFileClick = { 
                    showAttachmentOptions = false
                    // Handle file attachment
                },
                onAssignmentClick = {
                    showAttachmentOptions = false
                    // Handle assignment sharing
                },
                onLocationClick = {
                    showAttachmentOptions = false
                    // Handle location sharing
                },
                onContactClick = {
                    showAttachmentOptions = false
                    // Handle contact sharing
                }
            )
        }
        
        // Emoji picker
        AnimatedVisibility(
            visible = showEmojiPicker,
            enter = androidx.compose.animation.slideInVertically { it },
            exit = androidx.compose.animation.slideOutVertically { it }
        ) {
            // TODO: Implement EmojiPicker
            androidx.compose.foundation.layout.Box(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.Text("Emoji Picker - Coming Soon")
            }
            /*
            EmojiPicker(
                onEmojiSelected = { emoji ->
                    onMessageTextChange(messageText + emoji)
                    showEmojiPicker = false
                },
                onDismiss = { showEmojiPicker = false }
            )
            */
        }
        
        // Input bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Attach button
                IconButton(
                    onClick = { showAttachmentOptions = !showAttachmentOptions },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = "Đính kèm",
                        tint = Color(0xFF8B5CF6)
                    )
                }
                
                // Text input
                OutlinedTextField(
                    value = messageText,
                    onValueChange = onMessageTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nhập tin nhắn...") },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    maxLines = 4,
                    trailingIcon = {
                        Row {
                            IconButton(onClick = { showEmojiPicker = !showEmojiPicker }) {
                                Text(
                                    "😊",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            // Voice message button (placeholder)
                            IconButton(onClick = { /* Handle voice message */ }) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "Tin nhắn thoại",
                                    tint = Color(0xFF8B5CF6)
                                )
                            }
                        }
                    }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Send button
                FloatingActionButton(
                    onClick = onSendMessage,
                    modifier = Modifier.size(48.dp),
                    containerColor = if (messageText.isNotBlank()) Color(0xFF8B5CF6) else Color(0xFFE0E0E0),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Gửi",
                        tint = if (messageText.isNotBlank()) Color.White else Color(0xFF9E9E9E)
                    )
                }
            }
        }
    }
}

@Composable
fun AttachmentOptionsRow(
    onImageClick: () -> Unit,
    onFileClick: () -> Unit,
    onAssignmentClick: () -> Unit,
    onLocationClick: () -> Unit = {},
    onContactClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF8F9FA),
        shadowElevation = 4.dp
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AttachmentOption(
                    icon = Icons.Default.Image,
                    label = "Hình ảnh",
                    color = Color(0xFF4CAF50),
                    onClick = onImageClick
                )
            }
            item {
                AttachmentOption(
                    icon = Icons.Default.AttachFile,
                    label = "File",
                    color = Color(0xFF2196F3),
                    onClick = onFileClick
                )
            }
            item {
                AttachmentOption(
                    icon = Icons.Default.Assignment,
                    label = "Bài tập",
                    color = Color(0xFF8B5CF6),
                    onClick = onAssignmentClick
                )
            }
            item {
                AttachmentOption(
                    icon = Icons.Default.LocationOn,
                    label = "Vị trí",
                    color = Color(0xFFFF5722),
                    onClick = onLocationClick
                )
            }
            item {
                AttachmentOption(
                    icon = Icons.Default.Person,
                    label = "Liên hệ",
                    color = Color(0xFF9C27B0),
                    onClick = onContactClick
                )
            }
            item {
                AttachmentOption(
                    icon = Icons.Default.Poll,
                    label = "Khảo sát",
                    color = Color(0xFFFF9800),
                    onClick = { /* Handle poll creation */ }
                )
            }
        }
    }
}

@Composable
fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.1f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp),
                    tint = color
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ChatMessageItem(
    message: GroupMessage,
    isCurrentUser: Boolean,
    onReply: () -> Unit,
    onReact: (String) -> Unit,
    onDelete: () -> Unit
) {
    val alignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
        ) {
            // Reply indicator
            message.replyToMessageId?.let {
                ReplyIndicator(
                    replyToContent = message.replyToContent ?: "",
                    replyToSenderName = message.replyToSenderName ?: "",
                    isCurrentUser = isCurrentUser
                )
            }
            
            // Message bubble
            ChatMessageBubble(
                message = message,
                isCurrentUser = isCurrentUser,
                onReply = onReply,
                onReact = onReact,
                onDelete = onDelete
            )
            
            // Timestamp
            Text(
                text = formatChatTimestamp(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: GroupMessage,
    isCurrentUser: Boolean,
    onReply: () -> Unit,
    onReact: (String) -> Unit,
    onDelete: () -> Unit
) {
    var showActions by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .clickable { showActions = !showActions },
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = if (isCurrentUser) 16.dp else 4.dp,
            bottomEnd = if (isCurrentUser) 4.dp else 16.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) Color(0xFF8B5CF6) else Color(0xFFF5F5F5)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Sender name (for group messages from others)
            if (!isCurrentUser) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B5CF6)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            // Message content
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.onSurface
            )
            
            // Reactions
            message.reactions?.let { reactions ->
                if (reactions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    ChatReactionsRow(reactions = reactions)
                }
            }
            
            // Edited indicator
            if (message.isEdited) {
                Text(
                    text = "đã chỉnh sửa",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCurrentUser) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
    
    // Message actions
    AnimatedVisibility(
        visible = showActions,
        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(),
        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically()
    ) {
        ChatMessageActions(
            isCurrentUser = isCurrentUser,
            onReply = onReply,
            onReact = onReact,
            onDelete = onDelete
        )
    }
}

@Composable
fun ReplyIndicator(
    replyToContent: String,
    replyToSenderName: String,
    isCurrentUser: Boolean
) {
    Card(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .padding(bottom = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) Color(0xFF8B5CF6).copy(alpha = 0.3f) else Color(0xFFE0E0E0)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = replyToSenderName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B5CF6)
            )
            Text(
                text = replyToContent,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ChatReactionsRow(reactions: String) {
    // Simple parsing for demo - in real app, parse JSON properly
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        reactions.split(",").take(3).forEach { reaction ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF8B5CF6).copy(alpha = 0.1f)
            ) {
                Text(
                    text = reaction.take(2), // Show emoji part
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun ChatMessageActions(
    isCurrentUser: Boolean,
    onReply: () -> Unit,
    onReact: (String) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Reply
            IconButton(
                onClick = onReply,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Reply,
                    contentDescription = "Trả lời",
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // React
            IconButton(
                onClick = { onReact("❤️") },
                modifier = Modifier.size(32.dp)
            ) {
                Text("❤️")
            }
            
            IconButton(
                onClick = { onReact("👍") },
                modifier = Modifier.size(32.dp)
            ) {
                Text("👍")
            }
            
            // Delete (only for current user)
            if (isCurrentUser) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Xóa",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyChatCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
                Icons.Default.Chat,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF8B5CF6).copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Chưa có tin nhắn nào",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B5CF6)
            )
            Text(
                text = "Hãy bắt đầu cuộc trò chuyện!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Utility functions
fun formatChatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Vừa xong"
        diff < 3600_000 -> "${diff / 60_000} phút trước"
        diff < 86400_000 -> "${diff / 3600_000} giờ trước"
        else -> {
            val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

// Assignment sharing in chat
@Composable
fun AssignmentMessageBubble(
    assignmentTitle: String,
    assignmentDueDate: String?,
    assignmentStatus: String,
    isCurrentUser: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = if (isCurrentUser) 16.dp else 4.dp,
            bottomEnd = if (isCurrentUser) 4.dp else 16.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) Color(0xFF8B5CF6) else Color(0xFFF5F5F5)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCurrentUser) Color.Transparent else Color(0xFF8B5CF6).copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Assignment,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isCurrentUser) Color.White else Color(0xFF8B5CF6)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bài tập",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentUser) Color.White.copy(alpha = 0.8f) else Color(0xFF8B5CF6)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = assignmentTitle,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.onSurface
            )
            
            assignmentDueDate?.let { dueDate ->
                Text(
                    text = "Hạn: $dueDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCurrentUser) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = getStatusColor(assignmentStatus).copy(alpha = if (isCurrentUser) 0.3f else 0.1f)
            ) {
                Text(
                    text = getStatusText(assignmentStatus),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCurrentUser) Color.White else getStatusColor(assignmentStatus),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// Quick actions for assignments in chat
@Composable
fun AssignmentQuickActions(
    onViewAssignment: () -> Unit,
    onMarkComplete: () -> Unit,
    onAddReminder: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Thao tác nhanh",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B5CF6)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Visibility,
                    label = "Xem chi tiết",
                    onClick = onViewAssignment,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.CheckCircle,
                    label = "Hoàn thành",
                    onClick = onMarkComplete,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.Alarm,
                    label = "Nhắc nhở",
                    onClick = onAddReminder,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f)),
        contentPadding = PaddingValues(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF8B5CF6)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8B5CF6)
            )
        }
    }
}

// Online status indicator for members
@Composable
fun OnlineStatusIndicator(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(12.dp)
            .background(
                color = if (isOnline) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                shape = CircleShape
            )
    )
}

// Typing indicator
@Composable
fun TypingIndicator(
    typingUsers: List<String>
) {
    AnimatedVisibility(
        visible = typingUsers.isNotEmpty(),
        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(),
        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated dots
                repeat(3) { index ->
                    val animatedAlpha by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if ((System.currentTimeMillis() / 500) % 3 == index.toLong()) 1f else 0.3f,
                        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                            animation = androidx.compose.animation.core.tween(600),
                            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                        ),
                        label = "typing_animation"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                Color(0xFF8B5CF6).copy(alpha = animatedAlpha),
                                CircleShape
                            )
                    )
                    
                    if (index < 2) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = when (typingUsers.size) {
                        1 -> "${typingUsers.first()} đang nhập..."
                        2 -> "${typingUsers.joinToString(" và ")} đang nhập..."
                        else -> "${typingUsers.take(2).joinToString(", ")} và ${typingUsers.size - 2} người khác đang nhập..."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}
// Advanced Chat Components for GroupDetailScreen

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF8F9FA),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF8B5CF6)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Tìm kiếm tin nhắn...") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color.Transparent
                ),
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Xóa")
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Đóng")
            }
        }
    }
}

@Composable
fun PinnedMessagesSection(
    pinnedMessages: List<GroupMessage>,
    onMessageClick: (Long) -> Unit,
    onUnpin: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFFFF9800).copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tin nhắn đã ghim (${pinnedMessages.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pinnedMessages) { message ->
                    PinnedMessageCard(
                        message = message,
                        onClick = { onMessageClick(message.id) },
                        onUnpin = { onUnpin(message.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun PinnedMessageCard(
    message: GroupMessage,
    onClick: () -> Unit,
    onUnpin: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onUnpin,
                    modifier = Modifier.size(16.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Bỏ ghim",
                        modifier = Modifier.size(12.dp),
                        tint = Color(0xFF9E9E9E)
                    )
                }
            }
            
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = formatChatTimestamp(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun UploadProgressBar(progress: Float) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF5F5F5)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đang tải lên...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8B5CF6)
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B5CF6)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = Color(0xFF8B5CF6),
                trackColor = Color(0xFF8B5CF6).copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun AdvancedMessageBubble(
    message: GroupMessage,
    isCurrentUser: Boolean,
    isOnline: Boolean,
    onReply: () -> Unit,
    onEdit: () -> Unit,
    onReact: (String) -> Unit,
    onDelete: () -> Unit,
    onPin: () -> Unit,
    onFileClick: () -> Unit,
    onAssignmentClick: () -> Unit
) {
    var showActions by remember { mutableStateOf(false) }
    
    Column(
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        // Reply indicator
        message.replyToMessageId?.let {
            ReplyIndicator(
                replyToContent = message.replyToContent ?: "",
                replyToSenderName = message.replyToSenderName ?: "",
                isCurrentUser = isCurrentUser
            )
        }
        
        // Message bubble
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clickable { showActions = !showActions },
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isCurrentUser) 16.dp else 4.dp,
                bottomEnd = if (isCurrentUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isCurrentUser) Color(0xFF8B5CF6) else Color(0xFFF5F5F5)
            ),
            border = if (message.isPinned) {
                androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF9800))
            } else null
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Sender info with online status
                if (!isCurrentUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B5CF6)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        OnlineStatusIndicator(
                            isOnline = isOnline,
                            modifier = Modifier.size(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // Pinned indicator
                if (message.isPinned) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFFFF9800)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Đã ghim",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF9800)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // Message content based on type
                when (message.messageType) {
                    MessageType.TEXT -> {
                        // Check if it's assignment message
                        if (message.content.startsWith("📝 Bài tập:")) {
                            AssignmentMessageContent(
                                content = message.content,
                                onClick = onAssignmentClick
                            )
                        } else {
                            Text(
                                text = message.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    MessageType.IMAGE -> {
                        ImageMessageContent(
                            imageUrl = message.fileUrl ?: "",
                            fileName = message.fileName ?: "Image",
                            onClick = onFileClick
                        )
                    }
                    MessageType.FILE -> {
                        FileMessageContent(
                            fileName = message.fileName ?: "File",
                            fileSize = message.fileSize ?: 0,
                            fileUrl = message.fileUrl ?: "",
                            onClick = onFileClick
                        )
                    }
                    MessageType.SYSTEM -> {
                        SystemMessageContent(content = message.content)
                    }
                    else -> {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // Reactions
                message.reactions?.let { reactions ->
                    if (reactions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        ChatReactionsRow(reactions = reactions)
                    }
                }
                
                // Message metadata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = formatChatTimestamp(message.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCurrentUser) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (message.isEdited) {
                        Text(
                            text = "đã chỉnh sửa",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCurrentUser) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
        
        // Message actions
        AnimatedVisibility(
            visible = showActions,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically()
        ) {
            AdvancedMessageActions(
                isCurrentUser = isCurrentUser,
                isPinned = message.isPinned,
                onReply = onReply,
                onEdit = onEdit,
                onReact = onReact,
                onDelete = onDelete,
                onPin = onPin
            )
        }
    }
}

@Composable
fun AssignmentMessageContent(
    content: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF8B5CF6).copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFF8B5CF6).copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Assignment,
                    contentDescription = null,
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bài tập được chia sẻ",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B5CF6)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ImageMessageContent(
    imageUrl: String,
    fileName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(200.dp, 150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for actual image loading
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFF8B5CF6)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun FileMessageContent(
    fileName: String,
    fileSize: Long,
    fileUrl: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFFE0E0E0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AttachFile,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color(0xFF8B5CF6)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatFileSize(fileSize),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.Download,
                contentDescription = "Tải xuống",
                tint = Color(0xFF8B5CF6),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SystemMessageContent(content: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun AdvancedMessageActions(
    isCurrentUser: Boolean,
    isPinned: Boolean,
    onReply: () -> Unit,
    onEdit: () -> Unit,
    onReact: (String) -> Unit,
    onDelete: () -> Unit,
    onPin: () -> Unit
) {
    Card(
        modifier = Modifier.padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Reply
            IconButton(
                onClick = onReply,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Reply,
                    contentDescription = "Trả lời",
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // React
            IconButton(
                onClick = { onReact("❤️") },
                modifier = Modifier.size(32.dp)
            ) {
                Text("❤️")
            }
            
            IconButton(
                onClick = { onReact("👍") },
                modifier = Modifier.size(32.dp)
            ) {
                Text("👍")
            }
            
            IconButton(
                onClick = { onReact("😊") },
                modifier = Modifier.size(32.dp)
            ) {
                Text("😊")
            }
            
            // Pin/Unpin
            IconButton(
                onClick = onPin,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    if (isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                    contentDescription = if (isPinned) "Bỏ ghim" else "Ghim",
                    modifier = Modifier.size(16.dp),
                    tint = if (isPinned) Color(0xFFFF9800) else Color(0xFF9E9E9E)
                )
            }
            
            // Edit (only for current user)
            if (isCurrentUser) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Chỉnh sửa",
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                // Delete
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Xóa",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

// Utility function for file size formatting
fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}