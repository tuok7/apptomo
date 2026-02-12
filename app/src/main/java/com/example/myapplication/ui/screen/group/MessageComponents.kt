package com.example.myapplication.ui.screen.group

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.api.MessageData
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModernChatMessageItem(
    message: com.example.myapplication.data.model.GroupMessage,
    isCurrentUser: Boolean,
    currentUserId: Long,
    onDeleteClick: () -> Unit,
    onReplyClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onReactionClick: (String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val time = dateFormat.format(Date(message.timestamp))
    
    var showMessageActions by remember { mutableStateOf(false) }
    var showReactionPicker by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isCurrentUser) {
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            Column(
                modifier = Modifier.widthIn(max = 280.dp),
                horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
            ) {
                if (!isCurrentUser) {
                    Text(
                        text = message.senderName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                
                Box {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = if (isCurrentUser) 20.dp else 4.dp,
                            topEnd = if (isCurrentUser) 4.dp else 20.dp,
                            bottomStart = 20.dp,
                            bottomEnd = 20.dp
                        ),
                        color = if (isCurrentUser) Color(0xFF4A90E2) else Color(0xFFF5F5F5),
                        shadowElevation = 2.dp,
                        modifier = Modifier.combinedClickable(
                            onClick = { },
                            onLongClick = { showMessageActions = true }
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Reply preview
                            if (message.replyToMessageId != null && message.replyToContent != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isCurrentUser) 
                                        Color.White.copy(alpha = 0.2f) 
                                    else 
                                        Color.Gray.copy(alpha = 0.1f),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = message.replyToSenderName ?: "Người dùng",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrentUser) Color.White else Color(0xFF4A90E2)
                                        )
                                        Text(
                                            text = message.replyToContent,
                                            fontSize = 12.sp,
                                            color = if (isCurrentUser) 
                                                Color.White.copy(alpha = 0.8f) 
                                            else 
                                                Color.Gray,
                                            maxLines = 2,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            
                            Text(
                                text = message.content,
                                fontSize = 15.sp,
                                color = if (isCurrentUser) Color.White else Color.Black,
                                lineHeight = 20.sp
                            )
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = time,
                                    fontSize = 11.sp,
                                    color = if (isCurrentUser) Color.White.copy(alpha = 0.8f) else Color.Gray
                                )
                                if (message.isEdited) {
                                    Text(
                                        text = "• Đã chỉnh sửa",
                                        fontSize = 11.sp,
                                        color = if (isCurrentUser) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                    
                    IconButton(
                        onClick = { showReactionPicker = true },
                        modifier = Modifier
                            .size(24.dp)
                            .align(if (isCurrentUser) Alignment.BottomStart else Alignment.BottomEnd)
                            .offset(x = if (isCurrentUser) (-8).dp else 8.dp, y = 8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 2.dp
                        ) {
                            Icon(
                                Icons.Default.AddReaction,
                                contentDescription = "Thả cảm xúc",
                                tint = Color.Gray,
                                modifier = Modifier.padding(4.dp).size(16.dp)
                            )
                        }
                    }
                }
                
                // Display reactions
                if (!message.reactions.isNullOrEmpty()) {
                    ReactionDisplay(
                        reactions = message.reactions,
                        isCurrentUser = isCurrentUser
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isCurrentUser) Color(0xFF4A90E2) else Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
    
    if (showMessageActions) {
        MessageActionsBottomSheet(
            isCurrentUser = isCurrentUser,
            onDismiss = { showMessageActions = false },
            onReply = { onReplyClick(); showMessageActions = false },
            onEdit = { showEditDialog = true; showMessageActions = false },
            onDelete = { onDeleteClick(); showMessageActions = false },
            onReaction = { showReactionPicker = true; showMessageActions = false }
        )
    }
    
    if (showReactionPicker) {
        ReactionPickerDialog(
            onDismiss = { showReactionPicker = false },
            onReactionSelected = { reaction -> onReactionClick(reaction); showReactionPicker = false }
        )
    }
    
    if (showEditDialog) {
        EditMessageDialog(
            currentMessage = message.content,
            onDismiss = { showEditDialog = false },
            onConfirm = { newText -> onEditClick(newText); showEditDialog = false }
        )
    }
}

@Composable
fun ReactionDisplay(reactions: String, isCurrentUser: Boolean) {
    val reactionList = remember(reactions) {
        try {
            val reactionMap = org.json.JSONObject(reactions)
            val list = mutableListOf<Pair<String, Int>>()
            
            reactionMap.keys().forEach { emoji ->
                val users = reactionMap.getJSONArray(emoji)
                list.add(emoji to users.length())
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    if (reactionList.isNotEmpty()) {
        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            reactionList.forEach { (emoji, count) ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = emoji, fontSize = 14.sp)
                        if (count > 1) {
                            Text(
                                text = count.toString(),
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageActionsBottomSheet(
    isCurrentUser: Boolean,
    onDismiss: () -> Unit,
    onReply: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReaction: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Tùy chọn tin nhắn",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            MessageActionItem(Icons.Default.Reply, "Trả lời", onReply)
            MessageActionItem(Icons.Default.AddReaction, "Thả cảm xúc", onReaction)
            
            if (isCurrentUser) {
                MessageActionItem(Icons.Default.Edit, "Chỉnh sửa", onEdit)
                MessageActionItem(Icons.Default.Delete, "Thu hồi", onDelete, Color(0xFFF44336))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MessageActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    color: Color = Color.Black
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = text, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, fontSize = 16.sp, color = color)
        }
    }
}

@Composable
fun ReactionPickerDialog(
    onDismiss: () -> Unit,
    onReactionSelected: (String) -> Unit
) {
    val reactions = listOf("❤️", "👍", "😂", "😮", "😢", "😡", "🎉", "🔥")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn cảm xúc") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                reactions.forEach { reaction ->
                    TextButton(onClick = { onReactionSelected(reaction) }) {
                        Text(reaction, fontSize = 32.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun EditMessageDialog(
    currentMessage: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var editedText by remember { mutableStateOf(currentMessage) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chỉnh sửa tin nhắn") },
        text = {
            OutlinedTextField(
                value = editedText,
                onValueChange = { editedText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nhập nội dung mới...") },
                minLines = 3,
                maxLines = 5
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(editedText) },
                enabled = editedText.isNotBlank() && editedText != currentMessage
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}


// Wrapper component for backward compatibility with MessageData
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LegacyChatMessageItem(
    message: com.example.myapplication.data.api.MessageData,
    isCurrentUser: Boolean,
    currentUserId: Long,
    onDeleteClick: () -> Unit,
    onReplyClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onReactionClick: (String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val time = try {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(message.createdAt)
        dateFormat.format(date ?: Date())
    } catch (e: Exception) {
        message.createdAt
    }
    
    var showMessageActions by remember { mutableStateOf(false) }
    var showReactionPicker by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isCurrentUser) {
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            Column(
                modifier = Modifier.widthIn(max = 280.dp),
                horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
            ) {
                if (!isCurrentUser) {
                    Text(
                        text = message.senderName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                
                Box {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = if (isCurrentUser) 20.dp else 4.dp,
                            topEnd = if (isCurrentUser) 4.dp else 20.dp,
                            bottomStart = 20.dp,
                            bottomEnd = 20.dp
                        ),
                        color = if (isCurrentUser) Color(0xFF4A90E2) else Color(0xFFF5F5F5),
                        shadowElevation = 2.dp,
                        modifier = Modifier.combinedClickable(
                            onClick = { },
                            onLongClick = { showMessageActions = true }
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = message.message,
                                fontSize = 15.sp,
                                color = if (isCurrentUser) Color.White else Color.Black,
                                lineHeight = 20.sp
                            )
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = time,
                                    fontSize = 11.sp,
                                    color = if (isCurrentUser) Color.White.copy(alpha = 0.8f) else Color.Gray
                                )
                            }
                        }
                    }
                    
                    IconButton(
                        onClick = { showReactionPicker = true },
                        modifier = Modifier
                            .size(24.dp)
                            .align(if (isCurrentUser) Alignment.BottomStart else Alignment.BottomEnd)
                            .offset(x = if (isCurrentUser) (-8).dp else 8.dp, y = 8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 2.dp
                        ) {
                            Icon(
                                Icons.Default.AddReaction,
                                contentDescription = "Thả cảm xúc",
                                tint = Color.Gray,
                                modifier = Modifier.padding(4.dp).size(16.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isCurrentUser) Color(0xFF4A90E2) else Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
    
    if (showMessageActions) {
        MessageActionsBottomSheet(
            isCurrentUser = isCurrentUser,
            onDismiss = { showMessageActions = false },
            onReply = { onReplyClick(); showMessageActions = false },
            onEdit = { showEditDialog = true; showMessageActions = false },
            onDelete = { onDeleteClick(); showMessageActions = false },
            onReaction = { showReactionPicker = true; showMessageActions = false }
        )
    }
    
    if (showReactionPicker) {
        ReactionPickerDialog(
            onDismiss = { showReactionPicker = false },
            onReactionSelected = { reaction -> onReactionClick(reaction); showReactionPicker = false }
        )
    }
    
    if (showEditDialog) {
        EditMessageDialog(
            currentMessage = message.message,
            onDismiss = { showEditDialog = false },
            onConfirm = { newText -> onEditClick(newText); showEditDialog = false }
        )
    }
}
