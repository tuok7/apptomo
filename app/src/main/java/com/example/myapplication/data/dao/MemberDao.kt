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
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member): Long
    
    @Update
    suspend fun updateMember(member: Member)
    
    @Delete
    suspend fun deleteMember(member: Member)
}
