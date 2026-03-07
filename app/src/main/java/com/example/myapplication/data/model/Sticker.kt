package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sticker_packs")
data class StickerPack(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val author: String,
    val thumbnailUrl: String,
    val isInstalled: Boolean = false,
    val isFavorite: Boolean = false,
    val stickerCount: Int = 0,
    val category: String = "general", // general, emoji, meme, cute, funny
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stickers")
data class Sticker(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packId: Long,
    val imageUrl: String,
    val thumbnailUrl: String? = null,
    val keywords: String? = null, // JSON array for search
    val usageCount: Int = 0,
    val lastUsedAt: Long? = null
)

// GIF data
data class GifItem(
    val id: String,
    val url: String,
    val previewUrl: String,
    val width: Int,
    val height: Int,
    val title: String? = null
)

// Popular sticker categories
object StickerCategories {
    const val EMOJI = "emoji"
    const val MEME = "meme"
    const val CUTE = "cute"
    const val FUNNY = "funny"
    const val LOVE = "love"
    const val SAD = "sad"
    const val ANGRY = "angry"
    const val CELEBRATION = "celebration"
    
    val ALL_CATEGORIES = listOf(
        EMOJI, MEME, CUTE, FUNNY, LOVE, SAD, ANGRY, CELEBRATION
    )
    
    fun getCategoryEmoji(category: String): String {
        return when (category) {
            EMOJI -> "😀"
            MEME -> "🤣"
            CUTE -> "🥰"
            FUNNY -> "😂"
            LOVE -> "❤️"
            SAD -> "😢"
            ANGRY -> "😠"
            CELEBRATION -> "🎉"
            else -> "📦"
        }
    }
}
