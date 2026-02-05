package com.example.myapplication.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.viewmodel.AuthViewModel
import com.example.myapplication.ui.viewmodel.ResetPasswordState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    email: String,
    onBackToLogin: () -> Unit,
    onResetSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var verificationCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val resetPasswordState by viewModel.resetPasswordState.collectAsState()
    
    LaunchedEffect(resetPasswordState) {
        when (resetPasswordState) {
            is ResetPasswordState.Success -> {
                onResetSuccess()
                viewModel.resetForgotPasswordState()
            }
            is ResetPasswordState.Error -> {
                errorMessage = (resetPasswordState as ResetPasswordState.Error).message
            }
            else -> {}
        }
    }
    
    val isLoading = resetPasswordState is ResetPasswordState.Loading

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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackToLogin) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color(0xFF4A90E2)
                    )
                }
                Text(
                    text = "Quay lại đăng nhập",
                    fontSize = 16.sp,
                    color = Color(0xFF4A90E2),
                    modifier = Modifier.clickable { onBackToLogin() }
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        Color(0xFF4A90E2).copy(alpha = 0.1f),
                        RoundedCornerShape(50.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.VpnKey,
                    contentDescription = "Đặt lại mật khẩu",
                    modifier = Modifier.size(50.dp),
                    tint = Color(0xFF4A90E2)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Đặt lại mật khẩu",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Nhập mã xác nhận đã được gửi đến\n$email",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp, bottom = 40.dp)
            )
            
            // Verification Code Field
            Text(
                text = "Mã xác nhận",
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                fontWeight = FontWeight.Medium
            )
            
            OutlinedTextField(
                value = verificationCode,
                onValueChange = { 
                    if (it.length <= 6) {
                        verificationCode = it
                        errorMessage = ""
                    }
                },
                placeholder = { 
                    Text("Nhập mã 6 số", color = Color(0xFF999999)) 
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Pin,
                        contentDescription = "Mã xác nhận",
                        tint = Color(0xFF4A90E2)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4A90E2),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedTextColor = Color(0xFF333333),
                    unfocusedTextColor = Color(0xFF333333),
                    cursorColor = Color(0xFF4A90E2),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFF8F9FA)
                )
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // New Password Field
            Text(
                text = "Mật khẩu mới",
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                fontWeight = FontWeight.Medium
            )
            
            OutlinedTextField(
                value = newPassword,
                onValueChange = { 
                    newPassword = it
                    errorMessage = ""
                },
                placeholder = { 
                    Text("Tối thiểu 6 ký tự", color = Color(0xFF999999)) 
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Mật khẩu",
                        tint = Color(0xFF4A90E2)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                            tint = Color(0xFF999999)
                        )
                    }
                },
                visualTransformation = if (passwordVisible) 
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4A90E2),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedTextColor = Color(0xFF333333),
                    unfocusedTextColor = Color(0xFF333333),
                    cursorColor = Color(0xFF4A90E2),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFF8F9FA)
                )
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Confirm Password Field
            Text(
                text = "Xác nhận mật khẩu",
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                fontWeight = FontWeight.Medium
            )
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    errorMessage = ""
                },
                placeholder = { 
                    Text("Nhập lại mật khẩu mới", color = Color(0xFF999999)) 
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Xác nhận mật khẩu",
                        tint = Color(0xFF4A90E2)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                            tint = Color(0xFF999999)
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) 
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4A90E2),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedTextColor = Color(0xFF333333),
                    unfocusedTextColor = Color(0xFF333333),
                    cursorColor = Color(0xFF4A90E2),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFF8F9FA)
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Error Message
            if (errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Lỗi",
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = errorMessage,
                            color = Color(0xFFFF6B6B),
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            // Reset Button
            Button(
                onClick = {
                    errorMessage = ""
                    when {
                        verificationCode.isBlank() -> errorMessage = "Vui lòng nhập mã xác nhận"
                        verificationCode.length != 6 -> errorMessage = "Mã xác nhận phải có 6 số"
                        newPassword.isBlank() -> errorMessage = "Vui lòng nhập mật khẩu mới"
                        newPassword.length < 6 -> errorMessage = "Mật khẩu phải có ít nhất 6 ký tự"
                        confirmPassword != newPassword -> errorMessage = "Mật khẩu xác nhận không khớp"
                        else -> viewModel.resetPassword(email, verificationCode, newPassword)
                    }
                },
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
                        text = "Đặt lại mật khẩu",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Resend Code
            TextButton(
                onClick = { viewModel.forgotPassword(email) }
            ) {
                Text(
                    text = "Gửi lại mã xác nhận",
                    fontSize = 14.sp,
                    color = Color(0xFF4A90E2)
                )
            }
        }
    }
}
