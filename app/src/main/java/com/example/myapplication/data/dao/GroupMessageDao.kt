package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.GroupMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupMessageDao {
    @Query("SELECT * FROM group_messages WHERE groupId = :groupId ORDER BY timestamp ASC")
    fun getMessagesByGroup(groupId: Long): Flow<List<GroupMessage>>
    
    @Query("SELECT * FROM group_messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: Long): GroupMessage?
    
    @Insert
    suspend fun insertMessage(message: GroupMessage): Long
    
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
}
