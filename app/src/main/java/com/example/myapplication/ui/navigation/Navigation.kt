package com.example.myapplication.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.ui.screen.MainScreen
import com.example.myapplication.ui.screen.auth.*
import com.example.myapplication.ui.screen.group.*
import com.example.myapplication.ui.screen.assignment.*
import com.example.myapplication.ui.viewmodel.GroupViewModel

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password/{email}") {
        fun createRoute(email: String) = "reset_password/$email"
    }
    object Main : Screen("main")
    object GroupDetail : Screen("group_detail/{groupId}") {
        fun createRoute(groupId: Long) = "group_detail/$groupId"
    }
    object AddGroup : Screen("add_group")
    object AddMember : Screen("add_member/{groupId}") {
        fun createRoute(groupId: Long) = "add_member/$groupId"
    }
    object AddAssignment : Screen("add_assignment/{groupId}") {
        fun createRoute(groupId: Long) = "add_assignment/$groupId"
    }
    object AssignmentDetail : Screen("assignment_detail/{assignmentId}") {
        fun createRoute(assignmentId: Long) = "assignment_detail/$assignmentId"
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
                    // Reload groups khi auto login
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
            route = Screen.GroupDetail.route,
            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L
            if (groupId > 0) {
                GroupDetailScreen(
                    viewModel = viewModel,
                    groupId = groupId,
                    onBackClick = { navController.popBackStack() },
                    onAddMemberClick = { navController.navigate(Screen.AddMember.createRoute(groupId)) },
                    onAddAssignmentClick = { navController.navigate(Screen.AddAssignment.createRoute(groupId)) },
                    onAssignmentClick = { assignmentId ->
                        navController.navigate(Screen.AssignmentDetail.createRoute(assignmentId))
                    }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ID nhóm không hợp lệ")
                }
            }
        }
        
        composable(Screen.AddGroup.route) {
            AddGroupScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onGroupAdded = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.AddMember.route,
            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getLong("groupId") ?: return@composable
            AddMemberScreen(
                viewModel = viewModel,
                groupId = groupId,
                onBackClick = { navController.popBackStack() },
                onMemberAdded = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.AddAssignment.route,
            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getLong("groupId") ?: return@composable
            AddAssignmentScreen(
                viewModel = viewModel,
                groupId = groupId,
                onBackClick = { navController.popBackStack() },
                onAssignmentAdded = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.AssignmentDetail.route,
            arguments = listOf(navArgument("assignmentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val assignmentId = backStackEntry.arguments?.getLong("assignmentId") ?: return@composable
            AssignmentDetailScreen(
                viewModel = viewModel,
                assignmentId = assignmentId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
