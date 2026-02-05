package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.viewmodel.GroupViewModel
import com.example.myapplication.ui.screen.home.*
import com.example.myapplication.ui.screen.group.*
import com.example.myapplication.ui.screen.account.*

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", "Trang chủ", Icons.Default.Home)
    object Groups : BottomNavItem("groups", "Nhóm", Icons.Default.Group)
    object Notifications : BottomNavItem("notifications", "Thông báo", Icons.Default.Notifications)
    object Account : BottomNavItem("account", "Tài khoản", Icons.Default.AccountCircle)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    viewModel: GroupViewModel = viewModel(),
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Groups,
        BottomNavItem.Notifications,
        BottomNavItem.Account
    )
    
    // Load groups khi vào MainScreen
    LaunchedEffect(Unit) {
        viewModel.loadGroups()
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
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
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> HomeScreen()
                1 -> GroupListScreen(
                    viewModel = viewModel,
                    onGroupClick = { group ->
                        navController.navigate("group_detail/${group.id}")
                    },
                    onAddGroupClick = {
                        navController.navigate("add_group")
                    }
                )
                2 -> NotificationScreen()
                3 -> AccountScreen(
                    viewModel = viewModel,
                    onLogout = onLogout
                )
            }
        }
    }
}
