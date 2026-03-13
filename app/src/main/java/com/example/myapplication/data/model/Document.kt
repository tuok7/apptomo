package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "documents",
    foreignKeys = [
        ForeignKey(
            entity = Group::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupId"), Index("folderId")]
)
data class Document(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long,
    val title: String,
    val description: String = "",
    val fileName: String,
    val filePath: String,
    val fileType: String,
    val fileSize: Long,
    val folderId: Long? = null,
    val uploadedBy: Long,
    val uploaderName: String = "",
    val downloadCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "document_folders",
    foreignKeys = [
        ForeignKey(
            entity = Group::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupId"), Index("parentId")]
)
data class DocumentFolder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long,
    val name: String,
    val parentId: Long? = null,
    val createdBy: Long,
    val createdAt: Long = System.currentTimeMillis()
)

enum class FileType {
    PDF, WORD, EXCEL, POWERPOINT, IMAGE, VIDEO, AUDIO, OTHER;
    
    companion object {
        fun fromMimeType(mimeType: String): FileType {
            return when {
                mimeType.contains("pdf") -> PDF
                mimeType.contains("word") || mimeType.contains("document") -> WORD
                mimeType.contains("excel") || mimeType.contains("spreadsheet") -> EXCEL
                mimeType.contains("powerpoint") || mimeType.contains("presentation") -> POWERPOINT
                mimeType.startsWith("image/") -> IMAGE
                mimeType.startsWith("video/") -> VIDEO
                mimeType.startsWith("audio/") -> AUDIO
                else -> OTHER
            }
        }
        
        fun fromExtension(extension: String): FileType {
            return when (extension.lowercase()) {
                "pdf" -> PDF
                "doc", "docx" -> WORD
                "xls", "xlsx" -> EXCEL
                "ppt", "pptx" -> POWERPOINT
                "jpg", "jpeg", "png", "gif", "bmp" -> IMAGE
                "mp4", "avi", "mkv", "mov" -> VIDEO
                "mp3", "wav", "m4a" -> AUDIO
                else -> OTHER
            }
        }
    }
}
