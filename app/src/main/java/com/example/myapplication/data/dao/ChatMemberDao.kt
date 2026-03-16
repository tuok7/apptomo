package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.ChatMember
import com.example.myapplication.data.model.ChatRole
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMemberDao {
    @Query("SELECT * FROM chat_members WHERE chatRoomId = :chatRoomId AND isActive = 1 ORDER BY joinedAt ASC")
    fun getMembersByChatRoom(chatRoomId: Long): Flow<List<ChatMember>>
    
    @Query("SELECT * FROM chat_members WHERE memberId = :memberId AND isActive = 1")
    fun getChatRoomsByMember(memberId: Long): Flow<List<ChatMember>>
    
    @Query("SELECT * FROM chat_members WHERE chatRoomId = :chatRoomId AND memberId = :memberId")
    suspend fun getChatMember(chatRoomId: Long, memberId: Long): ChatMember?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMember(chatMember: ChatMember): Long
    
    @Update
    suspend fun updateChatMember(chatMember: ChatMember)
    
    @Delete
    suspend fun deleteChatMember(chatMember: ChatMember)
    
    @Query("UPDATE chat_members SET isActive = 0 WHERE chatRoomId = :chatRoomId AND memberId = :memberId")
    suspend fun removeMemberFromChat(chatRoomId: Long, memberId: Long)
    
    @Query("UPDATE chat_members SET role = :role WHERE chatRoomId = :chatRoomId AND memberId = :memberId")
    suspend fun updateMemberRole(chatRoomId: Long, memberId: Long, role: ChatRole)
    
    @Query("UPDATE chat_members SET lastReadMessageId = :messageId, lastReadAt = :timestamp WHERE chatRoomId = :chatRoomId AND memberId = :memberId")
    suspend fun updateLastRead(chatRoomId: Long, memberId: Long, messageId: Long, timestamp: Long)
    
    @Query("UPDATE chat_members SET isMuted = :isMuted, mutedUntil = :mutedUntil WHERE chatRoomId = :chatRoomId AND memberId = :memberId")
    suspend fun updateMuteStatus(chatRoomId: Long, memberId: Long, isMuted: Boolean, mutedUntil: Long?)
    
    @Query("SELECT COUNT(*) FROM chat_members WHERE chatRoomId = :chatRoomId AND isActive = 1")
    suspend fun getMemberCount(chatRoomId: Long): Int
    
    @Query("SELECT * FROM chat_members WHERE chatRoomId = :chatRoomId AND role IN (:roles) AND isActive = 1")
    suspend fun getMembersByRole(chatRoomId: Long, roles: List<ChatRole>): List<ChatMember>
    
    @Query("""
        SELECT * FROM chat_members 
        WHERE chatRoomId = :chatRoomId AND isActive = 1
    """)
    suspend fun searchMembers(chatRoomId: Long): List<ChatMember>
}