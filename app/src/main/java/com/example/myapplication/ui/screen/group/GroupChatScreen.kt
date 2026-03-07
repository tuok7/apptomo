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
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.data.model.GroupMessage
import com.example.myapplication.data.model.Member
import com.example.myapplication.data.database.AppDatabase
import com.example.myapplication.ui.viewmodel.GroupViewModel
import kotlinx.coroutines.launch
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
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    
    var messageText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Trò chuyện", "Tài liệu", "Công việc")
    
    // Load messages from Room database
    var messages by remember { mutableStateOf<List<GroupMessage>>(emptyList()) }
    val listState = rememberLazyListState()
    
    // Reply state
    var replyingTo by remember { mutableStateOf<GroupMessage?>(null) }
    
    // Info dialog state
    var showInfoDialog by remember { mutableStateOf(false) }
    
    // Members state
    var members by remember { mutableStateOf<List<Member>>(emptyList()) }
    var showMembersDialog by remember { mutableStateOf(false) }
    
    // Get current user name from preferences
    val userPreferences = remember { 
        com.example.myapplication.data.preferences.UserPreferences(context) 
    }
    val currentUserName = remember { userPreferences.getFullName().ifEmpty { "Người dùng" } }
    
    // Create ChatHelper
    val chatHelper = remember(database, groupId, currentUserId, currentUserName) {
        ChatHelper(database, groupId, currentUserId, currentUserName)
    }
    
    // Load messages when screen opens
    LaunchedEffect(groupId) {
        scope.launch {
            database.groupMessageDao().getMessagesByGroup(groupId).collect { messageList ->
                messages = messageList
            }
        }
    }
    
    // Load members
    LaunchedEffect(groupId) {
        scope.launch {
            database.memberDao().getMembersByGroup(groupId).collect { memberList ->
                members = memberList
                
                // Thêm dữ liệu mẫu nếu chưa có members
                if (memberList.isEmpty()) {
                    val sampleMembers = listOf(
                        Member(
                            groupId = groupId,
                            name = "Nguyễn Văn A",
                            email = "a@example.com",
                            role = "Admin",
                            isOnline = true,
                            lastSeen = System.currentTimeMillis(),
                            lastActivity = System.currentTimeMillis()
                        ),
                        Member(
                            groupId = groupId,
                            name = "Trần Thị B",
                            email = "b@example.com",
                            role = "Thành viên",
                            isOnline = true,
                            lastSeen = System.currentTimeMillis() - 300000, // 5 phút trước
                            lastActivity = System.currentTimeMillis() - 300000
                        ),
                        Member(
                            groupId = groupId,
                            name = "Lê Văn C",
                            email = "c@example.com",
                            role = "Thành viên",
                            isOnline = false,
                            lastSeen = System.currentTimeMillis() - 3600000, // 1 giờ trước
                            lastActivity = System.currentTimeMillis() - 3600000
                        ),
                        Member(
                            groupId = groupId,
                            name = "Phạm Thị D",
                            email = "d@example.com",
                            role = "Thành viên",
                            isOnline = false,
                            lastSeen = System.currentTimeMillis() - 86400000, // 1 ngày trước
                            lastActivity = System.currentTimeMillis() - 86400000
                        ),
                        Member(
                            groupId = groupId,
                            name = "Hoàng Văn E",
                            email = "e@example.com",
                            role = "Thành viên",
                            isOnline = true,
                            lastSeen = System.currentTimeMillis(),
                            lastActivity = System.currentTimeMillis()
                        )
                    )
                    
                    sampleMembers.forEach { member ->
                        database.memberDao().insertMember(member)
                    }
                }
            }
        }
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${members.count { it.isOnline }} online • ${members.size} thành viên",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            if (members.any { it.isOnline }) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50))
                                )
                            }
                        }
                    }
                    
                    IconButton(
                        onClick = { showMembersDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = "Thành viên",
                            tint = Color.Black
                        )
                    }
                    
                    IconButton(
                        onClick = { showInfoDialog = true },
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
                                    isCurrentUser = message.senderId == currentUserId,
                                    currentUserId = currentUserId,
                                    onDeleteClick = { 
                                        scope.launch {
                                            chatHelper.deleteMessage(message.id)
                                        }
                                    },
                                    onReplyClick = { replyingTo = message },
                                    onEditClick = { newText -> 
                                        scope.launch {
                                            chatHelper.editMessage(message.id, newText)
                                        }
                                    },
                                    onReactionClick = { reaction -> 
                                        scope.launch {
                                            chatHelper.addReaction(message.id, reaction, currentUserId)
                                        }
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
                    Column {
                        // Reply preview
                        if (replyingTo != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFF5F5F5)
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
                                        tint = Color(0xFF4A90E2),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Trả lời ${replyingTo?.senderName}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4A90E2)
                                        )
                                        Text(
                                            text = replyingTo?.content ?: "",
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                            maxLines = 1
                                        )
                                    }
                                    IconButton(
                                        onClick = { replyingTo = null },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Hủy",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                        
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
                                        scope.launch {
                                            chatHelper.sendMessage(
                                                content = messageText.trim(),
                                                replyToMessage = replyingTo
                                            )
                                            messageText = ""
                                            replyingTo = null
                                        }
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
    
    // Info Dialog
    if (showInfoDialog) {
        GroupInfoDialog(
            groupName = groupName,
            messageCount = messages.size,
            memberCount = members.size,
            onlineCount = members.count { it.isOnline },
            onDismiss = { showInfoDialog = false }
        )
    }
    
    // Members Dialog
    if (showMembersDialog) {
        MembersListDialog(
            members = members,
            onDismiss = { showMembersDialog = false }
        )
    }
}

@Composable
fun GroupInfoDialog(
    groupName: String,
    messageCount: Int,
    memberCount: Int,
    onlineCount: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF4A90E2),
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Thông tin nhóm",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(
                    icon = Icons.Default.Group,
                    label = "Tên nhóm",
                    value = groupName
                )
                InfoRow(
                    icon = Icons.Default.Chat,
                    label = "Số tin nhắn",
                    value = "$messageCount tin nhắn"
                )
                InfoRow(
                    icon = Icons.Default.People,
                    label = "Thành viên",
                    value = "$memberCount thành viên"
                )
                InfoRow(
                    icon = Icons.Default.Circle,
                    label = "Đang online",
                    value = "$onlineCount/$memberCount online",
                    iconTint = Color(0xFF4CAF50)
                )
                InfoRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Ngày tạo",
                    value = "Đang cập nhật..."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A90E2)
                )
            ) {
                Text("Đóng")
            }
        }
    )
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconTint: Color = Color(0xFF4A90E2)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun MembersListDialog(
    members: List<Member>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.People,
                contentDescription = null,
                tint = Color(0xFF4A90E2),
                modifier = Modifier.size(48.dp)
            )
        },
        title = { 
            Column {
                Text(
                    text = "Thành viên nhóm",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "${members.count { it.isOnline }} online • ${members.size} tổng",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Online members first
                val onlineMembers = members.filter { it.isOnline }.sortedBy { it.name }
                val offlineMembers = members.filter { !it.isOnline }.sortedBy { it.name }
                
                if (onlineMembers.isNotEmpty()) {
                    item {
                        Text(
                            text = "ĐANG ONLINE (${onlineMembers.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(onlineMembers) { member ->
                        MemberItem(
                            member = member,
                            showLastSeen = false
                        )
                    }
                }
                
                if (offlineMembers.isNotEmpty()) {
                    item {
                        Text(
                            text = "OFFLINE (${offlineMembers.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(offlineMembers) { member ->
                        MemberItem(
                            member = member,
                            showLastSeen = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A90E2)
                )
            ) {
                Text("Đóng")
            }
        }
    )
}

@Composable
fun MemberItem(
    member: Member,
    showLastSeen: Boolean = false
) {
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
                    .background(
                        if (member.isOnline) Color(0xFF4A90E2) 
                        else Color(0xFFE0E0E0)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.name.first().toString().uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = if (member.isOnline) Color.White else Color.Gray,
                    fontSize = 16.sp
                )
            }
            
            // Online indicator
            if (member.isOnline) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                        .align(Alignment.BottomEnd)
                        .padding(1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            
            if (member.isOnline) {
                Text(
                    text = "Đang hoạt động",
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50)
                )
            } else if (showLastSeen) {
                Text(
                    text = getLastSeenText(member.lastSeen),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
        
        // Role badge
        if (member.role == "Admin") {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF4A90E2).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "Admin",
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color(0xFF4A90E2),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun getLastSeenText(lastSeen: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - lastSeen
    
    return when {
        diff < 60_000 -> "Vừa xem"
        diff < 3600_000 -> "${diff / 60_000} phút trước"
        diff < 86400_000 -> "${diff / 3600_000} giờ trước"
        diff < 604800_000 -> "${diff / 86400_000} ngày trước"
        else -> "Lâu rồi"
    }
}
