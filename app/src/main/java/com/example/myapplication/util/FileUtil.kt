package com.example.myapplication.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

object FileUtil {
    
    /**
     * Lấy tên file từ Uri
     */
    fun getFileName(context: Context, uri: Uri): String {
        var fileName = "unknown"
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
     * Format kích thước file thành string dễ đọc
     */
    fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }
    
    /**
     * Lấy MIME type từ Uri
     */
    fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }
    
    /**
     * Lấy extension từ file name
     */
    fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "")
    }
    
    /**
     * Kiểm tra có phải file ảnh không
     */
    fun isImageFile(mimeType: String?): Boolean {
        return mimeType?.startsWith("image/") == true
    }
    
    /**
     * Kiểm tra có phải file video không
     */
    fun isVideoFile(mimeType: String?): Boolean {
        return mimeType?.startsWith("video/") == true
    }
    
    /**
     * Kiểm tra có phải file audio không
     */
    fun isAudioFile(mimeType: String?): Boolean {
        return mimeType?.startsWith("audio/") == true
    }
    
    /**
     * Copy file từ Uri vào internal storage
     */
    fun copyFileToInternalStorage(context: Context, uri: Uri, destinationPath: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val outputFile = File(destinationPath)
            outputFile.parentFile?.mkdirs()
            
            FileOutputStream(outputFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Tạo thumbnail cho ảnh
     */
    fun createImageThumbnail(imagePath: String, thumbnailPath: String, maxSize: Int = 200): Boolean {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(imagePath, options)
            
            val scale = maxOf(
                options.outWidth / maxSize,
                options.outHeight / maxSize
            )
            
            options.inJustDecodeBounds = false
            options.inSampleSize = scale
            
            val bitmap = BitmapFactory.decodeFile(imagePath, options)
            val thumbnailFile = File(thumbnailPath)
            thumbnailFile.parentFile?.mkdirs()
            
            FileOutputStream(thumbnailFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            bitmap.recycle()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Lấy đường dẫn lưu file cho nhóm
     */
    fun getGroupFilePath(context: Context, groupId: Long, fileName: String): String {
        val groupDir = File(context.filesDir, "groups/$groupId/files")
        groupDir.mkdirs()
        return File(groupDir, fileName).absolutePath
    }
    
    /**
     * Lấy đường dẫn lưu thumbnail
     */
    fun getThumbnailPath(context: Context, groupId: Long, fileName: String): String {
        val thumbDir = File(context.filesDir, "groups/$groupId/thumbnails")
        thumbDir.mkdirs()
        return File(thumbDir, "thumb_$fileName").absolutePath
    }
    
    /**
     * Xóa file
     */
    fun deleteFile(filePath: String): Boolean {
        return try {
            File(filePath).delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Kiểm tra file có tồn tại không
     */
    fun fileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }
}
