package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "group_messages")
data class GroupMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long,
    val senderId: Long,
    val senderName: String,
    val content: String,
    val messageType: MessageType = MessageType.TEXT,
    val fileUrl: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val thumbnailUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isEdited: Boolean = false,
    val editedAt: Long? = null,
    val replyToMessageId: Long? = null,
    val replyToContent: String? = null,
    val replyToSenderName: String? = null,
    val reactions: String? = null, // JSON string: {"❤️": [1, 2], "👍": [3]}
    val mentions: String? = null, // JSON array: [{"userId": 1, "userName": "Name", "start": 0, "end": 5}]
    val isPinned: Boolean = false,
    val pinnedAt: Long? = null,
    val pinnedBy: Long? = null
)

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    FILE,
    SYSTEM // For system messages like "User joined", "User left"
}
