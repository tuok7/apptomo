package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_notifications",
    foreignKeys = [
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GroupMessage::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ChatRoom::class,
            parentColumns = ["id"],
            childColumns = ["chatRoomId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["messageId"]),
        Index(value = ["chatRoomId"])
    ]
)
data class ChatNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val chatRoomId: Long,
    val messageId: Long? = null,
    val type: NotificationType,
    val title: String,
    val content: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val readAt: Long? = null,
    val actionData: String? = null // JSON for action data
)

enum class NotificationType {
    NEW_MESSAGE,
    MENTION,
    ASSIGNMENT_DUE,
    ASSIGNMENT_SUBMITTED,
    ASSIGNMENT_GRADED,
    MEMBER_JOINED,
    MEMBER_LEFT,
    CHAT_CREATED,
    SYSTEM_ANNOUNCEMENT
}