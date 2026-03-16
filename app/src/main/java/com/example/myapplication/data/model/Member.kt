package com.example.myapplication.data.model

// Member model - sử dụng dữ liệu từ API (users + group_members)
// Không tạo bảng riêng trong Room, chỉ dùng để mapping API response
data class Member(
    val id: Long = 0,
    val groupId: Long,
    val name: String,
    val email: String,
    val role: String = "Thành viên",
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val lastActivity: Long = System.currentTimeMillis()
)
