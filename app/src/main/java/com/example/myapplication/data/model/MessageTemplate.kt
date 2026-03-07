package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message_templates")
data class MessageTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val content: String,
    val category: String = "general", // general, greeting, meeting, deadline, etc.
    val usageCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long? = null,
    val isFavorite: Boolean = false
)

// Predefined quick replies
object QuickReplies {
    val DEFAULT_REPLIES = listOf(
        "OK 👍",
        "Cảm ơn! 🙏",
        "Đồng ý ✅",
        "Không được ❌",
        "Đang làm... ⏳",
        "Xong rồi! ✨",
        "Để tôi kiểm tra 🔍",
        "Tôi sẽ quay lại sau 🔄",
        "Có vấn đề gì không? 🤔",
        "Tuyệt vời! 🎉"
    )
    
    val MEETING_TEMPLATES = listOf(
        "Họp lúc {time} nhé!",
        "Địa điểm: {location}",
        "Link meeting: {link}",
        "Agenda: {agenda}"
    )
    
    val DEADLINE_TEMPLATES = listOf(
        "Deadline: {date}",
        "Còn {days} ngày nữa!",
        "Nhắc nhở: Hạn nộp {date}",
        "Ưu tiên cao! Deadline {date}"
    )
}
