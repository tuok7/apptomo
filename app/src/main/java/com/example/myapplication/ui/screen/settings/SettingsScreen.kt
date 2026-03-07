package com.example.myapplication.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.myapplication.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    userPreferences: com.example.myapplication.data.preferences.UserPreferences
) {
    val viewModel = remember { SettingsViewModel(userPreferences) }
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Cài đặt",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Account Section
            item {
                SettingsSection(
                    title = "TÀI KHOẢN",
                    titleColor = Color(0xFF8B5CF6)
                ) {
                    SettingsAccountItem(
                        name = uiState.userName,
                        subtitle = uiState.userClass,
                        onClick = { /* Navigate to profile */ }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "Đổi mật khẩu",
                        iconColor = Color(0xFF8B5CF6),
                        onClick = { /* Navigate to change password */ }
                    )
                }
            }
            
            // Notifications Section
            item {
                SettingsSection(
                    title = "THÔNG BÁO",
                    titleColor = Color(0xFF8B5CF6)
                ) {
                    SettingsSwitchItem(
                        icon = Icons.Default.Notifications,
                        title = "Nhắc nhở bài tập",
                        iconColor = Color(0xFF8B5CF6),
                        checked = uiState.reminderNotifications,
                        onCheckedChange = { viewModel.updateReminderNotifications(it) }
                    )
                    
                    SettingsSwitchItem(
                        icon = Icons.Default.Schedule,
                        title = "Lịch thi sắp tới",
                        iconColor = Color(0xFF8B5CF6),
                        checked = uiState.scheduleNotifications,
                        onCheckedChange = { viewModel.updateScheduleNotifications(it) }
                    )
                }
            }
            
            // Interface Section
            item {
                SettingsSection(
                    title = "GIAO DIỆN",
                    titleColor = Color(0xFF8B5CF6)
                ) {
                    SettingsSwitchItem(
                        icon = Icons.Default.DarkMode,
                        title = "Chế độ tối",
                        iconColor = Color(0xFF8B5CF6),
                        checked = uiState.darkMode,
                        onCheckedChange = { viewModel.updateDarkMode(it) }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = "Chủ đề ứng dụng",
                        iconColor = Color(0xFF8B5CF6),
                        subtitle = "Tím hiện đại",
                        onClick = { /* Navigate to theme selection */ }
                    )
                }
            }
            
            // Help Section
            item {
                SettingsSection(
                    title = "TRỢ GIÚP",
                    titleColor = Color(0xFF8B5CF6)
                ) {
                    SettingsItem(
                        icon = Icons.Default.Help,
                        title = "Trung tâm hỗ trợ",
                        iconColor = Color(0xFF8B5CF6),
                        showArrow = false,
                        onClick = { /* Navigate to help center */ }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "Về ứng dụng",
                        iconColor = Color(0xFF8B5CF6),
                        subtitle = uiState.appVersion,
                        showArrow = false,
                        onClick = { /* Show app info */ }
                    )
                }
            }
            
            // Logout Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                viewModel.logout()
                                // Navigate to login screen
                                navController.navigate("auth") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Đăng xuất",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    titleColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = titleColor,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(4.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsAccountItem(
    name: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color(0xFF8B5CF6).copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(1),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B5CF6)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F2937)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280)
            )
        }
        
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF9CA3AF),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    iconColor: Color,
    subtitle: String? = null,
    showArrow: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F2937)
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280)
                )
            }
        }
        
        if (showArrow) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    iconColor: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1F2937),
            modifier = Modifier.weight(1f)
        )
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF8B5CF6),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE5E7EB)
            )
        )
    }
}