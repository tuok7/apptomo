package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.Poll
import kotlinx.coroutines.flow.Flow

@Dao
interface PollDao {
    
    @Query("SELECT * FROM polls WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun getPollsByGroup(groupId: Long): Flow<List<Poll>>
    
    @Query("SELECT * FROM polls WHERE id = :pollId")
    suspend fun getPollById(pollId: Long): Poll?
    
    @Query("SELECT * FROM polls WHERE messageId = :messageId")
    suspend fun getPollByMessageId(messageId: Long): Poll?
    
    @Query("SELECT * FROM polls WHERE groupId = :groupId AND isClosed = 0 ORDER BY createdAt DESC")
    fun getActivePolls(groupId: Long): Flow<List<Poll>>
    
    @Query("SELECT * FROM polls WHERE createdBy = :userId ORDER BY createdAt DESC")
    fun getPollsByUser(userId: Long): Flow<List<Poll>>
    
    @Insert
    suspend fun insertPoll(poll: Poll): Long
    
    @Update
    suspend fun updatePoll(poll: Poll)
    
    @Delete
    suspend fun deletePoll(poll: Poll)
    
    @Query("DELETE FROM polls WHERE id = :pollId")
    suspend fun deletePollById(pollId: Long)
    
    @Query("UPDATE polls SET votes = :votes WHERE id = :pollId")
    suspend fun updatePollVotes(pollId: Long, votes: String)
    
    @Query("UPDATE polls SET isClosed = 1 WHERE id = :pollId")
    suspend fun closePoll(pollId: Long)
    
    @Query("UPDATE polls SET isClosed = 0 WHERE id = :pollId")
    suspend fun reopenPoll(pollId: Long)
    
    @Query("""
        SELECT * FROM polls 
        WHERE groupId = :groupId AND expiresAt IS NOT NULL AND expiresAt < :currentTime AND isClosed = 0
    """)
    suspend fun getExpiredPolls(groupId: Long, currentTime: Long): List<Poll>
    
    @Query("SELECT COUNT(*) FROM polls WHERE groupId = :groupId AND createdBy = :userId")
    suspend fun getPollCountByUser(groupId: Long, userId: Long): Int
}