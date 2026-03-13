package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "message_attachments",
    foreignKeys = [
        ForeignKey(
            entity = GroupMessage::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("messageId")]
)
data class MessageAttachment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val messageId: Long,
    val fileName: String,
    val filePath: String,
    val fileType: AttachmentType,
    val fileSize: Long,
    val thumbnailPath: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val duration: Int? = null, // For video/audio in seconds
    val createdAt: Long = System.currentTimeMillis()
)

enum class AttachmentType {
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
    OTHER;
    
    companion object {
        fun fromMimeType(mimeType: String): AttachmentType {
            return when {
                mimeType.startsWith("image/") -> IMAGE
                mimeType.startsWith("video/") -> VIDEO
                mimeType.startsWith("audio/") -> AUDIO
                mimeType.contains("pdf") || 
                mimeType.contains("document") || 
                mimeType.contains("spreadsheet") ||
                mimeType.contains("presentation") -> DOCUMENT
                else -> OTHER
            }
        }
    }
}

@Entity(
    tableName = "pinned_messages",
    foreignKeys = [
        ForeignKey(
            entity = GroupMessage::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupId"), Index("messageId")]
)
data class PinnedMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long,
    val messageId: Long,
    val pinnedBy: Long,
    val pinnedByName: String,
    val pinnedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "message_mentions")
data class MessageMention(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val messageId: Long,
    val userId: Long,
    val userName: String,
    val startIndex: Int,
    val endIndex: Int
)
