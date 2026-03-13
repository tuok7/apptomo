package com.example.myapplication.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.GroupMessage
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.data.model.Member
import com.example.myapplication.data.repository.MessageRepository
import com.example.myapplication.data.preferences.UserPreferences
import com.example.myapplication.util.FileUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class ChatViewModel(
    private val messageRepository: MessageRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _messages = MutableStateFlow<List<GroupMessage>>(emptyList())
    val messages: StateFlow<List<GroupMessage>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _currentUser = MutableStateFlow<Member?>(null)
    val currentUser: StateFlow<Member?> = _currentUser.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _typingUsers = MutableStateFlow<List<String>>(emptyList())
    val typingUsers: StateFlow<List<String>> = _typingUsers.asStateFlow()
    
    private val _onlineUsers = MutableStateFlow<Set<Long>>(emptySet())
    val onlineUsers: StateFlow<Set<Long>> = _onlineUsers.asStateFlow()
    
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()
    
    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()
    
    private val _pinnedMessages = MutableStateFlow<List<GroupMessage>>(emptyList())
    val pinnedMessages: StateFlow<List<GroupMessage>> = _pinnedMessages.asStateFlow()
    
    init {
        loadCurrentUser()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                // Load current user from preferences
                val userId = userPreferences.getUserId()
                val userName = userPreferences.getFullName()
                if (userId != null && userName != null) {
                    _currentUser.value = Member(
                        id = userId,
                        groupId = 0, // Temporary groupId
                        name = userName,
                        email = userPreferences.getEmail()
                    )
                }
            } catch (e: Exception) {
                _error.value = "Không thể tải thông tin người dùng: ${e.message}"
            }
        }
    }
    
    fun loadMessages(groupId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Simulate loading online users
                _onlineUsers.value = setOf(1L, 2L, 3L)
                
                // Create comprehensive mock messages with different types
                val mockMessages = listOf(
                    // System message
                    GroupMessage(
                        id = 1,
                        groupId = groupId,
                        senderId = 0,
                        senderName = "Hệ thống",
                        content = "Nhóm học tập đã được tạo",
                        messageType = MessageType.SYSTEM,
                        timestamp = System.currentTimeMillis() - 86400000 // 1 day ago
                    ),
                    // Regular text messages
                    GroupMessage(
                        id = 2,
                        groupId = groupId,
                        senderId = 1,
                        senderName = "Nguyễn Văn A",
                        content = "Chào mọi người! Hôm nay chúng ta có bài tập gì không?",
                        timestamp = System.currentTimeMillis() - 7200000, // 2 hours ago
                        reactions = "❤️:2,👍:1"
                    ),
                    GroupMessage(
                        id = 3,
                        groupId = groupId,
                        senderId = 2,
                        senderName = "Trần Thị B",
                        content = "Có bài tập toán về nhà, deadline là thứ 6 này nhé! Mọi người nhớ làm đầy đủ.",
                        timestamp = System.currentTimeMillis() - 5400000, // 1.5 hours ago
                        isPinned = true,
                        pinnedAt = System.currentTimeMillis() - 5400000,
                        pinnedBy = 2L
                    ),
                    // Reply message
                    GroupMessage(
                        id = 4,
                        groupId = groupId,
                        senderId = _currentUser.value?.id ?: 3,
                        senderName = _currentUser.value?.name ?: "Bạn",
                        content = "Ok, mình sẽ làm và nộp đúng hạn. Cảm ơn bạn đã nhắc nhở!",
                        timestamp = System.currentTimeMillis() - 3600000, // 1 hour ago
                        replyToMessageId = 3L,
                        replyToContent = "Có bài tập toán về nhà, deadline là thứ 6 này nhé!",
                        replyToSenderName = "Trần Thị B"
                    ),
                    // File message
                    GroupMessage(
                        id = 5,
                        groupId = groupId,
                        senderId = 1,
                        senderName = "Nguyễn Văn A",
                        content = "Mình gửi tài liệu tham khảo cho mọi người",
                        messageType = MessageType.FILE,
                        fileUrl = "/storage/documents/tai_lieu_tham_khao.pdf",
                        fileName = "Tài liệu tham khảo - Chương 5.pdf",
                        fileSize = 2048576, // 2MB
                        timestamp = System.currentTimeMillis() - 1800000 // 30 minutes ago
                    ),
                    // Image message
                    GroupMessage(
                        id = 6,
                        groupId = groupId,
                        senderId = 2,
                        senderName = "Trần Thị B",
                        content = "Hình ảnh bài giải mẫu",
                        messageType = MessageType.IMAGE,
                        fileUrl = "/storage/images/bai_giai_mau.jpg",
                        fileName = "bai_giai_mau.jpg",
                        fileSize = 1024000, // 1MB
                        thumbnailUrl = "/storage/thumbnails/thumb_bai_giai_mau.jpg",
                        timestamp = System.currentTimeMillis() - 900000 // 15 minutes ago
                    ),
                    // Assignment sharing message
                    GroupMessage(
                        id = 7,
                        groupId = groupId,
                        senderId = _currentUser.value?.id ?: 3,
                        senderName = _currentUser.value?.name ?: "Bạn",
                        content = "📝 Bài tập: Giải phương trình bậc 2\nHạn nộp: 25/03/2024\nTrạng thái: Đang làm",
                        messageType = MessageType.TEXT,
                        timestamp = System.currentTimeMillis() - 300000, // 5 minutes ago
                        mentions = "[{\"userId\":1,\"userName\":\"Nguyễn Văn A\",\"start\":0,\"end\":13}]"
                    ),
                    // Recent message
                    GroupMessage(
                        id = 8,
                        groupId = groupId,
                        senderId = 1,
                        senderName = "Nguyễn Văn A",
                        content = "Mình vừa hoàn thành bài tập rồi. Ai cần hỗ trợ thì nhắn mình nhé! 😊",
                        timestamp = System.currentTimeMillis() - 60000, // 1 minute ago
                        reactions = "👍:3,🎉:1"
                    )
                )
                
                _messages.value = mockMessages
                
                // Load pinned messages
                _pinnedMessages.value = mockMessages.filter { it.isPinned }
                
            } catch (e: Exception) {
                _error.value = "Không thể tải tin nhắn: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun sendMessage(groupId: Long, content: String, replyToMessageId: Long? = null) {
        viewModelScope.launch {
            try {
                val currentUser = _currentUser.value ?: return@launch
                
                val newMessage = GroupMessage(
                    id = System.currentTimeMillis(), // Temporary ID
                    groupId = groupId,
                    senderId = currentUser.id,
                    senderName = currentUser.name,
                    content = content,
                    messageType = MessageType.TEXT,
                    timestamp = System.currentTimeMillis(),
                    replyToMessageId = replyToMessageId,
                    replyToContent = replyToMessageId?.let { id ->
                        _messages.value.find { it.id == id }?.content
                    },
                    replyToSenderName = replyToMessageId?.let { id ->
                        _messages.value.find { it.id == id }?.senderName
                    }
                )
                
                // Add message to local list immediately
                _messages.value = _messages.value + newMessage
                
                // TODO: Send to server via messageRepository
                
            } catch (e: Exception) {
                _error.value = "Không thể gửi tin nhắn: ${e.message}"
            }
        }
    }
    
    fun sendFileMessage(
        context: Context,
        groupId: Long,
        fileUri: Uri,
        messageContent: String = ""
    ) {
        viewModelScope.launch {
            try {
                _isUploading.value = true
                _uploadProgress.value = 0f
                
                val currentUser = _currentUser.value ?: return@launch
                val fileName = FileUtil.getFileName(context, fileUri)
                val fileSize = FileUtil.getFileSize(context, fileUri)
                val mimeType = FileUtil.getMimeType(context, fileUri)
                
                // Simulate upload progress
                for (i in 1..10) {
                    delay(200)
                    _uploadProgress.value = i / 10f
                }
                
                // Copy file to internal storage
                val filePath = FileUtil.getGroupFilePath(context, groupId, fileName)
                val copiedFile = FileUtil.copyFileToInternalStorage(context, fileUri, filePath)
                
                if (copiedFile != null) {
                    val messageType = when {
                        FileUtil.isImageFile(mimeType) -> MessageType.IMAGE
                        FileUtil.isVideoFile(mimeType) -> MessageType.VIDEO
                        FileUtil.isAudioFile(mimeType) -> MessageType.AUDIO
                        else -> MessageType.FILE
                    }
                    
                    // Create thumbnail for images
                    var thumbnailUrl: String? = null
                    if (messageType == MessageType.IMAGE) {
                        val thumbnailPath = FileUtil.getThumbnailPath(context, groupId, fileName)
                        if (FileUtil.createImageThumbnail(filePath, thumbnailPath)) {
                            thumbnailUrl = thumbnailPath
                        }
                    }
                    
                    val newMessage = GroupMessage(
                        id = System.currentTimeMillis(),
                        groupId = groupId,
                        senderId = currentUser.id,
                        senderName = currentUser.name,
                        content = messageContent.ifEmpty { 
                            when (messageType) {
                                MessageType.IMAGE -> "📷 Đã gửi hình ảnh"
                                MessageType.VIDEO -> "🎥 Đã gửi video"
                                MessageType.AUDIO -> "🎵 Đã gửi âm thanh"
                                else -> "📎 Đã gửi file"
                            }
                        },
                        messageType = messageType,
                        fileUrl = filePath,
                        fileName = fileName,
                        fileSize = fileSize,
                        thumbnailUrl = thumbnailUrl,
                        timestamp = System.currentTimeMillis()
                    )
                    
                    _messages.value = _messages.value + newMessage
                }
                
            } catch (e: Exception) {
                _error.value = "Không thể gửi file: ${e.message}"
            } finally {
                _isUploading.value = false
                _uploadProgress.value = 0f
            }
        }
    }
    
    fun sendAssignmentMessage(
        groupId: Long,
        assignmentTitle: String,
        assignmentDueDate: String?,
        assignmentStatus: String,
        assignmentId: Long
    ) {
        viewModelScope.launch {
            try {
                val currentUser = _currentUser.value ?: return@launch
                
                val content = buildString {
                    append("📝 Bài tập: $assignmentTitle")
                    assignmentDueDate?.let { append("\nHạn nộp: $it") }
                    append("\nTrạng thái: $assignmentStatus")
                    append("\n#assignment_$assignmentId")
                }
                
                val newMessage = GroupMessage(
                    id = System.currentTimeMillis(),
                    groupId = groupId,
                    senderId = currentUser.id,
                    senderName = currentUser.name,
                    content = content,
                    messageType = MessageType.TEXT,
                    timestamp = System.currentTimeMillis()
                )
                
                _messages.value = _messages.value + newMessage
                
            } catch (e: Exception) {
                _error.value = "Không thể chia sẻ bài tập: ${e.message}"
            }
        }
    }
    
    fun reactToMessage(messageId: Long, emoji: String) {
        viewModelScope.launch {
            try {
                val currentMessages = _messages.value.toMutableList()
                val messageIndex = currentMessages.indexOfFirst { it.id == messageId }
                
                if (messageIndex != -1) {
                    val message = currentMessages[messageIndex]
                    val currentUserId = _currentUser.value?.id ?: return@launch
                    
                    // Parse existing reactions (simplified JSON parsing)
                    val reactions = mutableMapOf<String, MutableSet<Long>>()
                    message.reactions?.split(",")?.forEach { reaction ->
                        val parts = reaction.split(":")
                        if (parts.size == 2) {
                            val emojiPart = parts[0]
                            val count = parts[1].toIntOrNull() ?: 0
                            reactions[emojiPart] = mutableSetOf()
                            // Add mock user IDs for count
                            repeat(count) { reactions[emojiPart]?.add(it.toLong() + 1) }
                        }
                    }
                    
                    // Toggle user's reaction
                    if (reactions[emoji]?.contains(currentUserId) == true) {
                        reactions[emoji]?.remove(currentUserId)
                        if (reactions[emoji]?.isEmpty() == true) {
                            reactions.remove(emoji)
                        }
                    } else {
                        reactions.getOrPut(emoji) { mutableSetOf() }.add(currentUserId)
                    }
                    
                    // Convert back to string format
                    val newReactions = reactions.entries.joinToString(",") { (emoji, users) ->
                        "$emoji:${users.size}"
                    }.takeIf { it.isNotEmpty() }
                    
                    currentMessages[messageIndex] = message.copy(reactions = newReactions)
                    _messages.value = currentMessages
                }
            } catch (e: Exception) {
                _error.value = "Không thể thêm reaction: ${e.message}"
            }
        }
    }
    
    fun pinMessage(messageId: Long) {
        viewModelScope.launch {
            try {
                val currentMessages = _messages.value.toMutableList()
                val messageIndex = currentMessages.indexOfFirst { it.id == messageId }
                
                if (messageIndex != -1) {
                    val message = currentMessages[messageIndex]
                    val currentUserId = _currentUser.value?.id ?: return@launch
                    
                    val updatedMessage = message.copy(
                        isPinned = !message.isPinned,
                        pinnedAt = if (!message.isPinned) System.currentTimeMillis() else null,
                        pinnedBy = if (!message.isPinned) currentUserId else null
                    )
                    
                    currentMessages[messageIndex] = updatedMessage
                    _messages.value = currentMessages
                    
                    // Update pinned messages list
                    _pinnedMessages.value = currentMessages.filter { it.isPinned }
                }
            } catch (e: Exception) {
                _error.value = "Không thể ghim tin nhắn: ${e.message}"
            }
        }
    }
    
    fun editMessage(messageId: Long, newContent: String) {
        viewModelScope.launch {
            try {
                val currentMessages = _messages.value.toMutableList()
                val messageIndex = currentMessages.indexOfFirst { it.id == messageId }
                
                if (messageIndex != -1) {
                    val message = currentMessages[messageIndex]
                    val currentUserId = _currentUser.value?.id ?: return@launch
                    
                    // Only allow editing own messages
                    if (message.senderId == currentUserId) {
                        val updatedMessage = message.copy(
                            content = newContent,
                            isEdited = true,
                            editedAt = System.currentTimeMillis()
                        )
                        
                        currentMessages[messageIndex] = updatedMessage
                        _messages.value = currentMessages
                    }
                }
            } catch (e: Exception) {
                _error.value = "Không thể chỉnh sửa tin nhắn: ${e.message}"
            }
        }
    }
    
    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            try {
                val currentUserId = _currentUser.value?.id ?: return@launch
                val messageToDelete = _messages.value.find { it.id == messageId }
                
                // Only allow deleting own messages or if user is admin
                if (messageToDelete?.senderId == currentUserId) {
                    _messages.value = _messages.value.filter { it.id != messageId }
                    _pinnedMessages.value = _pinnedMessages.value.filter { it.id != messageId }
                }
                
            } catch (e: Exception) {
                _error.value = "Không thể xóa tin nhắn: ${e.message}"
            }
        }
    }
    
    fun startTyping(userName: String) {
        viewModelScope.launch {
            val currentTyping = _typingUsers.value.toMutableList()
            if (!currentTyping.contains(userName)) {
                currentTyping.add(userName)
                _typingUsers.value = currentTyping
            }
        }
    }
    
    fun stopTyping(userName: String) {
        viewModelScope.launch {
            _typingUsers.value = _typingUsers.value.filter { it != userName }
        }
    }
    
    fun searchMessages(query: String): List<GroupMessage> {
        return _messages.value.filter { message ->
            message.content.contains(query, ignoreCase = true) ||
            message.senderName.contains(query, ignoreCase = true)
        }
    }
    
    fun getMessagesByDate(date: Long): List<GroupMessage> {
        val startOfDay = date - (date % 86400000) // Start of day
        val endOfDay = startOfDay + 86400000 // End of day
        
        return _messages.value.filter { message ->
            message.timestamp in startOfDay until endOfDay
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}