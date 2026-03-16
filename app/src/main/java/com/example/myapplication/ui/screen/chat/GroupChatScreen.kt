package com.example.myapplication.ui.screen.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.myapplication.data.model.GroupMessage
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    viewModel: ChatViewModel,
    groupId: Long,
    groupName: String,
    onBackClick: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var messageText by remember { mutableStateOf("") }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var replyToMessage by remember { mutableStateOf<GroupMessage?>(null) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Auto scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
    
    LaunchedEffect(groupId) {
        viewModel.loadMessages(groupId)
    }
    
    Scaffold(
        topBar = {
            ChatTopBar(
                groupName = groupName,
                onBackClick = onBackClick,
                onInfoClick = { /* Navigate to group info */ }
            )
        },
        bottomBar = {
            ChatBottomBar(
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                onSendMessage = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(
                            groupId = groupId,
                            content = messageText.trim(),
                            replyToMessageId = replyToMessage?.id
                        )
                        messageText = ""
                        replyToMessage = null
                    }
                },
                onAttachFile = { /* Handle file attachment */ },
                onEmojiClick = { showEmojiPicker = !showEmojiPicker },
                replyToMessage = replyToMessage,
                onCancelReply = { replyToMessage = null }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageItem(
                        message = message,
                        isCurrentUser = message.senderId == currentUser?.id,
                        onReply = { replyToMessage = message },
                        onReact = { emoji -> viewModel.reactToMessage(message.id, emoji) },
                        onDelete = { viewModel.deleteMessage(message.id) }
                    )
                }
            }
            
            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF8B5CF6)
                )
            }
            
            // Emoji Picker (simplified)
            AnimatedVisibility(
                visible = showEmojiPicker,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                EmojiPicker(
                    onEmojiSelected = { emoji ->
                        messageText += emoji
                        showEmojiPicker = false
                    },
                    onDismiss = { showEmojiPicker = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    groupName: String,
    onBackClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Group Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = groupName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = groupName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Đang hoạt động",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
            }
        },
        actions = {
            IconButton(onClick = onInfoClick) {
                Icon(Icons.Default.Info, contentDescription = "Thông tin nhóm")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

@Composable
fun MessageItem(
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
            MessageBubble(
                message = message,
                isCurrentUser = isCurrentUser,
                onReply = onReply,
                onReact = onReact,
                onDelete = onDelete
            )
            
            // Timestamp
            Text(
                text = formatTimestamp(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun MessageBubble(
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
            when (message.messageType) {
                MessageType.TEXT -> {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
                MessageType.IMAGE -> {
                    ImageMessage(
                        imageUrl = message.fileUrl ?: "",
                        fileName = message.fileName ?: "Image"
                    )
                }
                MessageType.FILE -> {
                    FileMessage(
                        fileName = message.fileName ?: "File",
                        fileSize = message.fileSize ?: 0,
                        fileUrl = message.fileUrl ?: ""
                    )
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
                    ReactionsRow(reactions = reactions)
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
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        MessageActions(
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
fun ImageMessage(imageUrl: String, fileName: String) {
    // Placeholder for image message
    Card(
        modifier = Modifier.size(200.dp, 150.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F0F0)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFF8B5CF6)
                )
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FileMessage(fileName: String, fileSize: Long, fileUrl: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA)
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
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatFileSize(fileSize),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { /* Download file */ }) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "Tải xuống",
                    tint = Color(0xFF8B5CF6)
                )
            }
        }
    }
}

@Composable
fun ReactionsRow(reactions: String) {
    // Parse reactions JSON and display
    // For now, show placeholder
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf("❤️", "👍", "😊").forEach { emoji ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF8B5CF6).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "$emoji 2",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun MessageActions(
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
fun ChatBottomBar(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onAttachFile: () -> Unit,
    onEmojiClick: () -> Unit,
    replyToMessage: GroupMessage?,
    onCancelReply: () -> Unit
) {
    Column {
        // Reply indicator
        AnimatedVisibility(
            visible = replyToMessage != null,
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
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
                    onClick = onAttachFile,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = "Đính kèm file",
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
                        IconButton(onClick = onEmojiClick) {
                            Text(
                                "😊",
                                style = MaterialTheme.typography.titleMedium
                            )
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
fun EmojiPicker(
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chọn emoji",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng")
                }
            }
            
            // Emoji grid
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                val emojis = listOf(
                    "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣",
                    "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰",
                    "😘", "😗", "😙", "😚", "😋", "😛", "😝", "😜",
                    "🤪", "🤨", "🧐", "🤓", "😎", "🤩", "🥳", "😏",
                    "👍", "👎", "👌", "✌️", "🤞", "🤟", "🤘", "🤙",
                    "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍"
                )
                
                items(emojis.chunked(8)) { emojiRow ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        emojiRow.forEach { emoji ->
                            TextButton(
                                onClick = { onEmojiSelected(emoji) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Text(
                                    text = emoji,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Utility functions
fun formatTimestamp(timestamp: Long): String {
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

fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}