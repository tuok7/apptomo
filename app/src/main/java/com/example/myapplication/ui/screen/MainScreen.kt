package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.viewmodel.GroupViewModel
import com.example.myapplication.ui.screen.home.HomeScreen
import com.example.myapplication.ui.screen.group.GroupListScreen
import com.example.myapplication.ui.screen.schedule.ScheduleScreen
import com.example.myapplication.ui.screen.settings.SettingsScreen

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", "Trang chủ", Icons.Default.Home)
    object Groups : BottomNavItem("groups", "Nhóm", Icons.Default.Group)
    object Schedule : BottomNavItem("schedule", "Lịch trình", Icons.Default.Event)
    object Settings : BottomNavItem("settings", "Cài đặt", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    viewModel: GroupViewModel = viewModel(),
    onLogout: () -> Unit = {},
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Groups,
        BottomNavItem.Schedule,
        BottomNavItem.Settings
    )
    
    // Load groups khi vào MainScreen
    LaunchedEffect(Unit) {
        viewModel.loadGroups()
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF6366F1),
                            selectedTextColor = Color(0xFF6366F1),
                            indicatorColor = Color(0xFF6366F1).copy(alpha = 0.1f),
                            unselectedIconColor = Color(0xFF9CA3AF),
                            unselectedTextColor = Color(0xFF9CA3AF)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> HomeScreen(navController = navController)
                1 -> GroupListScreen(
                    viewModel = viewModel,
                    onGroupClick = { group ->
                        navController.navigate("group_detail/${group.id}")
                    },
                    onAddGroupClick = {
                        navController.navigate("add_group")
                    },
                    onJoinGroupClick = {
                        navController.navigate("join_group")
                    }
                )
                2 -> ScheduleScreen(navController = navController)
                3 -> {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val userPreferences = remember { com.example.myapplication.data.preferences.UserPreferences(context) }
                    SettingsScreen(
                        navController = navController,
                        userPreferences = userPreferences
                    )
                }
            }
        }
    }
}
