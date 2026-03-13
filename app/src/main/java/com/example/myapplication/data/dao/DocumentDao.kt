package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.Document
import com.example.myapplication.data.model.DocumentFolder
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    
    // Document operations
    @Query("SELECT * FROM documents WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun getDocumentsByGroup(groupId: Long): Flow<List<Document>>
    
    @Query("SELECT * FROM documents WHERE folderId = :folderId ORDER BY createdAt DESC")
    fun getDocumentsByFolder(folderId: Long): Flow<List<Document>>
    
    @Query("SELECT * FROM documents WHERE id = :documentId")
    suspend fun getDocumentById(documentId: Long): Document?
    
    @Query("SELECT * FROM documents WHERE groupId = :groupId AND title LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    suspend fun searchDocuments(groupId: Long, query: String): List<Document>
    
    @Insert
    suspend fun insertDocument(document: Document): Long
    
    @Update
    suspend fun updateDocument(document: Document)
    
    @Delete
    suspend fun deleteDocument(document: Document)
    
    @Query("DELETE FROM documents WHERE id = :documentId")
    suspend fun deleteDocumentById(documentId: Long)
    
    @Query("UPDATE documents SET downloadCount = downloadCount + 1 WHERE id = :documentId")
    suspend fun incrementDownloadCount(documentId: Long)
    
    // Folder operations
    @Query("SELECT * FROM document_folders WHERE groupId = :groupId ORDER BY name ASC")
    fun getFoldersByGroup(groupId: Long): Flow<List<DocumentFolder>>
    
    @Query("SELECT * FROM document_folders WHERE parentId = :parentId ORDER BY name ASC")
    fun getSubFolders(parentId: Long): Flow<List<DocumentFolder>>
    
    @Query("SELECT * FROM document_folders WHERE id = :folderId")
    suspend fun getFolderById(folderId: Long): DocumentFolder?
    
    @Insert
    suspend fun insertFolder(folder: DocumentFolder): Long
    
    @Update
    suspend fun updateFolder(folder: DocumentFolder)
    
    @Delete
    suspend fun deleteFolder(folder: DocumentFolder)
    
    @Query("DELETE FROM document_folders WHERE id = :folderId")
    suspend fun deleteFolderById(folderId: Long)
    
    // Combined queries
    @Query("""
        SELECT COUNT(*) FROM documents 
        WHERE groupId = :groupId AND folderId = :folderId
    """)
    suspend fun getDocumentCountInFolder(groupId: Long, folderId: Long?): Int
    
    @Query("""
        SELECT * FROM documents 
        WHERE groupId = :groupId AND fileType = :fileType 
        ORDER BY createdAt DESC
    """)
    suspend fun getDocumentsByType(groupId: Long, fileType: String): List<Document>
}