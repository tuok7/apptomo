package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_members",
    foreignKeys = [
        ForeignKey(
            entity = ChatRoom::class,
            parentColumns = ["id"],
            childColumns = ["chatRoomId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["chatRoomId"]),
        Index(value = ["memberId"]),
        Index(value = ["chatRoomId", "memberId"], unique = true)
    ]
)
data class ChatMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chatRoomId: Long,
    val memberId: Long,
    val role: ChatRole = ChatRole.MEMBER,
    val joinedAt: Long = System.currentTimeMillis(),
    val lastReadMessageId: Long? = null,
    val lastReadAt: Long? = null,
    val isActive: Boolean = true,
    val isMuted: Boolean = false,
    val mutedUntil: Long? = null,
    val permissions: String? = null // JSON string for permissions
)

enum class ChatRole {
    ADMIN,
    MODERATOR,
    MEMBER,
    VIEWER
}