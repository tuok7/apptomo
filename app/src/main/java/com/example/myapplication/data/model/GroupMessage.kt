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
    val messageType: String = "text", // text, file, image
    val fileUrl: String? = null,
    val fileName: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isEdited: Boolean = false,
    val editedAt: Long? = null,
    val replyToMessageId: Long? = null,
    val replyToContent: String? = null,
    val replyToSenderName: String? = null,
    val reactions: String? = null // JSON string: {"❤️": ["user1", "user2"], "👍": ["user3"]}
)
