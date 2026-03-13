package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_rooms")
data class ChatRoom(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long,
    val name: String,
    val description: String? = null,
    val avatarUrl: String? = null,
    val createdBy: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val lastMessageId: Long? = null,
    val lastMessageTime: Long? = null,
    val unreadCount: Int = 0,
    val memberCount: Int = 0,
    val chatType: ChatType = ChatType.GROUP,
    val settings: String? = null // JSON string for chat settings
)

enum class ChatType {
    GROUP,
    ASSIGNMENT,
    STUDY_SESSION,
    ANNOUNCEMENT
}