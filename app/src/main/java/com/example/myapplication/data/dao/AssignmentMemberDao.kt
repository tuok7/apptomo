package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.AssignmentMember
import com.example.myapplication.data.model.Member
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentMemberDao {
    // Không query trực tiếp từ members table vì không tồn tại
    // Sử dụng API để lấy thông tin members
    @Query("SELECT * FROM assignment_members WHERE assignmentId = :assignmentId")
    fun getAssignmentMembers(assignmentId: Long): Flow<List<AssignmentMember>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun assignMember(assignmentMember: AssignmentMember)
    
    @Delete
    suspend fun unassignMember(assignmentMember: AssignmentMember)
    
    @Query("DELETE FROM assignment_members WHERE assignmentId = :assignmentId")
    suspend fun clearAssignmentMembers(assignmentId: Long)
}
