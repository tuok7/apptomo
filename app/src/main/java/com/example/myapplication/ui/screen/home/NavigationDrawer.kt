package com.example.myapplication.ui.screen.home

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun NavigationDrawerContent(
    userPreferences: com.example.myapplication.data.preferences.UserPreferences,
    onClose: () -> Unit
) {
    var fullName by remember { mutableStateOf(userPreferences.getFullName().ifEmpty { "Người dùng" }) }
    var email by remember { mutableStateOf(userPreferences.getEmail().ifEmpty { "user@example.com" }) }
    var showEditDialog by remember { mutableStateOf(false) }
    
    ModalDrawerSheet(
        modifier = Modifier.fillMaxHeight().fillMaxWidth(0.8f),
        drawerContainerColor = Color.White
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DrawerHeader(fullName, email, onClose) {
                showEditDialog = true
            }
            DrawerMenuItems()
            DrawerFooter()
        }
    }
    
    if (showEditDialog) {
        EditProfileDialog(
            userPreferences = userPreferences,
            onDismiss = { showEditDialog = false },
            onSaved = {
                // Refresh data after save
                fullName = userPreferences.getFullName().ifEmpty { "Người dùng" }
                email = userPreferences.getEmail().ifEmpty { "user@example.com" }
            }
        )
    }
}

@Composable
private fun DrawerHeader(fullName: String, email: String, onClose: () -> Unit, onEditClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().background(
            Brush.linearGradient(listOf(Color(0xFF667eea), Color(0xFF764ba2)))
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier.size(72.dp)
                            .background(Color.White.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(email, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Edit button
                    Surface(
                        onClick = onEditClick,
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text("Chỉnh sửa hồ sơ", style = MaterialTheme.typography.labelMedium, color = Color.White)
                        }
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, "Đóng", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.DrawerMenuItems() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().weight(1f),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item { DrawerMenuItem(Icons.Default.Home, "Trang chủ") { } }
        item { DrawerMenuItem(Icons.Default.Person, "Hồ sơ") { } }
        item { DrawerMenuItem(Icons.Default.Group, "Nhóm của tôi") { } }
        item { DrawerMenuItem(Icons.Default.Assignment, "Nhiệm vụ") { } }
        item { DrawerMenuItem(Icons.Default.Event, "Lịch") { } }
        item { DrawerMenuItem(Icons.Default.Notifications, "Thông báo") { } }
        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
        item { DrawerMenuItem(Icons.Default.Settings, "Cài đặt") { } }
        item { DrawerMenuItem(Icons.Default.DarkMode, "Chế độ tối") { } }
        item { DrawerMenuItem(Icons.Default.Language, "Ngôn ngữ") { } }
        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
        item { DrawerMenuItem(Icons.Default.Help, "Trợ giúp") { } }
        item { DrawerMenuItem(Icons.Default.Info, "Về ứng dụng") { } }
        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
        item { 
            DrawerMenuItem(
                Icons.Default.Logout, "Đăng xuất",
                textColor = Color(0xFFF44336),
                iconTint = Color(0xFFF44336)
            ) { }
        }
    }
}

@Composable
private fun DrawerFooter() {
    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFF5F5F5)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Phiên bản 2.4.0", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text("Made with ❤️ for Students", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    textColor: Color = Color.Black,
    iconTint: Color = Color(0xFF666666),
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = textColor)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    userPreferences: com.example.myapplication.data.preferences.UserPreferences,
    onDismiss: () -> Unit,
    onSaved: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf(userPreferences.getFullName()) }
    var email by remember { mutableStateOf(userPreferences.getEmail()) }
    var phone by remember { mutableStateOf(userPreferences.getPhone()) }
    var bio by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Chỉnh sửa hồ sơ",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Đóng")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Avatar section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF667eea), Color(0xFF764ba2))
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { /* TODO: Change avatar */ }) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Đổi ảnh đại diện")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Form fields
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Họ và tên") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Giới thiệu bản thân") },
                    leadingIcon = {
                        Icon(Icons.Default.Description, null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    maxLines = 5
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hủy")
                    }
                    
                    Button(
                        onClick = {
                            isSaving = true
                            // Save to preferences
                            userPreferences.saveFullName(fullName)
                            userPreferences.saveEmail(email)
                            userPreferences.savePhone(phone)
                            // TODO: Save bio when added to preferences
                            isSaving = false
                            onSaved()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF667eea)
                        ),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lưu")
                        }
                    }
                }
            }
        }
    }
}
