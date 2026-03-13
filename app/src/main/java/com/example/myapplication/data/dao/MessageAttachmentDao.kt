package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.MessageAttachment
import com.example.myapplication.data.model.AttachmentType
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageAttachmentDao {
    @Query("SELECT * FROM message_attachments WHERE messageId = :messageId ORDER BY createdAt ASC")
    fun getAttachmentsByMessage(messageId: Long): Flow<List<MessageAttachment>>
    
    @Query("SELECT * FROM message_attachments WHERE id = :attachmentId")
    suspend fun getAttachmentById(attachmentId: Long): MessageAttachment?
    
    @Insert
    suspend fun insertAttachment(attachment: MessageAttachment): Long
    
    @Insert
    suspend fun insertAttachments(attachments: List<MessageAttachment>): List<Long>
    
    @Update
    suspend fun updateAttachment(attachment: MessageAttachment)
    
    @Delete
    suspend fun deleteAttachment(attachment: MessageAttachment)
    
    @Query("DELETE FROM message_attachments WHERE messageId = :messageId")
    suspend fun deleteAttachmentsByMessage(messageId: Long)
    
    @Query("SELECT * FROM message_attachments WHERE fileType = :type ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getAttachmentsByType(type: AttachmentType, limit: Int = 50): List<MessageAttachment>
    
    @Query("""
        SELECT ma.* FROM message_attachments ma
        INNER JOIN group_messages gm ON ma.messageId = gm.id
        WHERE gm.groupId = :groupId AND ma.fileType = :type
        ORDER BY ma.createdAt DESC
        LIMIT :limit
    """)
    suspend fun getGroupAttachmentsByType(groupId: Long, type: AttachmentType, limit: Int = 50): List<MessageAttachment>
    
    @Query("SELECT COUNT(*) FROM message_attachments WHERE messageId = :messageId")
    suspend fun getAttachmentCount(messageId: Long): Int
    
    @Query("SELECT SUM(fileSize) FROM message_attachments WHERE messageId = :messageId")
    suspend fun getTotalFileSize(messageId: Long): Long?
}