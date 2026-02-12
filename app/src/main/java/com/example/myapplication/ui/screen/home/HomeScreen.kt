package com.example.myapplication.ui.screen.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch

data class TabItem(val title: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: androidx.navigation.NavHostController? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userPreferences = remember { com.example.myapplication.data.preferences.UserPreferences(context) }
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        TabItem("Tổng quan", Icons.Default.Dashboard),
        TabItem("Biểu đồ", Icons.Default.BarChart),
        TabItem("Hoạt động", Icons.Default.Timeline),
        TabItem("Bạn bè", Icons.Default.People)
    )
    
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(
                userPreferences = userPreferences,
                onClose = { scope.launch { drawerState.close() } }
            )
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = {
                HomeTopBar(
                    showSearch = showSearch,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onSearchToggle = { 
                        showSearch = !showSearch
                        if (!showSearch) searchQuery = ""
                    },
                    onMenuClick = { scope.launch { drawerState.open() } },
                    tabs = tabs,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        ) { paddingValues ->
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
                },
                label = "tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> OverviewTab(paddingValues, userPreferences, navController)
                    1 -> ChartTab(paddingValues)
                    2 -> ActivityTab(paddingValues)
                    3 -> FriendsTab(paddingValues)
                }
            }
        }
    }
}
