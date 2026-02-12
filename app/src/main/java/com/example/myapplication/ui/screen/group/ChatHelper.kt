package com.example.myapplication.ui.screen.group

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.myapplication.data.model.GroupMessage
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class ChatHelper(
    private val database: com.example.myapplication.data.database.AppDatabase,
    private val groupId: Long,
    private val currentUserId: Long,
    private val currentUserName: String
) {
    suspend fun sendMessage(
        content: String,
        replyToMessage: GroupMessage? = null
    ): Long {
        val message = GroupMessage(
            groupId = groupId,
            senderId = currentUserId,
            senderName = currentUserName,
            content = content,
            replyToMessageId = replyToMessage?.id,
            replyToContent = replyToMessage?.content,
            replyToSenderName = replyToMessage?.senderName
        )
        return database.groupMessageDao().insertMessage(message)
    }
    
    suspend fun editMessage(messageId: Long, newContent: String) {
        database.groupMessageDao().editMessage(
            messageId = messageId,
            newContent = newContent,
            editedAt = System.currentTimeMillis()
        )
    }
    
    suspend fun deleteMessage(messageId: Long) {
        database.groupMessageDao().deleteMessageById(messageId)
    }
    
    suspend fun addReaction(messageId: Long, emoji: String, userId: Long) {
        val message = database.groupMessageDao().getMessageById(messageId) ?: return
        
        val reactionsMap = if (message.reactions.isNullOrEmpty()) {
            JSONObject()
        } else {
            try {
                JSONObject(message.reactions)
            } catch (e: Exception) {
                JSONObject()
            }
        }
        
        // Get or create array for this emoji
        val usersArray = if (reactionsMap.has(emoji)) {
            reactionsMap.getJSONArray(emoji)
        } else {
            JSONArray()
        }
        
        // Check if user already reacted with this emoji
        var alreadyReacted = false
        for (i in 0 until usersArray.length()) {
            if (usersArray.getLong(i) == userId) {
                alreadyReacted = true
                break
            }
        }
        
        if (!alreadyReacted) {
            usersArray.put(userId)
            reactionsMap.put(emoji, usersArray)
            database.groupMessageDao().updateReactions(messageId, reactionsMap.toString())
        }
    }
}
