package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.MessageReaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageReactionDao {
    @Query("SELECT * FROM message_reactions WHERE messageId = :messageId ORDER BY createdAt ASC")
    fun getReactionsByMessage(messageId: Long): Flow<List<MessageReaction>>
    
    @Query("SELECT * FROM message_reactions WHERE messageId = :messageId AND userId = :userId AND emoji = :emoji")
    suspend fun getReaction(messageId: Long, userId: Long, emoji: String): MessageReaction?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReaction(reaction: MessageReaction): Long
    
    @Delete
    suspend fun deleteReaction(reaction: MessageReaction)
    
    @Query("DELETE FROM message_reactions WHERE messageId = :messageId AND userId = :userId AND emoji = :emoji")
    suspend fun deleteReaction(messageId: Long, userId: Long, emoji: String)
    
    @Query("DELETE FROM message_reactions WHERE messageId = :messageId")
    suspend fun deleteReactionsByMessage(messageId: Long)
    
    @Query("""
        SELECT emoji, COUNT(*) as count 
        FROM message_reactions 
        WHERE messageId = :messageId 
        GROUP BY emoji 
        ORDER BY count DESC
    """)
    suspend fun getReactionCounts(messageId: Long): List<ReactionCount>
    
    @Query("SELECT COUNT(*) FROM message_reactions WHERE messageId = :messageId")
    suspend fun getTotalReactionCount(messageId: Long): Int
}

data class ReactionCount(
    val emoji: String,
    val count: Int
)