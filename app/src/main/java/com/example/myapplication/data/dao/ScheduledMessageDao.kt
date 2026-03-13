package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.ScheduledMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledMessageDao {
    
    @Query("SELECT * FROM scheduled_messages WHERE groupId = :groupId ORDER BY scheduledTime ASC")
    fun getScheduledMessagesByGroup(groupId: Long): Flow<List<ScheduledMessage>>
    
    @Query("SELECT * FROM scheduled_messages WHERE senderId = :senderId ORDER BY scheduledTime ASC")
    fun getScheduledMessagesByUser(senderId: Long): Flow<List<ScheduledMessage>>
    
    @Query("SELECT * FROM scheduled_messages WHERE status = :status ORDER BY scheduledTime ASC")
    fun getScheduledMessagesByStatus(status: String): Flow<List<ScheduledMessage>>
    
    @Query("SELECT * FROM scheduled_messages WHERE id = :messageId")
    suspend fun getScheduledMessageById(messageId: Long): ScheduledMessage?
    
    @Query("""
        SELECT * FROM scheduled_messages 
        WHERE status = 'pending' AND scheduledTime <= :currentTime 
        ORDER BY scheduledTime ASC
    """)
    suspend fun getPendingMessagesToSend(currentTime: Long): List<ScheduledMessage>
    
    @Insert
    suspend fun insertScheduledMessage(scheduledMessage: ScheduledMessage): Long
    
    @Update
    suspend fun updateScheduledMessage(scheduledMessage: ScheduledMessage)
    
    @Delete
    suspend fun deleteScheduledMessage(scheduledMessage: ScheduledMessage)
    
    @Query("DELETE FROM scheduled_messages WHERE id = :messageId")
    suspend fun deleteScheduledMessageById(messageId: Long)
    
    @Query("UPDATE scheduled_messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: Long, status: String)
    
    @Query("UPDATE scheduled_messages SET status = 'cancelled' WHERE id = :messageId")
    suspend fun cancelScheduledMessage(messageId: Long)
    
    @Query("SELECT COUNT(*) FROM scheduled_messages WHERE groupId = :groupId AND status = 'pending'")
    suspend fun getPendingMessageCount(groupId: Long): Int
    
    @Query("SELECT COUNT(*) FROM scheduled_messages WHERE senderId = :senderId AND status = 'pending'")
    suspend fun getPendingMessageCountByUser(senderId: Long): Int
    
    @Query("""
        DELETE FROM scheduled_messages 
        WHERE status IN ('sent', 'failed', 'cancelled') AND createdAt < :cutoffTime
    """)
    suspend fun cleanupOldMessages(cutoffTime: Long)
}