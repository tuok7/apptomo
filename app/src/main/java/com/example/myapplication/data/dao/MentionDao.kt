package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.Mention
import com.example.myapplication.data.model.LinkPreview
import kotlinx.coroutines.flow.Flow

@Dao
interface MentionDao {
    
    // Mention operations
    @Query("SELECT * FROM mentions WHERE messageId = :messageId ORDER BY startIndex ASC")
    suspend fun getMentionsByMessage(messageId: Long): List<Mention>
    
    @Query("SELECT * FROM mentions WHERE userId = :userId AND isRead = 0 ORDER BY createdAt DESC")
    fun getUnreadMentions(userId: Long): Flow<List<Mention>>
    
    @Query("SELECT * FROM mentions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getMentionsByUser(userId: Long): Flow<List<Mention>>
    
    @Insert
    suspend fun insertMention(mention: Mention): Long
    
    @Insert
    suspend fun insertMentions(mentions: List<Mention>)
    
    @Update
    suspend fun updateMention(mention: Mention)
    
    @Delete
    suspend fun deleteMention(mention: Mention)
    
    @Query("DELETE FROM mentions WHERE messageId = :messageId")
    suspend fun deleteMentionsByMessage(messageId: Long)
    
    @Query("UPDATE mentions SET isRead = 1 WHERE userId = :userId AND messageId = :messageId")
    suspend fun markMentionAsRead(userId: Long, messageId: Long)
    
    @Query("UPDATE mentions SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllMentionsAsRead(userId: Long)
    
    @Query("SELECT COUNT(*) FROM mentions WHERE userId = :userId AND isRead = 0")
    suspend fun getUnreadMentionCount(userId: Long): Int
    
    // Link Preview operations
    @Query("SELECT * FROM link_previews WHERE messageId = :messageId")
    suspend fun getLinkPreviewsByMessage(messageId: Long): List<LinkPreview>
    
    @Query("SELECT * FROM link_previews WHERE url = :url LIMIT 1")
    suspend fun getLinkPreviewByUrl(url: String): LinkPreview?
    
    @Insert
    suspend fun insertLinkPreview(linkPreview: LinkPreview): Long
    
    @Insert
    suspend fun insertLinkPreviews(linkPreviews: List<LinkPreview>)
    
    @Update
    suspend fun updateLinkPreview(linkPreview: LinkPreview)
    
    @Delete
    suspend fun deleteLinkPreview(linkPreview: LinkPreview)
    
    @Query("DELETE FROM link_previews WHERE messageId = :messageId")
    suspend fun deleteLinkPreviewsByMessage(messageId: Long)
}