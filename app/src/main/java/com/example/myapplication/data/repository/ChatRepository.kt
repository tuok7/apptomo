package com.example.myapplication.data.repository

import android.content.Context
import android.net.Uri
import com.example.myapplication.data.dao.*
import com.example.myapplication.data.model.*
import com.example.myapplication.util.FileUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ChatRepository(
    private val chatRoomDao: ChatRoomDao,
    private val chatMemberDao: ChatMemberDao,
    private val groupMessageDao: GroupMessageDao,
    private val messageReactionDao: MessageReactionDao,
    private val messageAttachmentDao: MessageAttachmentDao,
    private val chatNotificationDao: ChatNotificationDao
) {
    
    // Chat Room Operations
    fun getChatRoomsByGroup(groupId: Long): Flow<List<ChatRoom>> {
        return chatRoomDao.getChatRoomsByGroup(groupId)
    }
    
    suspend fun createChatRoom(chatRoom: ChatRoom): Long {
        return chatRoomDao.insertChatRoom(chatRoom)
    }
    
    suspend fun getChatRoomById(chatRoomId: Long): ChatRoom? {
        return chatRoomDao.getChatRoomById(chatRoomId)
    }
    
    suspend fun updateChatRoom(chatRoom: ChatRoom) {
        chatRoomDao.updateChatRoom(chatRoom)
    }
    
    // Message Operations
    fun getMessagesByGroup(groupId: Long): Flow<List<GroupMessage>> {
        return groupMessageDao.getMessagesByGroup(groupId)
    }
    
    suspend fun sendMessage(message: GroupMessage): Long {
        val messageId = groupMessageDao.insertMessage(message)
        
        // Update chat room last message
        chatRoomDao.updateLastMessage(message.groupId, messageId, message.timestamp)
        
        // Create notifications for other members
        createMessageNotifications(message.copy(id = messageId))
        
        return messageId
    }
    
    suspend fun sendFileMessage(
        context: Context,
        groupId: Long,
        senderId: Long,
        senderName: String,
        fileUri: Uri,
        messageContent: String = ""
    ): Long {
        val fileName = FileUtil.getFileName(context, fileUri)
        val fileSize = FileUtil.getFileSize(context, fileUri)
        val mimeType = FileUtil.getMimeType(context, fileUri)
        
        // Copy file to internal storage
        val filePath = FileUtil.getGroupFilePath(context, groupId, fileName)
        val copiedFile = FileUtil.copyFileToInternalStorage(context, fileUri, filePath)
            ?: throw Exception("Failed to copy file")
        
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
        
        val message = GroupMessage(
            groupId = groupId,
            senderId = senderId,
            senderName = senderName,
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
        
        val messageId = sendMessage(message)
        
        // Create attachment record
        val attachment = MessageAttachment(
            messageId = messageId,
            fileName = fileName,
            filePath = filePath,
            fileType = AttachmentType.fromMimeType(mimeType ?: ""),
            fileSize = fileSize,
            thumbnailPath = thumbnailUrl
        )
        messageAttachmentDao.insertAttachment(attachment)
        
        return messageId
    }
    
    suspend fun editMessage(messageId: Long, newContent: String) {
        groupMessageDao.editMessage(messageId, newContent, System.currentTimeMillis())
    }
    
    suspend fun deleteMessage(messageId: Long) {
        groupMessageDao.deleteMessageById(messageId)
        messageReactionDao.deleteReactionsByMessage(messageId)
        messageAttachmentDao.deleteAttachmentsByMessage(messageId)
    }
    
    // Reaction Operations
    suspend fun addReaction(messageId: Long, userId: Long, emoji: String) {
        val existingReaction = messageReactionDao.getReaction(messageId, userId, emoji)
        if (existingReaction == null) {
            val reaction = MessageReaction(
                messageId = messageId,
                userId = userId,
                emoji = emoji
            )
            messageReactionDao.insertReaction(reaction)
        }
    }
    
    suspend fun removeReaction(messageId: Long, userId: Long, emoji: String) {
        messageReactionDao.deleteReaction(messageId, userId, emoji)
    }
    
    fun getReactionsByMessage(messageId: Long): Flow<List<MessageReaction>> {
        return messageReactionDao.getReactionsByMessage(messageId)
    }
    
    // Pin Operations
    suspend fun pinMessage(messageId: Long, userId: Long) {
        val message = groupMessageDao.getMessageById(messageId)
        if (message != null) {
            val isPinned = !message.isPinned
            val pinnedAt = if (isPinned) System.currentTimeMillis() else null
            val pinnedBy = if (isPinned) userId else null
            
            groupMessageDao.updatePinStatus(messageId, isPinned, pinnedAt, pinnedBy)
        }
    }
    
    fun getPinnedMessages(groupId: Long): Flow<List<GroupMessage>> {
        return groupMessageDao.getPinnedMessages(groupId)
    }
    
    // Search Operations
    suspend fun searchMessages(groupId: Long, query: String): List<GroupMessage> {
        return groupMessageDao.searchMessages(groupId, query)
    }
    
    // Member Operations
    fun getChatMembers(chatRoomId: Long): Flow<List<ChatMember>> {
        return chatMemberDao.getMembersByChatRoom(chatRoomId)
    }
    
    suspend fun addMemberToChat(chatMember: ChatMember): Long {
        val memberId = chatMemberDao.insertChatMember(chatMember)
        
        // Update member count
        val memberCount = chatMemberDao.getMemberCount(chatMember.chatRoomId)
        chatRoomDao.updateMemberCount(chatMember.chatRoomId, memberCount)
        
        return memberId
    }
    
    suspend fun removeMemberFromChat(chatRoomId: Long, memberId: Long) {
        chatMemberDao.removeMemberFromChat(chatRoomId, memberId)
        
        // Update member count
        val memberCount = chatMemberDao.getMemberCount(chatRoomId)
        chatRoomDao.updateMemberCount(chatRoomId, memberCount)
    }
    
    suspend fun updateMemberRole(chatRoomId: Long, memberId: Long, role: ChatRole) {
        chatMemberDao.updateMemberRole(chatRoomId, memberId, role)
    }
    
    // Notification Operations
    fun getNotificationsByUser(userId: Long): Flow<List<ChatNotification>> {
        return chatNotificationDao.getNotificationsByUser(userId)
    }
    
    fun getUnreadNotifications(userId: Long): Flow<List<ChatNotification>> {
        return chatNotificationDao.getUnreadNotifications(userId)
    }
    
    suspend fun markNotificationAsRead(notificationId: Long) {
        chatNotificationDao.markAsRead(notificationId)
    }
    
    suspend fun markChatNotificationsAsRead(userId: Long, chatRoomId: Long) {
        chatNotificationDao.markChatNotificationsAsRead(userId, chatRoomId)
    }
    
    // Attachment Operations
    fun getAttachmentsByMessage(messageId: Long): Flow<List<MessageAttachment>> {
        return messageAttachmentDao.getAttachmentsByMessage(messageId)
    }
    
    suspend fun getGroupAttachmentsByType(groupId: Long, type: AttachmentType, limit: Int = 50): List<MessageAttachment> {
        return messageAttachmentDao.getGroupAttachmentsByType(groupId, type, limit)
    }
    
    // Private helper methods
    private suspend fun createMessageNotifications(message: GroupMessage) {
        // Get all chat members except sender
        val chatRoom = chatRoomDao.getChatRoomByType(message.groupId, ChatType.GROUP)
        if (chatRoom != null) {
            val members = chatMemberDao.getMembersByChatRoom(chatRoom.id)
                .collect { memberList ->
                    memberList.filter { it.memberId != message.senderId && it.isActive }
                        .forEach { member ->
                            val notification = ChatNotification(
                                userId = member.memberId,
                                chatRoomId = chatRoom.id,
                                messageId = message.id,
                                type = if (message.mentions?.contains(member.memberId.toString()) == true) {
                                    NotificationType.MENTION
                                } else {
                                    NotificationType.NEW_MESSAGE
                                },
                                title = "Tin nhắn mới từ ${message.senderName}",
                                content = message.content.take(100)
                            )
                            chatNotificationDao.insertNotification(notification)
                        }
                }
        }
    }
    
    // Assignment Integration
    suspend fun createAssignmentChat(assignmentId: Long, groupId: Long): Long {
        // Create chat room for assignment
        val chatRoom = ChatRoom(
            groupId = groupId,
            name = "Thảo luận bài tập",
            description = "Chat cho bài tập #$assignmentId",
            chatType = ChatType.ASSIGNMENT,
            createdBy = 0 // System created
        )
        
        val chatRoomId = chatRoomDao.insertChatRoom(chatRoom)
        
        // Link assignment to chat
        val assignmentChat = AssignmentChat(
            assignmentId = assignmentId,
            chatRoomId = chatRoomId
        )
        
        return chatRoomId
    }
    
    suspend fun getAssignmentChat(assignmentId: Long): ChatRoom? {
        // Implementation would depend on having AssignmentChatDao
        return null
    }
}