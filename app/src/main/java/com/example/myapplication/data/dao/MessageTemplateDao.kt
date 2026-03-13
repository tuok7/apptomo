package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.MessageTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageTemplateDao {
    
    @Query("SELECT * FROM message_templates WHERE userId = :userId ORDER BY usageCount DESC, lastUsedAt DESC")
    fun getTemplatesByUser(userId: Long): Flow<List<MessageTemplate>>
    
    @Query("SELECT * FROM message_templates WHERE userId = :userId AND category = :category ORDER BY usageCount DESC")
    fun getTemplatesByCategory(userId: Long, category: String): Flow<List<MessageTemplate>>
    
    @Query("SELECT * FROM message_templates WHERE userId = :userId AND isFavorite = 1 ORDER BY lastUsedAt DESC")
    fun getFavoriteTemplates(userId: Long): Flow<List<MessageTemplate>>
    
    @Query("SELECT * FROM message_templates WHERE id = :templateId")
    suspend fun getTemplateById(templateId: Long): MessageTemplate?
    
    @Query("""
        SELECT * FROM message_templates 
        WHERE userId = :userId AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY usageCount DESC
    """)
    suspend fun searchTemplates(userId: Long, query: String): List<MessageTemplate>
    
    @Query("SELECT DISTINCT category FROM message_templates WHERE userId = :userId ORDER BY category ASC")
    suspend fun getCategoriesByUser(userId: Long): List<String>
    
    @Insert
    suspend fun insertTemplate(template: MessageTemplate): Long
    
    @Update
    suspend fun updateTemplate(template: MessageTemplate)
    
    @Delete
    suspend fun deleteTemplate(template: MessageTemplate)
    
    @Query("DELETE FROM message_templates WHERE id = :templateId")
    suspend fun deleteTemplateById(templateId: Long)
    
    @Query("""
        UPDATE message_templates 
        SET usageCount = usageCount + 1, lastUsedAt = :timestamp 
        WHERE id = :templateId
    """)
    suspend fun incrementUsageCount(templateId: Long, timestamp: Long)
    
    @Query("UPDATE message_templates SET isFavorite = :isFavorite WHERE id = :templateId")
    suspend fun updateFavoriteStatus(templateId: Long, isFavorite: Boolean)
    
    @Query("SELECT * FROM message_templates WHERE userId = :userId ORDER BY lastUsedAt DESC LIMIT :limit")
    suspend fun getRecentlyUsedTemplates(userId: Long, limit: Int = 5): List<MessageTemplate>
    
    @Query("SELECT * FROM message_templates WHERE userId = :userId ORDER BY usageCount DESC LIMIT :limit")
    suspend fun getMostUsedTemplates(userId: Long, limit: Int = 10): List<MessageTemplate>
}