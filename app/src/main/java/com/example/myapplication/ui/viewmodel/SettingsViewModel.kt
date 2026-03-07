package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val reminderNotifications: Boolean = true,
    val scheduleNotifications: Boolean = true,
    val darkMode: Boolean = false,
    val userName: String = "",
    val userClass: String = "",
    val appVersion: String = "v2.4.0"
)

class SettingsViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                userName = userPreferences.getFullName().ifEmpty { "Nguyễn Văn A" },
                userClass = "Lớp 12A1 - THPT Chuyên",
                reminderNotifications = userPreferences.getReminderNotifications(),
                scheduleNotifications = userPreferences.getScheduleNotifications(),
                darkMode = userPreferences.getDarkMode()
            )
        }
    }
    
    fun updateReminderNotifications(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setReminderNotifications(enabled)
            _uiState.value = _uiState.value.copy(reminderNotifications = enabled)
        }
    }
    
    fun updateScheduleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setScheduleNotifications(enabled)
            _uiState.value = _uiState.value.copy(scheduleNotifications = enabled)
        }
    }
    
    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDarkMode(enabled)
            _uiState.value = _uiState.value.copy(darkMode = enabled)
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            userPreferences.clearUserData()
        }
    }
}