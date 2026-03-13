package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.ChatRoom
import com.example.myapplication.data.model.ChatType
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatRoomDao {
    @Query("SELECT * FROM chat_rooms WHERE groupId = :groupId AND isActive = 1 ORDER BY lastMessageTime DESC")
    fun getChatRoomsByGroup(groupId: Long): Flow<List<ChatRoom>>
    
    @Query("SELECT * FROM chat_rooms WHERE id = :chatRoomId")
    suspend fun getChatRoomById(chatRoomId: Long): ChatRoom?
    
    @Query("SELECT * FROM chat_rooms WHERE groupId = :groupId AND chatType = :chatType AND isActive = 1")
    suspend fun getChatRoomByType(groupId: Long, chatType: ChatType): ChatRoom?
    
    @Insert
    suspend fun insertChatRoom(chatRoom: ChatRoom): Long
    
    @Update
    suspend fun updateChatRoom(chatRoom: ChatRoom)
    
    @Delete
    suspend fun deleteChatRoom(chatRoom: ChatRoom)
    
    @Query("UPDATE chat_rooms SET isActive = 0 WHERE id = :chatRoomId")
    suspend fun deactivateChatRoom(chatRoomId: Long)
    
    @Query("UPDATE chat_rooms SET lastMessageId = :messageId, lastMessageTime = :timestamp WHERE id = :chatRoomId")
    suspend fun updateLastMessage(chatRoomId: Long, messageId: Long, timestamp: Long)
    
    @Query("UPDATE chat_rooms SET unreadCount = unreadCount + 1 WHERE id = :chatRoomId")
    suspend fun incrementUnreadCount(chatRoomId: Long)
    
    @Query("UPDATE chat_rooms SET unreadCount = 0 WHERE id = :chatRoomId")
    suspend fun resetUnreadCount(chatRoomId: Long)
    
    @Query("UPDATE chat_rooms SET memberCount = :count WHERE id = :chatRoomId")
    suspend fun updateMemberCount(chatRoomId: Long, count: Int)
    
    @Query("SELECT COUNT(*) FROM chat_rooms WHERE groupId = :groupId AND isActive = 1")
    suspend fun getChatRoomCount(groupId: Long): Int
}