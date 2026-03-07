package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_messages")
data class ScheduledMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long,
    val senderId: Long,
    val senderName: String,
    val content: String,
    val messageType: String = "text",
    val fileUrl: String? = null,
    val fileName: String? = null,
    val scheduledTime: Long, // Thời gian dự kiến gửi
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "pending", // pending, sent, cancelled, failed
    val replyToMessageId: Long? = null,
    val replyToContent: String? = null,
    val replyToSenderName: String? = null
)
