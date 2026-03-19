package com.example.myapplication.data.repository

import com.example.myapplication.data.api.*
import kotlinx.coroutines.delay

class AuthRepository {
    // Mock mode - không cần backend
    private val useMockData = false  // SỬ DỤNG API THẬT VỚI DATABASE MYSQL
    private val apiService = RetrofitClient.apiService
    
    // Mock users database
    private val mockUsers = mutableListOf(
        UserData(1, "Nguyễn Văn A", "test@gmail.com", "0865577745"),
        UserData(2, "Trần Thị B", "user@gmail.com", "0123456789")
    )
    
    suspend fun login(email: String, password: String) = try {
        if (useMockData) {
            // Mock login - chấp nhận bất kỳ email/password nào
            delay(500) // Giả lập network delay
            
            val user = mockUsers.find { it.email == email || it.phone == email } 
                ?: UserData(1, "Demo User", email, email)
            
            Result.success(AuthResponse(
                success = true,
                message = "Đăng nhập thành công",
                data = user
            ))
        } else {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Đăng nhập thất bại: ${response.message()}"))
            }
        }
    } catch (e: Exception) {
        Result.failure(Exception("Lỗi kết nối: ${e.message}"))
    }
    
    suspend fun register(fullName: String, email: String, phone: String, password: String) = try {
        if (useMockData) {
            delay(500)
            
            // Kiểm tra email đã tồn tại
            if (mockUsers.any { it.email == email }) {
                Result.failure(Exception("Email đã được sử dụng"))
            } else {
                val newUser = UserData(
                    id = mockUsers.size + 1L,
                    fullName = fullName,
                    email = email,
                    phone = phone
                )
                mockUsers.add(newUser)
                
                Result.success(AuthResponse(
                    success = true,
                    message = "Đăng ký thành công",
                    data = newUser
                ))
            }
        } else {
            val response = apiService.register(RegisterRequest(fullName, email, phone, password))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Đăng ký thất bại: ${response.message()}"))
            }
        }
    } catch (e: Exception) {
        Result.failure(Exception("Lỗi kết nối: ${e.message}"))
    }
    
    suspend fun forgotPassword(email: String) = try {
        if (useMockData) {
            delay(500)
            Result.success(BaseResponse(
                success = true,
                message = "Mã xác nhận đã được gửi đến email của bạn"
            ))
        } else {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Gửi mã xác nhận thất bại: ${response.message()}"))
            }
        }
    } catch (e: Exception) {
        Result.failure(Exception("Lỗi kết nối: ${e.message}"))
    }
    
    suspend fun resetPassword(email: String, code: String, newPassword: String) = try {
        if (useMockData) {
            delay(500)
            Result.success(BaseResponse(
                success = true,
                message = "Mật khẩu đã được đặt lại thành công"
            ))
        } else {
            val response = apiService.resetPassword(ResetPasswordRequest(email, code, newPassword))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Đặt lại mật khẩu thất bại: ${response.message()}"))
            }
        }
    } catch (e: Exception) {
        Result.failure(Exception("Lỗi kết nối: ${e.message}"))
    }
}
