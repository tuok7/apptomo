package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.Member
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    // MemberDao chỉ dùng để lưu cache từ API
    // Không có bảng members trong Room, dữ liệu lấy từ API (users + group_members)
    
    // Các method này sẽ được implement trong Repository layer
    // để gọi API thay vì truy vấn database local
    
    // Placeholder methods - sẽ không được sử dụng trực tiếp
    @Query("SELECT 1") // Dummy query
    suspend fun placeholder(): Int
}
