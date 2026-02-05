package com.example.myapplication.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "user_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_REMEMBER_LOGIN = "remember_login"
        private const val KEY_SAVED_EMAIL = "saved_email"
        private const val KEY_SAVED_PASSWORD = "saved_password"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_BIOMETRIC_EMAIL = "biometric_email"
    }
    
    fun saveUserInfo(userId: Long, fullName: String, email: String) {
        prefs.edit().apply {
            putLong(KEY_USER_ID, userId)
            putString(KEY_FULL_NAME, fullName)
            putString(KEY_EMAIL, email)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, -1)
    
    fun getFullName(): String = prefs.getString(KEY_FULL_NAME, "") ?: ""
    
    fun getEmail(): String = prefs.getString(KEY_EMAIL, "") ?: ""
    
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    
    // Ghi nhớ đăng nhập
    fun saveLoginCredentials(email: String, password: String, remember: Boolean) {
        prefs.edit().apply {
            putBoolean(KEY_REMEMBER_LOGIN, remember)
            if (remember) {
                putString(KEY_SAVED_EMAIL, email)
                // Mã hóa đơn giản password (trong thực tế nên dùng encryption mạnh hơn)
                val encodedPassword = Base64.encodeToString(password.toByteArray(), Base64.DEFAULT)
                putString(KEY_SAVED_PASSWORD, encodedPassword)
            } else {
                remove(KEY_SAVED_EMAIL)
                remove(KEY_SAVED_PASSWORD)
            }
            apply()
        }
    }
    
    fun isRememberLogin(): Boolean = prefs.getBoolean(KEY_REMEMBER_LOGIN, false)
    
    fun getSavedEmail(): String = prefs.getString(KEY_SAVED_EMAIL, "") ?: ""
    
    fun getSavedPassword(): String {
        val encoded = prefs.getString(KEY_SAVED_PASSWORD, "") ?: ""
        return if (encoded.isNotEmpty()) {
            try {
                String(Base64.decode(encoded, Base64.DEFAULT))
            } catch (e: Exception) {
                ""
            }
        } else {
            ""
        }
    }
    
    // Sinh trắc học
    fun enableBiometric(email: String) {
        prefs.edit().apply {
            putBoolean(KEY_BIOMETRIC_ENABLED, true)
            putString(KEY_BIOMETRIC_EMAIL, email)
            apply()
        }
    }
    
    fun disableBiometric() {
        prefs.edit().apply {
            putBoolean(KEY_BIOMETRIC_ENABLED, false)
            remove(KEY_BIOMETRIC_EMAIL)
            apply()
        }
    }
    
    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    
    fun getBiometricEmail(): String = prefs.getString(KEY_BIOMETRIC_EMAIL, "") ?: ""
    
    fun clearUserInfo() {
        val rememberLogin = isRememberLogin()
        val savedEmail = getSavedEmail()
        val savedPassword = prefs.getString(KEY_SAVED_PASSWORD, "")
        val biometricEnabled = isBiometricEnabled()
        val biometricEmail = getBiometricEmail()
        
        prefs.edit().clear().apply()
        
        // Giữ lại thông tin đăng nhập nếu đã chọn ghi nhớ
        if (rememberLogin) {
            prefs.edit().apply {
                putBoolean(KEY_REMEMBER_LOGIN, true)
                putString(KEY_SAVED_EMAIL, savedEmail)
                putString(KEY_SAVED_PASSWORD, savedPassword)
                apply()
            }
        }
        
        // Giữ lại thông tin sinh trắc học
        if (biometricEnabled) {
            prefs.edit().apply {
                putBoolean(KEY_BIOMETRIC_ENABLED, true)
                putString(KEY_BIOMETRIC_EMAIL, biometricEmail)
                apply()
            }
        }
    }
    
    // Xóa tất cả dữ liệu (bao gồm cả remember login và biometric)
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}