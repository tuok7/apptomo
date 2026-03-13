package com.example.myapplication.data.database

import androidx.room.TypeConverter
import com.example.myapplication.data.model.*

class Converters {
    @TypeConverter
    fun fromAssignmentStatus(value: AssignmentStatus): String {
        return value.name
    }
    
    @TypeConverter
    fun toAssignmentStatus(value: String): AssignmentStatus {
        return AssignmentStatus.valueOf(value)
    }
    
    @TypeConverter
    fun fromPriority(value: Priority): String {
        return value.name
    }
    
    @TypeConverter
    fun toPriority(value: String): Priority {
        return Priority.valueOf(value)
    }
    
    // New converters for chat features
    @TypeConverter
    fun fromMessageType(value: MessageType): String {
        return value.name
    }
    
    @TypeConverter
    fun toMessageType(value: String): MessageType {
        return MessageType.valueOf(value)
    }
    
    @TypeConverter
    fun fromChatType(value: ChatType): String {
        return value.name
    }
    
    @TypeConverter
    fun toChatType(value: String): ChatType {
        return ChatType.valueOf(value)
    }
    
    @TypeConverter
    fun fromChatRole(value: ChatRole): String {
        return value.name
    }
    
    @TypeConverter
    fun toChatRole(value: String): ChatRole {
        return ChatRole.valueOf(value)
    }
    
    @TypeConverter
    fun fromAttachmentType(value: AttachmentType): String {
        return value.name
    }
    
    @TypeConverter
    fun toAttachmentType(value: String): AttachmentType {
        return AttachmentType.valueOf(value)
    }
    
    @TypeConverter
    fun fromNotificationType(value: NotificationType): String {
        return value.name
    }
    
    @TypeConverter
    fun toNotificationType(value: String): NotificationType {
        return NotificationType.valueOf(value)
    }
    
    // New converters for document features
    @TypeConverter
    fun fromFileType(value: FileType): String {
        return value.name
    }
    
    @TypeConverter
    fun toFileType(value: String): FileType {
        return FileType.valueOf(value)
    }
}
