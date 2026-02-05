package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.AssignmentMember
import com.example.myapplication.data.model.Member
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentMemberDao {
    @Query("""
        SELECT m.* FROM members m
        INNER JOIN assignment_members am ON m.id = am.memberId
        WHERE am.assignmentId = :assignmentId
    """)
    fun getMembersForAssignment(assignmentId: Long): Flow<List<Member>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun assignMember(assignmentMember: AssignmentMember)
    
    @Delete
    suspend fun unassignMember(assignmentMember: AssignmentMember)
    
    @Query("DELETE FROM assignment_members WHERE assignmentId = :assignmentId")
    suspend fun clearAssignmentMembers(assignmentId: Long)
}
