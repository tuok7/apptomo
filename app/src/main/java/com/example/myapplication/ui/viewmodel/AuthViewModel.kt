package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.preferences.UserPreferences
import com.example.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository()
    private val userPreferences = UserPreferences(application)
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState
    
    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState
    
    private val _resetPasswordState = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Idle)
    val resetPasswordState: StateFlow<ResetPasswordState> = _resetPasswordState
    
    fun login(email: String, password: String, rememberMe: Boolean = false) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(email, password)
            _authState.value = if (result.isSuccess) {
                val response = result.getOrNull()
                if (response?.success == true && response.data != null) {
                    // Lưu thông tin user
                    userPreferences.saveUserInfo(
                        response.data.id,
                        response.data.fullName,
                        response.data.email ?: response.data.phone ?: ""
                    )
                    // Lưu thông tin đăng nhập nếu chọn ghi nhớ
                    userPreferences.saveLoginCredentials(email, password, rememberMe)
                    
                    // Tự động bật sinh trắc học nếu chọn ghi nhớ
                    if (rememberMe) {
                        userPreferences.enableBiometric(email)
                    }
                    
                    AuthState.Success(response.message)
                } else {
                    AuthState.Error(response?.message ?: "Đăng nhập thất bại")
                }
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Lỗi không xác định")
            }
        }
    }
    
    fun register(fullName: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.register(fullName, email, password)
            _authState.value = if (result.isSuccess) {
                val response = result.getOrNull()
                if (response?.success == true) {
                    // KHÔNG lưu thông tin user, chỉ thông báo đăng ký thành công
                    AuthState.RegisterSuccess(response.message)
                } else {
                    AuthState.Error(response?.message ?: "Đăng ký thất bại")
                }
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Lỗi không xác định")
            }
        }
    }
    
    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _forgotPasswordState.value = ForgotPasswordState.Loading
            val result = repository.forgotPassword(email)
            _forgotPasswordState.value = if (result.isSuccess) {
                val response = result.getOrNull()
                if (response?.success == true) {
                    ForgotPasswordState.Success(response.message)
                } else {
                    ForgotPasswordState.Error(response?.message ?: "Gửi mã xác nhận thất bại")
                }
            } else {
                ForgotPasswordState.Error(result.exceptionOrNull()?.message ?: "Lỗi không xác định")
            }
        }
    }
    
    fun resetPassword(email: String, code: String, newPassword: String) {
        viewModelScope.launch {
            _resetPasswordState.value = ResetPasswordState.Loading
            val result = repository.resetPassword(email, code, newPassword)
            _resetPasswordState.value = if (result.isSuccess) {
                val response = result.getOrNull()
                if (response?.success == true) {
                    ResetPasswordState.Success(response.message)
                } else {
                    ResetPasswordState.Error(response?.message ?: "Đặt lại mật khẩu thất bại")
                }
            } else {
                ResetPasswordState.Error(result.exceptionOrNull()?.message ?: "Lỗi không xác định")
            }
        }
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
    
    fun resetForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordState.Idle
        _resetPasswordState.value = ResetPasswordState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class RegisterSuccess(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    data class Success(val message: String) : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}

sealed class ResetPasswordState {
    object Idle : ResetPasswordState()
    object Loading : ResetPasswordState()
    data class Success(val message: String) : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()
}
