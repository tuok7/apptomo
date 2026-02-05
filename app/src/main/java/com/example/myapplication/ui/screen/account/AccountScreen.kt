package com.example.myapplication.ui.screen.account

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.preferences.UserPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: com.example.myapplication.ui.viewmodel.GroupViewModel,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    
    // Lấy thông tin user từ SharedPreferences
    val fullName = userPreferences.getFullName()
    val email = userPreferences.getEmail()
    
    // State cho sinh trắc học
    var isBiometricEnabled by remember { mutableStateOf(userPreferences.isBiometricEnabled()) }
    var showBiometricDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Tài khoản",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF667eea),
                                        Color(0xFF764ba2)
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.White
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column {
                                Text(
                                    text = if (fullName.isNotEmpty()) fullName else "Người dùng",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (email.isNotEmpty()) email else "Chưa có email",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }

            // Account Settings
            item {
                Text(
                    text = "Cài đặt tài khoản",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.Edit,
                    title = "Chỉnh sửa thông tin",
                    subtitle = "Cập nhật thông tin cá nhân",
                    onClick = { }
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.Lock,
                    title = "Đổi mật khẩu",
                    subtitle = "Thay đổi mật khẩu của bạn",
                    onClick = { }
                )
            }
            
            item {
                SettingItemWithSwitch(
                    icon = Icons.Default.Fingerprint,
                    title = "Đăng nhập sinh trắc học",
                    subtitle = if (isBiometricEnabled) "Đã bật - Dùng vân tay/Face ID" else "Tắt - Đăng nhập bằng mật khẩu",
                    checked = isBiometricEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            // Bật sinh trắc học
                            if (userPreferences.isRememberLogin()) {
                                userPreferences.enableBiometric(userPreferences.getSavedEmail())
                                isBiometricEnabled = true
                            } else {
                                showBiometricDialog = true
                            }
                        } else {
                            // Tắt sinh trắc học
                            userPreferences.disableBiometric()
                            isBiometricEnabled = false
                        }
                    }
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.Notifications,
                    title = "Thông báo",
                    subtitle = "Quản lý thông báo",
                    onClick = { }
                )
            }

            // App Settings
            item {
                Text(
                    text = "Cài đặt ứng dụng",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.Language,
                    title = "Ngôn ngữ",
                    subtitle = "Tiếng Việt",
                    onClick = { }
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.DarkMode,
                    title = "Giao diện",
                    subtitle = "Chế độ sáng/tối",
                    onClick = { }
                )
            }
            
            item {
                SettingItem(
                    icon = Icons.Default.CloudOff,
                    title = "Chế độ Offline",
                    subtitle = "Làm việc không cần internet",
                    onClick = { }
                )
            }
            
            item {
                SettingItem(
                    icon = Icons.Default.Mic,
                    title = "Nhập giọng nói",
                    subtitle = "Bật/tắt nhập bằng giọng nói",
                    onClick = { }
                )
            }

            // Other
            item {
                Text(
                    text = "Dữ liệu",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingItem(
                    icon = Icons.Default.Download,
                    title = "Xuất dữ liệu",
                    subtitle = "Xuất PDF/Excel",
                    onClick = { }
                )
            }
            
            item {
                SettingItem(
                    icon = Icons.Default.Backup,
                    title = "Sao lưu & Khôi phục",
                    subtitle = "Quản lý dữ liệu sao lưu",
                    onClick = { }
                )
            }
            
            item {
                SettingItem(
                    icon = Icons.Default.QrCode,
                    title = "Chia sẻ QR Code",
                    subtitle = "Tạo mã QR cho nhóm",
                    onClick = { }
                )
            }

            // Other
            item {
                Text(
                    text = "Khác",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.Help,
                    title = "Trợ giúp",
                    subtitle = "Câu hỏi thường gặp",
                    onClick = { }
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.Info,
                    title = "Về ứng dụng",
                    subtitle = "Phiên bản 1.0.0",
                    onClick = { }
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.Logout,
                    title = "Đăng xuất",
                    subtitle = "Thoát khỏi tài khoản",
                    onClick = { showLogoutDialog = true },
                    iconTint = Color(0xFFF44336)
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Dialog thông báo sinh trắc học
        if (showBiometricDialog) {
            AlertDialog(
                onDismissRequest = { showBiometricDialog = false },
                icon = { Icon(Icons.Default.Fingerprint, contentDescription = null) },
                title = { Text("Bật đăng nhập sinh trắc học") },
                text = { 
                    Text("Để sử dụng sinh trắc học, bạn cần đăng nhập và chọn 'Ghi nhớ đăng nhập' trước.\n\nSau đó, bạn có thể bật tính năng này để đăng nhập nhanh bằng vân tay hoặc Face ID.")
                },
                confirmButton = {
                    TextButton(onClick = { showBiometricDialog = false }) {
                        Text("Đã hiểu")
                    }
                }
            )
        }
        
        // Dialog xác nhận đăng xuất
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                icon = { Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFF44336)) },
                title = { Text("Đăng xuất") },
                text = { 
                    Text("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Xóa tất cả dữ liệu đã lưu
                            userPreferences.clearAll()
                            // Clear ViewModel data
                            viewModel.clearData()
                            showLogoutDialog = false
                            onLogout()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFF44336)
                        )
                    ) {
                        Text("Đăng xuất")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingItemWithSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF4A90E2),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Gray
                )
            )
        }
    }
}
