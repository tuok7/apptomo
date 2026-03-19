package com.example.myapplication.ui.screen.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToForgotPassword: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userPreferences = remember { com.example.myapplication.data.preferences.UserPreferences(context) }
    val authRepository = remember { com.example.myapplication.data.repository.AuthRepository() }
    val coroutineScope = rememberCoroutineScope()
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoginMode by remember { mutableStateOf(true) }
    var rememberMe by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scale = remember { Animatable(0f) }
    
    // Load saved credentials when screen loads
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
        
        // Load saved login info if remember login is enabled
        if (userPreferences.isRememberLogin()) {
            email = userPreferences.getSavedEmail()
            password = userPreferences.getSavedPassword()
            rememberMe = true
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .scale(scale.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Card(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Logo",
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = if (isLoginMode) "Đăng nhập" else "Đăng ký",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = if (isLoginMode) "Vào tài khoản TOMO của bạn" else "Tạo tài khoản TOMO mới",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Auth form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Full name field (only for register)
                    if (!isLoginMode) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Họ và tên") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            enabled = !isLoading
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Số điện thoại") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            enabled = !isLoading
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    OutlinedTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            errorMessage = "" // Clear error when user types
                        },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        enabled = !isLoading,
                        isError = errorMessage.isNotEmpty()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            errorMessage = "" // Clear error when user types
                        },
                        label = { Text("Mật khẩu") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        enabled = !isLoading,
                        isError = errorMessage.isNotEmpty()
                    )
                    
                    // Confirm password field (only for register)
                    if (!isLoginMode) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Xác nhận mật khẩu") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (confirmPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            enabled = !isLoading
                        )
                    }
                    
                    if (isLoginMode) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Remember me checkbox
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF667eea)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lưu mật khẩu",
                                fontSize = 14.sp,
                                color = Color(0xFF374151)
                            )
                        }
                    }
                    
                    // Error message
                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            errorMessage = ""
                            
                            if (isLoginMode) {
                                // Đăng nhập
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = "Vui lòng nhập đầy đủ thông tin"
                                    return@Button
                                }
                                
                                isLoading = true
                                coroutineScope.launch {
                                    try {
                                        val result = authRepository.login(email, password)
                                        result.fold(
                                            onSuccess = { response ->
                                                if (response.success && response.data != null) {
                                                    // Lưu thông tin đăng nhập
                                                    userPreferences.saveLoginCredentials(email, password, rememberMe)
                                                    userPreferences.saveUserInfo(
                                                        response.data.id,
                                                        response.data.fullName,
                                                        response.data.email ?: email
                                                    )
                                                    onLoginSuccess()
                                                } else {
                                                    errorMessage = response.message
                                                }
                                            },
                                            onFailure = { exception ->
                                                errorMessage = exception.message ?: "Đăng nhập thất bại"
                                            }
                                        )
                                    } catch (e: Exception) {
                                        errorMessage = "Lỗi kết nối: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            } else {
                                // Đăng ký
                                if (email.isBlank() || password.isBlank() || fullName.isBlank() || phone.isBlank()) {
                                    errorMessage = "Vui lòng nhập đầy đủ thông tin"
                                    return@Button
                                }
                                
                                if (password != confirmPassword) {
                                    errorMessage = "Mật khẩu xác nhận không khớp"
                                    return@Button
                                }
                                
                                if (password.length < 6) {
                                    errorMessage = "Mật khẩu phải có ít nhất 6 ký tự"
                                    return@Button
                                }
                                
                                isLoading = true
                                coroutineScope.launch {
                                    try {
                                        val result = authRepository.register(fullName, email, phone, password)
                                        result.fold(
                                            onSuccess = { response ->
                                                if (response.success) {
                                                    // Đăng ký thành công, chuyển về đăng nhập
                                                    isLoginMode = true
                                                    fullName = ""
                                                    phone = ""
                                                    confirmPassword = ""
                                                    password = ""
                                                    errorMessage = ""
                                                    // Hiển thị thông báo thành công
                                                } else {
                                                    errorMessage = response.message
                                                }
                                            },
                                            onFailure = { exception ->
                                                errorMessage = exception.message ?: "Đăng ký thất bại"
                                            }
                                        )
                                    } catch (e: Exception) {
                                        errorMessage = "Lỗi kết nối: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF667eea)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isLoginMode) "Đăng nhập" else "Đăng ký",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                    
                    if (isLoginMode) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        TextButton(
                            onClick = onNavigateToForgotPassword,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Quên mật khẩu?",
                                color = Color(0xFF667eea)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            TextButton(
                onClick = { 
                    isLoginMode = !isLoginMode
                    // Reset form khi chuyển mode
                    email = ""
                    password = ""
                    fullName = ""
                    phone = ""
                    confirmPassword = ""
                    errorMessage = ""
                }
            ) {
                Text(
                    text = if (isLoginMode) "Chưa có tài khoản? Đăng ký ngay" else "Đã có tài khoản? Đăng nhập",
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}