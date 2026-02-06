package com.example.myapplication.ui.screen.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.preferences.UserPreferences
import com.example.myapplication.ui.viewmodel.AuthState
import com.example.myapplication.ui.viewmodel.AuthViewModel
import com.example.myapplication.utils.BiometricHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToForgotPassword: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val biometricHelper = remember { BiometricHelper(context) }
    
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Load saved email and password if remember login is enabled
    LaunchedEffect(Unit) {
        if (userPreferences.isBiometricEnabled()) {
            email = userPreferences.getBiometricEmail()
            password = userPreferences.getSavedPassword()
        } else if (userPreferences.isRememberLogin()) {
            email = userPreferences.getSavedEmail()
            password = userPreferences.getSavedPassword()
        }
    }
    
    val authState by viewModel.authState.collectAsState()
    
    // Xử lý state từ ViewModel
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                // Đăng nhập thành công
                onLoginSuccess()
                viewModel.resetState()
            }
            is AuthState.RegisterSuccess -> {
                // Đăng ký thành công, chuyển về màn hình đăng nhập
                isLoginMode = true
                errorMessage = ""
                // Hiển thị thông báo thành công
                android.widget.Toast.makeText(
                    context,
                    (authState as AuthState.RegisterSuccess).message,
                    android.widget.Toast.LENGTH_LONG
                ).show()
                viewModel.resetState()
            }
            is AuthState.Error -> {
                errorMessage = (authState as AuthState.Error).message
            }
            else -> {}
        }
    }
    
    val isLoading = authState is AuthState.Loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoginMode) {
                // Login Screen
                LoginContent(
                    email = email,
                    onEmailChange = { email = it },
                    password = password,
                    onPasswordChange = { password = it },
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                    errorMessage = errorMessage,
                    isLoading = isLoading,
                    onLoginClick = { rememberMe ->
                        errorMessage = ""
                        // Kiểm tra xem là email hay SĐT
                        val isEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                        val isPhone = email.matches(Regex("^[0-9]{10,11}$"))
                        
                        when {
                            email.isBlank() -> errorMessage = "Vui lòng nhập email hoặc số điện thoại"
                            !isEmail && !isPhone -> errorMessage = "Email hoặc số điện thoại không hợp lệ"
                            password.isBlank() -> errorMessage = "Vui lòng nhập mật khẩu"
                            password.length < 6 -> errorMessage = "Mật khẩu phải có ít nhất 6 ký tự"
                            else -> viewModel.login(email, password, rememberMe)
                        }
                    },
                    onBiometricClick = {
                        if (biometricHelper.isBiometricAvailable()) {
                            if (userPreferences.isBiometricEnabled()) {
                                val biometricEmail = userPreferences.getBiometricEmail()
                                val savedPassword = userPreferences.getSavedPassword()
                                
                                if (biometricEmail.isNotEmpty() && savedPassword.isNotEmpty()) {
                                    biometricHelper.authenticate(
                                        activity = context as androidx.fragment.app.FragmentActivity,
                                        onSuccess = {
                                            // Đăng nhập bằng thông tin đã lưu
                                            viewModel.login(biometricEmail, savedPassword, true)
                                        },
                                        onError = { error ->
                                            errorMessage = error
                                        }
                                    )
                                } else {
                                    errorMessage = "Chưa có thông tin đăng nhập đã lưu"
                                }
                            } else {
                                errorMessage = "Vui lòng đăng nhập và chọn 'Ghi nhớ' để bật sinh trắc học"
                            }
                        } else {
                            errorMessage = "Thiết bị không hỗ trợ sinh trắc học"
                        }
                    },
                    isBiometricAvailable = biometricHelper.isBiometricAvailable() && userPreferences.isBiometricEnabled(),
                    onSwitchToRegister = { isLoginMode = false },
                    onForgotPassword = onNavigateToForgotPassword
                )
            } else {
                // Register Screen
                RegisterContent(
                    fullName = fullName,
                    onFullNameChange = { fullName = it },
                    email = email,
                    onEmailChange = { email = it },
                    password = password,
                    onPasswordChange = { password = it },
                    confirmPassword = confirmPassword,
                    onConfirmPasswordChange = { confirmPassword = it },
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                    confirmPasswordVisible = confirmPasswordVisible,
                    onConfirmPasswordVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible },
                    errorMessage = errorMessage,
                    isLoading = isLoading,
                    onRegisterClick = {
                        errorMessage = ""
                        // Kiểm tra xem là email hay SĐT
                        val isEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                        val isPhone = email.matches(Regex("^[0-9]{10,11}$"))
                        
                        when {
                            fullName.isBlank() -> errorMessage = "Vui lòng nhập họ tên"
                            email.isBlank() -> errorMessage = "Vui lòng nhập email hoặc số điện thoại"
                            !isEmail && !isPhone -> errorMessage = "Email hoặc số điện thoại không hợp lệ"
                            password.isBlank() -> errorMessage = "Vui lòng nhập mật khẩu"
                            password.length < 6 -> errorMessage = "Mật khẩu phải có ít nhất 6 ký tự"
                            confirmPassword != password -> 
                                errorMessage = "Mật khẩu xác nhận không khớp"
                            else -> viewModel.register(fullName, email, password)
                        }
                    },
                    onSwitchToLogin = { isLoginMode = true }
                )
            }
        }
    }
}

@Composable
fun LoginContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    errorMessage: String,
    isLoading: Boolean,
    onLoginClick: (Boolean) -> Unit,
    onBiometricClick: () -> Unit = {},
    isBiometricAvailable: Boolean = false,
    onSwitchToRegister: () -> Unit,
    onForgotPassword: () -> Unit = {}
) {
    var rememberMe by remember { mutableStateOf(false) }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        // App Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF4A90E2)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Group,
                contentDescription = "App Icon",
                modifier = Modifier.size(40.dp),
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Chào mừng trở lại!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Quản lý dự án nhóm của bạn hiệu quả hơn",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
        )
        
        // Email Field
        Text(
            text = "Email hoặc Số điện thoại",
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            fontWeight = FontWeight.Medium
        )
        
        ModernTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = "Nhập email hoặc số điện thoại",
            keyboardType = KeyboardType.Text,
            isDark = false
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Password Field
        Text(
            text = "Mật khẩu",
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            fontWeight = FontWeight.Medium
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModernTextField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = "Nhập mật khẩu",
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = onPasswordVisibilityChange,
                isDark = false,
                modifier = Modifier.weight(1f)
            )
            
            // Biometric Button
            if (isBiometricAvailable) {
                IconButton(
                    onClick = onBiometricClick,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8F9FA))
                ) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = "Xác thực sinh trắc học",
                        tint = Color(0xFF4A90E2),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        
        // Remember Me & Forgot Password
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { rememberMe = !rememberMe }
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF4A90E2),
                        uncheckedColor = Color.Gray
                    )
                )
                Text(
                    text = "Ghi nhớ đăng nhập",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
            
            Text(
                text = "Quên mật khẩu?",
                fontSize = 14.sp,
                color = Color(0xFF4A90E2),
                modifier = Modifier.clickable { onForgotPassword() },
                textDecoration = TextDecoration.Underline
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Error Message
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color(0xFFFF6B6B),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // Login Button
        Button(
            onClick = { onLoginClick(rememberMe) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4A90E2)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Đăng nhập",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Social Login
        Text(
            text = "Hoặc đăng nhập bằng",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SocialButton(
                text = "Google",
                icon = Icons.Default.AccountCircle,
                onClick = { /* TODO */ }
            )
            SocialButton(
                text = "Facebook",
                icon = Icons.Default.Facebook,
                onClick = { /* TODO */ }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Switch to Register
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Chưa có tài khoản? ",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "Đăng ký ngay",
                fontSize = 14.sp,
                color = Color(0xFF4A90E2),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onSwitchToRegister() }
            )
        }
    }
}@Composable
fun RegisterContent(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    confirmPasswordVisible: Boolean,
    onConfirmPasswordVisibilityChange: () -> Unit,
    errorMessage: String,
    isLoading: Boolean,
    onRegisterClick: () -> Unit,
    onSwitchToLogin: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Header with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                    ),
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Tạo tài khoản mới",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Quản lý dự án nhóm hiệu quả cùng bạn\nbè và đồng nghiệp",
            fontSize = 14.sp,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )
        
        // Full Name Field
        Text(
            text = "Họ và tên",
            fontSize = 14.sp,
            color = Color(0xFF333333),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            fontWeight = FontWeight.Medium
        )
        
        ModernTextField(
            value = fullName,
            onValueChange = onFullNameChange,
            placeholder = "Nhập họ và tên của bạn",
            isDark = false
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Email or Phone Field
        Text(
            text = "Email hoặc Số điện thoại",
            fontSize = 14.sp,
            color = Color(0xFF333333),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            fontWeight = FontWeight.Medium
        )
        
        ModernTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = "Nhập email hoặc số điện thoại",
            keyboardType = KeyboardType.Text,
            isDark = false
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Password Field
        Text(
            text = "Mật khẩu",
            fontSize = 14.sp,
            color = Color(0xFF333333),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            fontWeight = FontWeight.Medium
        )
        
        ModernTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "Tối thiểu 6 ký tự",
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordVisibilityChange = onPasswordVisibilityChange,
            isDark = false
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Confirm Password Field
        Text(
            text = "Xác nhận mật khẩu",
            fontSize = 14.sp,
            color = Color(0xFF333333),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            fontWeight = FontWeight.Medium
        )
        
        ModernTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            placeholder = "Nhập lại mật khẩu",
            isPassword = true,
            passwordVisible = confirmPasswordVisible,
            onPasswordVisibilityChange = onConfirmPasswordVisibilityChange,
            isDark = false
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Error Message
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color(0xFFFF6B6B),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        
        // Register Button
        Button(
            onClick = onRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4A90E2)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Đăng ký",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Terms
        Text(
            text = "Bằng cách đăng ký, bạn đồng ý với Điều khoản sử dụng\nvà Chính sách bảo mật của chúng tôi",
            fontSize = 12.sp,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Switch to Login
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Đã có tài khoản? ",
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )
            Text(
                text = "Đăng nhập",
                fontSize = 14.sp,
                color = Color(0xFF4A90E2),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onSwitchToLogin() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: (() -> Unit)? = null,
    isDark: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { 
            Text(
                placeholder,
                color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF999999)
            ) 
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { onPasswordVisibilityChange?.invoke() }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                        tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF999999)
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) 
            PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF4A90E2),
            unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color(0xFFE0E0E0),
            focusedTextColor = if (isDark) Color.White else Color(0xFF333333),
            unfocusedTextColor = if (isDark) Color.White else Color(0xFF333333),
            cursorColor = Color(0xFF4A90E2),
            focusedContainerColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.White,
            unfocusedContainerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF8F9FA)
        )
    )
}

@Composable
fun SocialButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .width(150.dp)
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                icon,
                contentDescription = text,
                modifier = Modifier.size(20.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                maxLines = 1
            )
        }
    }
}
