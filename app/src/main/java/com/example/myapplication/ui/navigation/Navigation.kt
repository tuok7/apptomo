package com.example.myapplication.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.ui.screen.MainScreen
import com.example.myapplication.ui.screen.auth.AuthScreen
import com.example.myapplication.ui.screen.auth.ForgotPasswordScreen
import com.example.myapplication.ui.screen.auth.ResetPasswordScreen
import com.example.myapplication.ui.screen.auth.SplashScreen
import com.example.myapplication.ui.screen.group.AddGroupScreen
import com.example.myapplication.ui.screen.group.AddMemberScreen
import com.example.myapplication.ui.screen.group.GroupDetailScreen
import com.example.myapplication.ui.screen.group.JoinGroupScreen
import com.example.myapplication.ui.screen.assignment.AddAssignmentScreen
import com.example.myapplication.ui.screen.assignment.AssignmentDetailScreen
import com.example.myapplication.ui.screen.settings.SettingsScreen
import com.example.myapplication.ui.screen.schedule.ScheduleScreen
import com.example.myapplication.ui.viewmodel.GroupViewModel

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password/{email}") {
        fun createRoute(email: String) = "reset_password/$email"
    }
    object Main : Screen("main")
    object MainWithTab : Screen("main/{tab}") {
        fun createRoute(tab: Int) = "main/$tab"
    }
    object Settings : Screen("settings")
    object Schedule : Screen("schedule")
    object GroupDetail : Screen("group_detail/{groupId}") {
        fun createRoute(groupId: Long) = "group_detail/$groupId"
    }
    object AddGroup : Screen("add_group")
    object JoinGroup : Screen("join_group")
    object AddMember : Screen("add_member/{groupId}") {
        fun createRoute(groupId: Long) = "add_member/$groupId"
    }
    object AddAssignment : Screen("add_assignment/{groupId}") {
        fun createRoute(groupId: Long) = "add_assignment/$groupId"
    }
    object AssignmentDetail : Screen("assignment_detail/{assignmentId}") {
        fun createRoute(assignmentId: Long) = "assignment_detail/$assignmentId"
    }
    object GroupChat : Screen("group_chat/{groupId}/{groupName}") {
        fun createRoute(groupId: Long, groupName: String) = "group_chat/$groupId/$groupName"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: GroupViewModel = viewModel()
) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onAutoLoginSuccess = {
                    // Auto login thành công, reload groups và chuyển đến Main
                    viewModel.loadGroups()
                    navController.navigate(Screen.Main.route) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Auth.route) {
            AuthScreen(
                onLoginSuccess = {
                    // Reload groups sau khi login
                    viewModel.loadGroups()
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }
        
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBackToLogin = {
                    navController.popBackStack()
                },
                onNavigateToReset = { email ->
                    navController.navigate(Screen.ResetPassword.createRoute(email))
                }
            )
        }
        
        composable(
            route = Screen.ResetPassword.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: return@composable
            ResetPasswordScreen(
                email = email,
                onBackToLogin = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                },
                onResetSuccess = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Main.route) {
            MainScreen(
                navController = navController, 
                viewModel = viewModel,
                onLogout = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.MainWithTab.route,
            arguments = listOf(navArgument("tab") { type = NavType.IntType })
        ) { backStackEntry ->
            val tab = backStackEntry.arguments?.getInt("tab") ?: 0
            MainScreen(
                navController = navController, 
                viewModel = viewModel,
                initialTab = tab,
                onLogout = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // Các screen khác có thể thêm sau
        composable(Screen.Settings.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val userPreferences = remember { com.example.myapplication.data.preferences.UserPreferences(context) }
            SettingsScreen(
                navController = navController,
                userPreferences = userPreferences
            )
        }
        
        composable(Screen.Schedule.route) {
            ScheduleScreen(navController = navController)
        }
    }
}
