package com.example.myapplication.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    showSearch: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchToggle: () -> Unit,
    onMenuClick: () -> Unit,
    tabs: List<TabItem>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    // Simple header theo mẫu StudyFlow
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        TopAppBar(
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Logo StudyFlow
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                Color(0xFF6366F1),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "S",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "StudyFlow",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                }
            },
            actions = {
                IconButton(onClick = onSearchToggle) {
                    Icon(
                        Icons.Default.Search,
                        "Tìm kiếm",
                        tint = Color(0xFF6B7280)
                    )
                }
                Box {
                    IconButton(onClick = { /* TODO: Notifications */ }) {
                        Icon(
                            Icons.Default.Notifications, 
                            "Thông báo",
                            tint = Color(0xFF6B7280)
                        )
                    }
                    // Notification badge
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFEF4444), CircleShape)
                            .offset(x = 4.dp, y = 4.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color(0xFF1F2937)
            )
        )
    }
}
