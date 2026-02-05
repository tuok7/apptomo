package com.example.myapplication.ui.screen.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.preferences.UserPreferences
import com.example.myapplication.ui.viewmodel.AuthState
import com.example.myapplication.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    onAutoLoginSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val viewModel: AuthViewModel = viewModel()
    val authState by viewModel.authState.collectAsState()
    
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    var isCheckingLogin by remember { mutableStateOf(true) }
    
    // Xử lý kết quả đăng nhập tự động
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                onAutoLoginSuccess()
            }
            is AuthState.Error -> {
                // Nếu đăng nhập tự động thất bại, chuyển sang màn hình đăng nhập
                onSplashFinished()
            }
            else -> {}
        }
    }
    
    LaunchedEffect(Unit) {
        // Animation
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000)
        )
        
        delay(1500)
        
        // Luôn chuyển sang màn hình đăng nhập
        // Không tự động đăng nhập, chỉ điền sẵn thông tin
        onSplashFinished()
        isCheckingLogin = false
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E88E5),
                        Color(0xFF7E57C2)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value),
                tint = Color.White
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Tomo",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.scale(scale.value)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Bài tập nhóm",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.scale(scale.value)
            )
            
            if (isCheckingLogin) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Đang khởi động...",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}
