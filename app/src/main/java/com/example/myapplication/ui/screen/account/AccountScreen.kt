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
    
    val fullName = userPreferences.getFullName()
    val email = userPreferences.getEmail()
    
    var isBiometricEnabled by remember { mutableStateOf(userPreferences.isBiometricEnabled()) }
    var showBiometricDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Tài khoản",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE3F2FD),
                    titleContentColor = Color(0xFF1976D2)
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
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

            // Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        number = "05",
                        label = "Số nhóm",
                        color = Color(0xFF4CAF50)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        number = "12",
                        label = "Công việc",
                        color = Color(0xFF2196F3)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        number = "4.8+",
                        label = "Đánh giá",
                        color = Color(0xFFFF9800)
                    )
                }
            }

            // Account Settings Section
            item {
                Text(
                    text = "CÀI ĐẶT TÀI KHOẢN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.Edit,
                    iconColor = Color(0xFF2196F3),
                    title = "Chỉnh sửa thông tin",
                    onClick = { }
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.Notifications,
                    iconColor = Color(0xFF2196F3),
                    title = "Quản lý thông báo",
                    onClick = { }
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.Lock,
                    iconColor = Color(0xFF2196F3),
                    title = "Đổi mật khẩu",
                    onClick = { }
                )
            }
            
            item {
                SettingItem(
                    icon = Icons.Default.Security,
                    iconColor = Color(0xFF2196F3),
                    title = "Bảo mật & Quyền riêng tư",
                    onClick = { }
                )
            }

            // Help Section
            item {
                Text(
                    text = "HỖ TRỢ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.Help,
                    iconColor = Color(0xFF2196F3),
                    title = "Trung tâm trợ giúp",
                    onClick = { }
                )
            }

            // Logout Button
            item {
                Card(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Đăng xuất",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF44336)
                        )
                    }
                }
            }

            // Footer
            item {
                Text(
                    text = "Phiên bản 2.4.0 • Made with ❤ for Students",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        
        // Biometric Dialog
        if (showBiometricDialog) {
            AlertDialog(
                onDismissRequest = { showBiometricDialog = false },
                icon = { Icon(Icons.Default.Fingerprint, contentDescription = null) },
                title = { Text("Bật đăng nhập sinh trắc học") },
                text = { 
                    Text("Để sử dụng sinh trắc học, bạn cần đăng nhập và chọn 'Ghi nhớ đăng nhập' trước.")
                },
                confirmButton = {
                    TextButton(onClick = { showBiometricDialog = false }) {
                        Text("Đã hiểu")
                    }
                }
            )
        }
        
        // Logout Dialog
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
                            userPreferences.clearAll()
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

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    number: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = number,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
