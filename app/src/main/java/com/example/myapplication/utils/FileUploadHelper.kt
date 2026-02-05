package com.example.myapplication.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

object FileUploadHelper {
    
    /**
     * Tạo MultipartBody.Part từ Uri
     */
    fun createMultipartFromUri(
        context: Context,
        uri: Uri,
        paramName: String = "file"
    ): MultipartBody.Part? {
        return try {
            val file = getFileFromUri(context, uri) ?: return null
            val requestFile = file.asRequestBody(getMimeType(file.extension).toMediaTypeOrNull())
            MultipartBody.Part.createFormData(paramName, file.name, requestFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Lấy file từ Uri
     */
    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = getFileName(context, uri)
            val file = File(context.cacheDir, fileName)
            
            FileOutputStream(file).use { output ->
                inputStream.copyTo(output)
            }
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Lấy tên file từ Uri
     */
    fun getFileName(context: Context, uri: Uri): String {
        var fileName = "file_${System.currentTimeMillis()}"
        
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                fileName = cursor.getString(nameIndex)
            }
        }
        
        return fileName
    }
    
    /**
     * Lấy kích thước file từ Uri
     */
    fun getFileSize(context: Context, uri: Uri): Long {
        var fileSize = 0L
        
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst() && sizeIndex >= 0) {
                fileSize = cursor.getLong(sizeIndex)
            }
        }
        
        return fileSize
    }
    
    /**
     * Tạo RequestBody cho type parameter
     */
    fun createTypeRequestBody(type: String): okhttp3.RequestBody {
        return type.toRequestBody("text/plain".toMediaTypeOrNull())
    }
    
    /**
     * Lấy MIME type từ extension
     */
    private fun getMimeType(extension: String): String {
        return when (extension.lowercase()) {
            // Documents
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "txt" -> "text/plain"
            "csv" -> "text/csv"
            
            // Images
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "bmp" -> "image/bmp"
            "svg" -> "image/svg+xml"
            "webp" -> "image/webp"
            
            // Videos
            "mp4" -> "video/mp4"
            "avi" -> "video/x-msvideo"
            "mov" -> "video/quicktime"
            "wmv" -> "video/x-ms-wmv"
            "flv" -> "video/x-flv"
            "mkv" -> "video/x-matroska"
            
            // Audio
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "aac" -> "audio/aac"
            
            // Archives
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            "7z" -> "application/x-7z-compressed"
            "tar" -> "application/x-tar"
            "gz" -> "application/gzip"
            
            else -> "application/octet-stream"
        }
    }
    
    /**
     * Format file size
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
    
    /**
     * Kiểm tra file có hợp lệ không
     */
    fun isValidFile(context: Context, uri: Uri, maxSizeInMB: Int = 50): Pair<Boolean, String> {
        val fileSize = getFileSize(context, uri)
        val maxSizeInBytes = maxSizeInMB * 1024 * 1024L
        
        return if (fileSize > maxSizeInBytes) {
            false to "File quá lớn. Kích thước tối đa là ${maxSizeInMB}MB"
        } else if (fileSize == 0L) {
            false to "File không hợp lệ"
        } else {
            true to "OK"
        }
    }
    
    /**
     * Lấy icon cho file type
     */
    fun getFileIcon(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "pdf" -> "📄"
            "doc", "docx" -> "📝"
            "xls", "xlsx" -> "📊"
            "ppt", "pptx" -> "📽️"
            "txt" -> "📃"
            "jpg", "jpeg", "png", "gif", "bmp", "webp" -> "🖼️"
            "mp4", "avi", "mov", "mkv" -> "🎥"
            "mp3", "wav", "ogg" -> "🎵"
            "zip", "rar", "7z" -> "📦"
            else -> "📎"
        }
    }
}
