package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "polls")
data class Poll(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long,
    val messageId: Long, // Link to message
    val question: String,
    val options: String, // JSON array: ["Option 1", "Option 2", "Option 3"]
    val votes: String? = null, // JSON: {"0": [1,2,3], "1": [4,5]} - option index -> user IDs
    val createdBy: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null, // Thời gian hết hạn
    val allowMultipleVotes: Boolean = false,
    val isAnonymous: Boolean = false,
    val isClosed: Boolean = false
)

// Vote result for display
data class PollOption(
    val index: Int,
    val text: String,
    val voteCount: Int,
    val percentage: Float,
    val voters: List<Long>,
    val hasVoted: Boolean
)

data class PollResult(
    val poll: Poll,
    val options: List<PollOption>,
    val totalVotes: Int,
    val hasVoted: Boolean,
    val userVotes: List<Int> // Indices of options user voted for
)
