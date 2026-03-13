package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.ChatNotification
import com.example.myapplication.data.model.NotificationType
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatNotificationDao {
    @Query("SELECT * FROM chat_notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsByUser(userId: Long): Flow<List<ChatNotification>>
    
    @Query("SELECT * FROM chat_notifications WHERE userId = :userId AND isRead = 0 ORDER BY createdAt DESC")
    fun getUnreadNotifications(userId: Long): Flow<List<ChatNotification>>
    
    @Query("SELECT * FROM chat_notifications WHERE chatRoomId = :chatRoomId ORDER BY createdAt DESC")
    fun getNotificationsByChatRoom(chatRoomId: Long): Flow<List<ChatNotification>>
    
    @Query("SELECT * FROM chat_notifications WHERE id = :notificationId")
    suspend fun getNotificationById(notificationId: Long): ChatNotification?
    
    @Insert
    suspend fun insertNotification(notification: ChatNotification): Long
    
    @Insert
    suspend fun insertNotifications(notifications: List<ChatNotification>): List<Long>
    
    @Update
    suspend fun updateNotification(notification: ChatNotification)
    
    @Delete
    suspend fun deleteNotification(notification: ChatNotification)
    
    @Query("UPDATE chat_notifications SET isRead = 1, readAt = :readAt WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: Long, readAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE chat_notifications SET isRead = 1, readAt = :readAt WHERE userId = :userId AND chatRoomId = :chatRoomId")
    suspend fun markChatNotificationsAsRead(userId: Long, chatRoomId: Long, readAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE chat_notifications SET isRead = 1, readAt = :readAt WHERE userId = :userId")
    suspend fun markAllAsRead(userId: Long, readAt: Long = System.currentTimeMillis())
    
    @Query("SELECT COUNT(*) FROM chat_notifications WHERE userId = :userId AND isRead = 0")
    suspend fun getUnreadCount(userId: Long): Int
    
    @Query("SELECT COUNT(*) FROM chat_notifications WHERE userId = :userId AND chatRoomId = :chatRoomId AND isRead = 0")
    suspend fun getUnreadCountByChatRoom(userId: Long, chatRoomId: Long): Int
    
    @Query("DELETE FROM chat_notifications WHERE userId = :userId AND createdAt < :beforeTimestamp")
    suspend fun deleteOldNotifications(userId: Long, beforeTimestamp: Long)
    
    @Query("SELECT * FROM chat_notifications WHERE userId = :userId AND type = :type ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getNotificationsByType(userId: Long, type: NotificationType, limit: Int = 50): List<ChatNotification>
}