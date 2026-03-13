package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.Sticker
import com.example.myapplication.data.model.StickerPack
import kotlinx.coroutines.flow.Flow

@Dao
interface StickerDao {
    
    // Sticker Pack operations
    @Query("SELECT * FROM sticker_packs ORDER BY name ASC")
    fun getAllStickerPacks(): Flow<List<StickerPack>>
    
    @Query("SELECT * FROM sticker_packs WHERE isInstalled = 1 ORDER BY name ASC")
    fun getInstalledStickerPacks(): Flow<List<StickerPack>>
    
    @Query("SELECT * FROM sticker_packs WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteStickerPacks(): Flow<List<StickerPack>>
    
    @Query("SELECT * FROM sticker_packs WHERE category = :category ORDER BY name ASC")
    fun getStickerPacksByCategory(category: String): Flow<List<StickerPack>>
    
    @Query("SELECT * FROM sticker_packs WHERE id = :packId")
    suspend fun getStickerPackById(packId: Long): StickerPack?
    
    @Insert
    suspend fun insertStickerPack(stickerPack: StickerPack): Long
    
    @Update
    suspend fun updateStickerPack(stickerPack: StickerPack)
    
    @Delete
    suspend fun deleteStickerPack(stickerPack: StickerPack)
    
    @Query("UPDATE sticker_packs SET isInstalled = :isInstalled WHERE id = :packId")
    suspend fun updateInstallStatus(packId: Long, isInstalled: Boolean)
    
    @Query("UPDATE sticker_packs SET isFavorite = :isFavorite WHERE id = :packId")
    suspend fun updateFavoriteStatus(packId: Long, isFavorite: Boolean)
    
    // Sticker operations
    @Query("SELECT * FROM stickers WHERE packId = :packId ORDER BY usageCount DESC")
    fun getStickersByPack(packId: Long): Flow<List<Sticker>>
    
    @Query("SELECT * FROM stickers WHERE id = :stickerId")
    suspend fun getStickerById(stickerId: Long): Sticker?
    
    @Query("""
        SELECT s.* FROM stickers s 
        INNER JOIN sticker_packs sp ON s.packId = sp.id 
        WHERE sp.isInstalled = 1 AND s.keywords LIKE '%' || :query || '%'
        ORDER BY s.usageCount DESC
    """)
    suspend fun searchStickers(query: String): List<Sticker>
    
    @Query("""
        SELECT s.* FROM stickers s 
        INNER JOIN sticker_packs sp ON s.packId = sp.id 
        WHERE sp.isInstalled = 1 
        ORDER BY s.lastUsedAt DESC 
        LIMIT :limit
    """)
    suspend fun getRecentlyUsedStickers(limit: Int = 20): List<Sticker>
    
    @Query("""
        SELECT s.* FROM stickers s 
        INNER JOIN sticker_packs sp ON s.packId = sp.id 
        WHERE sp.isInstalled = 1 
        ORDER BY s.usageCount DESC 
        LIMIT :limit
    """)
    suspend fun getMostUsedStickers(limit: Int = 20): List<Sticker>
    
    @Insert
    suspend fun insertSticker(sticker: Sticker): Long
    
    @Insert
    suspend fun insertStickers(stickers: List<Sticker>)
    
    @Update
    suspend fun updateSticker(sticker: Sticker)
    
    @Delete
    suspend fun deleteSticker(sticker: Sticker)
    
    @Query("DELETE FROM stickers WHERE packId = :packId")
    suspend fun deleteStickersInPack(packId: Long)
    
    @Query("""
        UPDATE stickers 
        SET usageCount = usageCount + 1, lastUsedAt = :timestamp 
        WHERE id = :stickerId
    """)
    suspend fun incrementStickerUsage(stickerId: Long, timestamp: Long)
    
    @Query("SELECT DISTINCT category FROM sticker_packs ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>
    
    @Query("SELECT COUNT(*) FROM stickers WHERE packId = :packId")
    suspend fun getStickerCountInPack(packId: Long): Int
}