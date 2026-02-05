package com.example.myapplication.ui.screen.group

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.api.MessageData
import com.example.myapplication.ui.viewmodel.GroupViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    viewModel: GroupViewModel,
    groupId: Long,
    groupName: String,
    currentUserId: Long,
    onBackClick: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Trò chuyện", "Tài liệu", "Công việc")
    
    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()
    
    // Load messages when screen opens
    LaunchedEffect(groupId) {
        viewModel.loadMessages(groupId)
    }
    
    // Auto scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Custom Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.Black
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = groupName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "${messages.size} tin nhắn",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    
                    IconButton(
                        onClick = { /* Info */ },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Thông tin",
                            tint = Color.Black
                        )
                    }
                }
                
                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = Color.Black
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    color = if (selectedTab == index) Color(0xFF4A90E2) else Color.Gray
                                )
                            }
                        )
                    }
                }
            }
        }
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> {
                // Chat content
                Box(modifier = Modifier.weight(1f)) {
                    if (messages.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Chưa có tin nhắn nào",
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            state = listState,
                            reverseLayout = false,
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(messages, key = { it.id }) { message ->
                                ModernChatMessageItem(
                                    message = message,
                                    isCurrentUser = message.userId == currentUserId,
                                    onDeleteClick = {
                                        viewModel.deleteMessage(message.id, groupId)
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
                
                // Input area
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
                        IconButton(
                            onClick = { /* Attach */ },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Đính kèm",
                                tint = Color.Gray
                            )
                        }
                        
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { 
                                Text(
                                    "Nhắn tin...",
                                    color = Color.Gray
                                ) 
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4A90E2),
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color(0xFF4A90E2)
                            ),
                            maxLines = 3
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        IconButton(
                            onClick = { /* Emoji */ },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.EmojiEmotions,
                                contentDescription = "Emoji",
                                tint = Color.Gray
                            )
                        }
                        
                        FloatingActionButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    viewModel.sendMessage(groupId, messageText.trim())
                                    messageText = ""
                                }
                            },
                            modifier = Modifier.size(48.dp),
                            containerColor = if (messageText.isNotBlank()) Color(0xFF4A90E2) else Color.LightGray
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Gửi",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
            1 -> {
                // Documents tab
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Tài liệu nhóm sẽ hiển thị ở đây",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
            2 -> {
                // Tasks tab
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Công việc nhóm sẽ hiển thị ở đây",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ModernChatMessageItem(
    message: MessageData,
    isCurrentUser: Boolean,
    onDeleteClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val time = try {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(message.createdAt)
        dateFormat.format(date ?: Date())
    } catch (e: Exception) {
        message.createdAt
    }
    
    Column {
        if (!isCurrentUser) {
            // Show sender name for others
            Text(
                text = message.senderName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                modifier = Modifier.padding(start = 52.dp, bottom = 4.dp)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isCurrentUser) {
                // Avatar for others
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = message.senderName.first().toString(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            Column(
                modifier = Modifier.widthIn(max = 280.dp),
                horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
            ) {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = if (isCurrentUser) 20.dp else 4.dp,
                        topEnd = if (isCurrentUser) 4.dp else 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 20.dp
                    ),
                    color = if (isCurrentUser) 
                        Color(0xFF4A90E2)
                    else 
                        Color(0xFFF5F5F5),
                    shadowElevation = 2.dp,
                    modifier = if (isCurrentUser) Modifier.clickable { onDeleteClick() } else Modifier
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = message.message,
                            fontSize = 14.sp,
                            color = if (isCurrentUser) Color.White else Color.Black,
                            lineHeight = 20.sp
                        )
                    }
                }
                
                Text(
                    text = time,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            if (isCurrentUser) {
                Spacer(modifier = Modifier.width(12.dp))
                // Avatar for current user
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4A90E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = message.senderName.first().toString(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MemberListDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thành viên nhóm") },
        text = {
            LazyColumn {
                items(5) { index ->
                    MemberItem(
                        name = when(index) {
                            0 -> "Nguyễn Văn A"
                            1 -> "Trần Thị B"
                            2 -> "Lê Văn C"
                            3 -> "Phạm Thị D"
                            else -> "Hoàng Văn E"
                        },
                        role = if (index == 0) "Admin" else "Member",
                        isOnline = index < 3
                    )
                    if (index < 4) Divider()
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
fun MemberItem(name: String, role: String, isOnline: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.first().toString(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            if (isOnline) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                        .align(Alignment.BottomEnd)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = role,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (role == "Admin") {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "Admin",
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
