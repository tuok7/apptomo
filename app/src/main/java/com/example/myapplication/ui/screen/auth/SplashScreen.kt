package com.example.myapplication.ui.screen.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    onAutoLoginSuccess: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userPreferences = remember { com.example.myapplication.data.preferences.UserPreferences(context) }
    
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    var isAnimationComplete by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // Animation
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(1000)
            )
            isAnimationComplete = true
        }
        
        delay(2500) // Hiển thị splash trong 2.5 giây
        
        // Kiểm tra auto-login
        if (userPreferences.isLoggedIn() && userPreferences.isRememberLogin()) {
            onAutoLoginSuccess()
        } else {
            onSplashFinished()
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
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(enabled = isAnimationComplete) {
                    onSplashFinished()
                }
                .padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = "Tomo Logo",
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value),
                tint = Color.White
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "TOMO",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.scale(alpha.value)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Bài tập nhóm thông minh",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.scale(alpha.value)
            )
            
            if (isAnimationComplete) {
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Nhấn để tiếp tục",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.scale(alpha.value)
                )
            }
        }
    }
}
