package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.GroupMessage
import com.example.myapplication.data.model.MessageType
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupMessageDao {
    @Query("SELECT * FROM group_messages WHERE groupId = :groupId ORDER BY timestamp ASC")
    fun getMessagesByGroup(groupId: Long): Flow<List<GroupMessage>>
    
    @Query("SELECT * FROM group_messages WHERE groupId = :groupId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getMessagesPaginated(groupId: Long, limit: Int, offset: Int): List<GroupMessage>
    
    @Query("SELECT * FROM group_messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: Long): GroupMessage?
    
    @Insert
    suspend fun insertMessage(message: GroupMessage): Long
    
    @Insert
    suspend fun insertMessages(messages: List<GroupMessage>): List<Long>
    
    @Update
    suspend fun updateMessage(message: GroupMessage)
    
    @Delete
    suspend fun deleteMessage(message: GroupMessage)
    
    @Query("DELETE FROM group_messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: Long)
    
    @Query("DELETE FROM group_messages WHERE groupId = :groupId")
    suspend fun deleteMessagesByGroup(groupId: Long)
    
    @Query("SELECT * FROM group_messages WHERE groupId = :groupId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessage(groupId: Long): GroupMessage?
    
    @Query("UPDATE group_messages SET content = :newContent, isEdited = 1, editedAt = :editedAt WHERE id = :messageId")
    suspend fun editMessage(messageId: Long, newContent: String, editedAt: Long)
    
    @Query("UPDATE group_messages SET reactions = :reactions WHERE id = :messageId")
    suspend fun updateReactions(messageId: Long, reactions: String?)
    
    @Query("UPDATE group_messages SET isPinned = :isPinned, pinnedAt = :pinnedAt, pinnedBy = :pinnedBy WHERE id = :messageId")
    suspend fun updatePinStatus(messageId: Long, isPinned: Boolean, pinnedAt: Long?, pinnedBy: Long?)
    
    @Query("SELECT * FROM group_messages WHERE groupId = :groupId AND isPinned = 1 ORDER BY pinnedAt DESC")
    fun getPinnedMessages(groupId: Long): Flow<List<GroupMessage>>
    
    @Query("SELECT * FROM group_messages WHERE groupId = :groupId AND messageType = :messageType ORDER BY timestamp DESC")
    fun getMessagesByType(groupId: Long, messageType: MessageType): Flow<List<GroupMessage>>
    
    @Query("SELECT * FROM group_messages WHERE senderId = :senderId AND groupId = :groupId ORDER BY timestamp DESC")
    fun getMessagesBySender(groupId: Long, senderId: Long): Flow<List<GroupMessage>>
    
    @Query("""
        SELECT * FROM group_messages 
        WHERE groupId = :groupId 
        AND (content LIKE '%' || :query || '%' OR senderName LIKE '%' || :query || '%')
        ORDER BY timestamp DESC
    """)
    suspend fun searchMessages(groupId: Long, query: String): List<GroupMessage>
    
    @Query("SELECT * FROM group_messages WHERE replyToMessageId = :parentMessageId ORDER BY timestamp ASC")
    suspend fun getReplies(parentMessageId: Long): List<GroupMessage>
    
    @Query("SELECT COUNT(*) FROM group_messages WHERE groupId = :groupId")
    suspend fun getMessageCount(groupId: Long): Int
    
    @Query("SELECT COUNT(*) FROM group_messages WHERE groupId = :groupId AND senderId = :senderId")
    suspend fun getMessageCountBySender(groupId: Long, senderId: Long): Int
    
    @Query("""
        SELECT * FROM group_messages 
        WHERE groupId = :groupId 
        AND timestamp BETWEEN :startTime AND :endTime 
        ORDER BY timestamp ASC
    """)
    suspend fun getMessagesByTimeRange(groupId: Long, startTime: Long, endTime: Long): List<GroupMessage>
    
    @Query("SELECT * FROM group_messages WHERE groupId = :groupId AND mentions LIKE '%' || :userId || '%' ORDER BY timestamp DESC")
    suspend fun getMentionedMessages(groupId: Long, userId: Long): List<GroupMessage>
    
    @Query("SELECT DISTINCT DATE(timestamp/1000, 'unixepoch') as date FROM group_messages WHERE groupId = :groupId ORDER BY date DESC")
    suspend fun getMessageDates(groupId: Long): List<String>
    
    @Query("UPDATE group_messages SET fileUrl = :newUrl WHERE id = :messageId")
    suspend fun updateFileUrl(messageId: Long, newUrl: String)
}
