package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "message_threads",
    foreignKeys = [
        ForeignKey(
            entity = GroupMessage::class,
            parentColumns = ["id"],
            childColumns = ["parentMessageId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GroupMessage::class,
            parentColumns = ["id"],
            childColumns = ["replyMessageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["parentMessageId"]),
        Index(value = ["replyMessageId"])
    ]
)
data class MessageThread(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val parentMessageId: Long,
    val replyMessageId: Long,
    val createdAt: Long = System.currentTimeMillis()
)