package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "assignment_chats",
    foreignKeys = [
        ForeignKey(
            entity = Assignment::class,
            parentColumns = ["id"],
            childColumns = ["assignmentId"],
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
        Index(value = ["assignmentId"]),
        Index(value = ["chatRoomId"]),
        Index(value = ["assignmentId", "chatRoomId"], unique = true)
    ]
)
data class AssignmentChat(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val assignmentId: Long,
    val chatRoomId: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val autoCreateSubmissionReminders: Boolean = true,
    val reminderSettings: String? = null // JSON for reminder settings
)