package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.Group
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups ORDER BY createdAt DESC")
    fun getAllGroups(): Flow<List<Group>>
    
    @Query("SELECT * FROM groups WHERE id = :groupId")
    fun getGroupById(groupId: Long): Flow<Group?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: Group): Long
    
    @Update
    suspend fun updateGroup(group: Group)
    
    @Delete
    suspend fun deleteGroup(group: Group)
}
