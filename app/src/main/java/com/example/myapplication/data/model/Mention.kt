package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mentions")
data class Mention(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val messageId: Long,
    val userId: Long,
    val userName: String,
    val startIndex: Int, // Vị trí bắt đầu trong text
    val endIndex: Int, // Vị trí kết thúc
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "link_previews")
data class LinkPreview(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val messageId: Long,
    val url: String,
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val siteName: String? = null,
    val favicon: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

// Helper class for parsing mentions
data class MentionSpan(
    val userId: Long,
    val userName: String,
    val startIndex: Int,
    val length: Int
)

// Helper for link detection
object LinkDetector {
    private val URL_REGEX = Regex(
        "(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?",
        RegexOption.IGNORE_CASE
    )
    
    fun extractLinks(text: String): List<String> {
        return URL_REGEX.findAll(text)
            .map { it.value }
            .filter { it.contains(".") }
            .toList()
    }
    
    fun containsLink(text: String): Boolean {
        return URL_REGEX.containsMatchIn(text)
    }
}

// Helper for mention parsing
object MentionParser {
    private val MENTION_REGEX = Regex("@\\[(\\d+)\\]\\(([^)]+)\\)")
    
    fun parseMentions(text: String): List<MentionSpan> {
        val mentions = mutableListOf<MentionSpan>()
        MENTION_REGEX.findAll(text).forEach { match ->
            val userId = match.groupValues[1].toLongOrNull() ?: return@forEach
            val userName = match.groupValues[2]
            mentions.add(
                MentionSpan(
                    userId = userId,
                    userName = userName,
                    startIndex = match.range.first,
                    length = match.value.length
                )
            )
        }
        return mentions
    }
    
    fun formatMention(userId: Long, userName: String): String {
        return "@[$userId]($userName)"
    }
    
    fun extractMentionedUserIds(text: String): List<Long> {
        return parseMentions(text).map { it.userId }
    }
}
