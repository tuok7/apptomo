package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.Member
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members WHERE groupId = :groupId")
    fun getMembersByGroup(groupId: Long): Flow<List<Member>>
    
    @Query("SELECT * FROM members WHERE id = :memberId")
    fun getMemberById(memberId: Long): Flow<Member?>
    
    @Query("SELECT * FROM members WHERE groupId = :groupId AND isOnline = 1")
    fun getOnlineMembersByGroup(groupId: Long): Flow<List<Member>>
    
    @Query("UPDATE members SET isOnline = :isOnline, lastActivity = :lastActivity WHERE id = :memberId")
    suspend fun updateMemberOnlineStatus(memberId: Long, isOnline: Boolean, lastActivity: Long = System.currentTimeMillis())
    
    @Query("UPDATE members SET lastSeen = :lastSeen WHERE id = :memberId")
    suspend fun updateMemberLastSeen(memberId: Long, lastSeen: Long = System.currentTimeMillis())
    
    @Query("UPDATE members SET isOnline = 0, lastSeen = :lastSeen WHERE id = :memberId")
    suspend fun setMemberOffline(memberId: Long, lastSeen: Long = System.currentTimeMillis())
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member): Long
    
    @Update
    suspend fun updateMember(member: Member)
    
    @Delete
    suspend fun deleteMember(member: Member)
}
