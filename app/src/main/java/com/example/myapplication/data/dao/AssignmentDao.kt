package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.Assignment
import com.example.myapplication.data.model.AssignmentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments WHERE groupId = :groupId ORDER BY dueDate ASC")
    fun getAssignmentsByGroup(groupId: Long): Flow<List<Assignment>>
    
    @Query("SELECT * FROM assignments WHERE id = :assignmentId")
    fun getAssignmentById(assignmentId: Long): Flow<Assignment?>
    
    @Query("SELECT * FROM assignments WHERE status = :status ORDER BY dueDate ASC")
    fun getAssignmentsByStatus(status: AssignmentStatus): Flow<List<Assignment>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: Assignment): Long
    
    @Update
    suspend fun updateAssignment(assignment: Assignment)
    
    @Delete
    suspend fun deleteAssignment(assignment: Assignment)
}
