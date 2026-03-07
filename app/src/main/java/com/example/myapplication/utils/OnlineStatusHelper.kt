package com.example.myapplication.utils

import com.example.myapplication.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class OnlineStatusHelper(
    private val database: AppDatabase,
    private val scope: CoroutineScope
) {
    
    fun setUserOnline(userId: Long) {
        scope.launch {
            database.memberDao().updateMemberOnlineStatus(
                memberId = userId,
                isOnline = true,
                lastActivity = System.currentTimeMillis()
            )
        }
    }
    
    fun setUserOffline(userId: Long) {
        scope.launch {
            database.memberDao().setMemberOffline(
                memberId = userId,
                lastSeen = System.currentTimeMillis()
            )
        }
    }
    
    fun updateUserActivity(userId: Long) {
        scope.launch {
            database.memberDao().updateMemberOnlineStatus(
                memberId = userId,
                isOnline = true,
                lastActivity = System.currentTimeMillis()
            )
        }
    }
}