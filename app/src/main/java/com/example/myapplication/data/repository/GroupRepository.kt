package com.example.myapplication.data.repository

import com.example.myapplication.data.api.*

class GroupRepository {
    private val apiService = RetrofitClient.apiService
    
    suspend fun getGroups(userId: Long) = try {
        val response = apiService.getGroups(userId)
        if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Failed to get groups: ${response.message()}"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Network error: ${e.message}"))
    }
    
    suspend fun createGroup(name: String, description: String, createdBy: Long) = try {
        val response = apiService.createGroup(CreateGroupRequest(name, description, createdBy))
        if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Failed to create group: ${response.message()}"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Network error: ${e.message}"))
    }
    
    suspend fun updateGroup(id: Long, name: String, description: String) = try {
        val response = apiService.updateGroup(UpdateGroupRequest(id, name, description))
        if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Failed to update group: ${response.message()}"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Network error: ${e.message}"))
    }
    
    suspend fun deleteGroup(groupId: Long) = try {
        val response = apiService.deleteGroup(groupId)
        if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Failed to delete group: ${response.message()}"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Network error: ${e.message}"))
    }
    
    suspend fun getAssignments(groupId: Long) = try {
        val response = apiService.getAssignments(groupId)
        if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Failed to get assignments: ${response.message()}"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Network error: ${e.message}"))
    }
    
    suspend fun createAssignment(
        groupId: Long,
        title: String,
        description: String,
        dueDate: Long?,
        priority: String,
        createdBy: Long,
        assignedMembers: List<Long> = emptyList()
    ) = try {
        val response = apiService.createAssignment(
            CreateAssignmentRequest(groupId, title, description, dueDate, priority, createdBy, assignedMembers)
        )
        if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Failed to create assignment: ${response.message()}"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Network error: ${e.message}"))
    }
    
    suspend fun getGroupMembers(groupId: Long) = try {
        val response = apiService.getGroupMembers(groupId)
        if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Failed to get members: ${response.message()}"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Network error: ${e.message}"))
    }
    
    suspend fun addGroupMember(groupId: Long, email: String, role: String = "member") = try {
        val response = apiService.addGroupMember(AddMemberRequest(groupId, email, role))
        if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Failed to add member: ${response.message()}"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Network error: ${e.message}"))
    }
    
    suspend fun getMessages(groupId: Long, limit: Int = 50, offset: Int = 0) = try {
        val response = apiService.getMessages(groupId, limit, offset)
        if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Failed to get messages: ${response.message()}"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Network error: ${e.message}"))
    }
    
    suspend fun sendMessage(groupId: Long, userId: Long, message: String) = try {
        val response = apiService.sendMessage(SendMessageRequest(groupId, userId, message))
        if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Failed to send message: ${response.message()}"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Network error: ${e.message}"))
    }
    
    suspend fun deleteMessage(messageId: Long, userId: Long) = try {
        val response = apiService.deleteMessage(messageId, userId)
        if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Failed to delete message: ${response.message()}"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Network error: ${e.message}"))
    }
}