package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.api.MessageData
import com.example.myapplication.data.api.SendMessageRequest

class MessageRepository(private val apiService: ApiService) {
    
    suspend fun getMessages(groupId: Long, limit: Int = 50, offset: Int = 0): Result<List<MessageData>> {
        return try {
            val response = apiService.getMessages(groupId, limit, offset)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to load messages"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendMessage(groupId: Long, userId: Long, message: String): Result<MessageData> {
        return try {
            val request = SendMessageRequest(groupId, userId, message)
            val response = apiService.sendMessage(request)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("No data returned"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to send message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteMessage(messageId: Long, userId: Long): Result<Boolean> {
        return try {
            val response = apiService.deleteMessage(messageId, userId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to delete message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
