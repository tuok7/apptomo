package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ForgotPasswordRequest
import com.example.myapplication.data.api.LoginRequest
import com.example.myapplication.data.api.RegisterRequest
import com.example.myapplication.data.api.ResetPasswordRequest
import com.example.myapplication.data.api.RetrofitClient

class AuthRepository {
    private val apiService = RetrofitClient.apiService
    
    suspend fun login(email: String, password: String) = try {
        val response = apiService.login(LoginRequest(email, password))
        if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Đăng nhập thất bại: ${response.message()}"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Lỗi kết nối: ${e.message}"))
    }
    
    suspend fun register(fullName: String, email: String, password: String) = try {
        val response = apiService.register(RegisterRequest(fullName, email, password))
        if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Đăng ký thất bại: ${response.message()}"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Lỗi kết nối: ${e.message}"))
    }
    
    suspend fun forgotPassword(email: String) = try {
        val response = apiService.forgotPassword(ForgotPasswordRequest(email))
        if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Gửi mã xác nhận thất bại: ${response.message()}"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Lỗi kết nối: ${e.message}"))
    }
    
    suspend fun resetPassword(email: String, code: String, newPassword: String) = try {
        val response = apiService.resetPassword(ResetPasswordRequest(email, code, newPassword))
        if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Đặt lại mật khẩu thất bại: ${response.message()}"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Lỗi kết nối: ${e.message}"))
    }
}
