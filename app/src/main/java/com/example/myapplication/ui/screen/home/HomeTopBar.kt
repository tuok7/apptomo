package com.example.myapplication.ui.screen.home

import androidx.compose.foundation.layout.*
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
    Column {
        TopAppBar(
            title = { 
                if (showSearch) {
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Tìm kiếm...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                } else {
                    Column {
                        Text(
                            "Trang chủ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Chào buổi sáng! ☀️",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1976D2).copy(alpha = 0.7f)
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = onSearchToggle) {
                    Icon(
                        if (showSearch) Icons.Default.Close else Icons.Default.Search,
                        if (showSearch) "Đóng" else "Tìm kiếm"
                    )
                }
                if (!showSearch) {
                    IconButton(onClick = { /* TODO: Notifications */ }) {
                        Badge(containerColor = Color(0xFFF44336)) {
                            Icon(Icons.Default.Notifications, "Thông báo")
                        }
                    }
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, "Menu")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFE3F2FD),
                titleContentColor = Color(0xFF1976D2)
            )
        )
        
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFFE3F2FD),
            contentColor = Color(0xFF2196F3),
            edgePadding = 16.dp
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    text = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(tab.icon, null, modifier = Modifier.size(18.dp))
                            Text(
                                tab.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
            }
        }
    }
}
