package com.example.myapplication.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userPreferences = remember { com.example.myapplication.data.preferences.UserPreferences(context) }
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Tổng quan", "Biểu đồ", "Hoạt động", "Bạn bè")
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            RightDrawerContent(
                userPreferences = userPreferences,
                onClose = {
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        },
        gesturesEnabled = drawerState.isOpen
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = { 
                            Text(
                                "Trang chủ",
                                fontWeight = FontWeight.Bold
                            ) 
                        },
                        actions = {
                            IconButton(onClick = { /* TODO: Search */ }) {
                                Icon(Icons.Default.Search, "Tìm kiếm")
                            }
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Menu, "Menu")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    
                    // Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            when (selectedTab) {
                0 -> OverviewTab(paddingValues)
                1 -> ChartTab(paddingValues)
                2 -> ActivityTab(paddingValues)
                3 -> FriendsTab(paddingValues)
            }
        }
    }
}

@Composable
fun OverviewTab(paddingValues: PaddingValues) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userPreferences = remember { com.example.myapplication.data.preferences.UserPreferences(context) }
    val userName = remember { userPreferences.getFullName().ifEmpty { "Bạn" } }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Color(0xFFF8F9FA)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF667eea)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Xin chào, $userName! 👋",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hôm nay bạn có 4 nhiệm vụ cần hoàn thành",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
        
        // Stats Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    number = "5",
                    label = "Nhóm",
                    color = Color(0xFF4CAF50),
                    icon = Icons.Default.Group
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    number = "12",
                    label = "Nhiệm vụ",
                    color = Color(0xFF2196F3),
                    icon = Icons.Default.Assignment
                )
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    number = "8",
                    label = "Hoàn thành",
                    color = Color(0xFFFF9800),
                    icon = Icons.Default.CheckCircle
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    number = "4",
                    label = "Đang làm",
                    color = Color(0xFF9C27B0),
                    icon = Icons.Default.Schedule
                )
            }
        }
        
        // Priority Tasks Section
        item {
            Text(
                text = "Nhiệm vụ ưu tiên cao",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        items(2) { index ->
            PriorityTaskCard(
                title = if (index == 0) "Thiết kế UI Dashboard" else "Review code Backend API",
                deadline = if (index == 0) "Hôm nay, 17:00" else "Mai, 10:00",
                tags = if (index == 0) listOf("Design", "UI/UX") else listOf("Backend", "Code Review"),
                priority = "Cao"
            )
        }
        
        // Quick Actions
        item {
            Text(
                text = "Thao tác nhanh",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Add,
                    label = "Tạo nhóm",
                    color = Color(0xFF667eea)
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Assignment,
                    label = "Thêm nhiệm vụ",
                    color = Color(0xFF764ba2)
                )
            }
        }
        
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun ChartTab(paddingValues: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Thống kê tiến độ",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Progress Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Tiến độ tuần này",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Simple progress bars
                    ProgressItem("Hoàn thành", 8, 12, Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.height(12.dp))
                    ProgressItem("Đang làm", 4, 12, Color(0xFF2196F3))
                    Spacer(modifier = Modifier.height(12.dp))
                    ProgressItem("Chưa bắt đầu", 0, 12, Color(0xFFFF9800))
                }
            }
        }

        // Task by Priority
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Nhiệm vụ theo mức độ ưu tiên",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    PriorityItem("Cao", 3, Color(0xFFF44336))
                    Spacer(modifier = Modifier.height(12.dp))
                    PriorityItem("Trung bình", 6, Color(0xFFFF9800))
                    Spacer(modifier = Modifier.height(12.dp))
                    PriorityItem("Thấp", 3, Color(0xFF4CAF50))
                }
            }
        }
    }
}

@Composable
fun ActivityTab(paddingValues: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Lịch sử hoạt động",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item { ActivityItem("Hoàn thành nhiệm vụ 'Thiết kế UI'", "5 phút trước", Icons.Default.CheckCircle, Color(0xFF4CAF50)) }
        item { ActivityItem("Bình luận trong 'Review code'", "1 giờ trước", Icons.Default.Comment, Color(0xFF2196F3)) }
        item { ActivityItem("Thêm thành viên mới vào nhóm", "2 giờ trước", Icons.Default.PersonAdd, Color(0xFF9C27B0)) }
        item { ActivityItem("Tạo nhiệm vụ mới", "3 giờ trước", Icons.Default.Add, Color(0xFFFF9800)) }
        item { ActivityItem("Cập nhật deadline", "1 ngày trước", Icons.Default.Schedule, Color(0xFF607D8B)) }
    }
}

@Composable
fun ProgressItem(label: String, current: Int, total: Int, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 14.sp)
            Text(text = "$current/$total", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { current.toFloat() / total },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun PriorityItem(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 14.sp)
        }
        Text(
            text = "$count nhiệm vụ",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ActivityItem(title: String, time: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = time,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FriendsTab(paddingValues: PaddingValues) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Tất cả") }
    val filters = listOf("Tất cả", "Đang online", "Bạn thân")
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tìm kiếm bạn bè...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Tìm kiếm")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Xóa")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }
        
        // Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        leadingIcon = if (selectedFilter == filter) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }
        
        // Friend Requests Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lời mời kết bạn",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { /* TODO */ }) {
                    Text("Xem tất cả")
                }
            }
        }
        
        item {
            FriendRequestCard(
                name = "Nguyễn Văn A",
                mutualFriends = 12,
                avatarColor = Color(0xFF4CAF50),
                onAccept = { /* TODO */ },
                onDecline = { /* TODO */ }
            )
        }
        
        item {
            FriendRequestCard(
                name = "Trần Thị B",
                mutualFriends = 8,
                avatarColor = Color(0xFF2196F3),
                onAccept = { /* TODO */ },
                onDecline = { /* TODO */ }
            )
        }
        
        // Friends List Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Danh sách bạn bè (24)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { /* TODO: Add friend */ }) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Thêm bạn")
                }
            }
        }
        
        // Friends
        item { FriendCard("Lê Văn C", "Đang online", true, Color(0xFFFF9800)) }
        item { FriendCard("Phạm Thị D", "Đang online", true, Color(0xFF9C27B0)) }
        item { FriendCard("Hoàng Văn E", "Hoạt động 2 giờ trước", false, Color(0xFF00BCD4)) }
        item { FriendCard("Đỗ Thị F", "Hoạt động 5 giờ trước", false, Color(0xFFE91E63)) }
        item { FriendCard("Vũ Văn G", "Hoạt động hôm qua", false, Color(0xFF3F51B5)) }
        item { FriendCard("Bùi Thị H", "Hoạt động 2 ngày trước", false, Color(0xFF009688)) }
    }
}

@Composable
fun FriendRequestCard(
    name: String,
    mutualFriends: Int,
    avatarColor: Color,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(avatarColor, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$mutualFriends bạn chung",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A90E2)
                        )
                    ) {
                        Text("Chấp nhận", fontSize = 14.sp)
                    }
                    
                    OutlinedButton(
                        onClick = onDecline,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Từ chối", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendCard(
    name: String,
    status: String,
    isOnline: Boolean,
    avatarColor: Color
) {
    Card(
        onClick = { /* TODO: Open chat or profile */ },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with online indicator
            Box {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(avatarColor, RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                if (isOnline) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd)
                            .background(Color(0xFF4CAF50), RoundedCornerShape(7.dp))
                            .padding(2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = status,
                    fontSize = 14.sp,
                    color = if (isOnline) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
fun RightDrawerContent(
    userPreferences: com.example.myapplication.data.preferences.UserPreferences,
    onClose: () -> Unit
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.8f), // Chiếm 80% màn hình
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // User Profile Header
            UserProfileHeader(
                userPreferences = userPreferences,
                onEditClick = { showEditProfileDialog = true },
                onClose = onClose
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Menu Items
                DrawerMenuItem(
                    icon = Icons.Default.Home,
                    title = "Trang chủ",
                    onClick = { /* TODO */ }
                )
                
                DrawerMenuItem(
                    icon = Icons.Default.Group,
                    title = "Nhóm của tôi",
                    onClick = { /* TODO */ }
                )
                
                DrawerMenuItem(
                    icon = Icons.Default.Assignment,
                    title = "Nhiệm vụ",
                    onClick = { /* TODO */ }
                )
                
                DrawerMenuItem(
                    icon = Icons.Default.CalendarToday,
                    title = "Lịch",
                    onClick = { /* TODO */ }
                )
                
                DrawerMenuItem(
                    icon = Icons.Default.Notifications,
                    title = "Thông báo",
                    onClick = { /* TODO */ }
                )
                
                DrawerMenuItem(
                    icon = Icons.Default.Settings,
                    title = "Cài đặt",
                    onClick = { /* TODO */ }
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Footer
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                DrawerMenuItem(
                    icon = Icons.Default.Help,
                    title = "Trợ giúp",
                    onClick = { /* TODO */ }
                )
                
                DrawerMenuItem(
                    icon = Icons.Default.Info,
                    title = "Về ứng dụng",
                    onClick = { /* TODO */ }
                )
            }
        }
    }
    
    // Edit Profile Dialog
    if (showEditProfileDialog) {
        EditProfileDialog(
            userPreferences = userPreferences,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, bio ->
                // TODO: Save profile to server
                showEditProfileDialog = false
            }
        )
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Composable
fun UserProfileHeader(
    userPreferences: com.example.myapplication.data.preferences.UserPreferences,
    onEditClick: () -> Unit,
    onClose: () -> Unit
) {
    val fullName = remember { userPreferences.getFullName() }
    val email = remember { userPreferences.getEmail() }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = Color.White
                    )
                }
            }
            
            // Avatar and Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with edit button
                Box(
                    modifier = Modifier.size(80.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color.White.copy(alpha = 0.3f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Avatar",
                                modifier = Modifier.size(40.dp),
                                tint = Color.White
                            )
                        }
                    }
                    
                    // Edit button on avatar
                    Surface(
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.BottomEnd),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color.White,
                        onClick = onEditClick
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Chỉnh sửa",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF667eea)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // User info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = fullName.ifEmpty { "Người dùng" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = email.ifEmpty { "Chưa có thông tin" },
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        onClick = onEditClick
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Chỉnh sửa",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    userPreferences: com.example.myapplication.data.preferences.UserPreferences,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(userPreferences.getFullName()) }
    var bio by remember { mutableStateOf("") }
    var showImagePicker by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Chỉnh sửa thông tin",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box {
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = Color(0xFF667eea).copy(alpha = 0.2f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.size(50.dp),
                                    tint = Color(0xFF667eea)
                                )
                            }
                        }
                        
                        // Camera button
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .align(Alignment.BottomEnd),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = Color(0xFF667eea),
                            onClick = { showImagePicker = true }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Đổi ảnh",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
                
                Text(
                    text = "Chọn ảnh từ thư viện hoặc chụp ảnh mới",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Họ và tên") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Bio field
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Giới thiệu bản thân") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = null)
                    },
                    placeholder = { Text("Viết vài dòng về bạn...") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, bio) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667eea)
                )
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
    
    // Image picker dialog
    if (showImagePicker) {
        AlertDialog(
            onDismissRequest = { showImagePicker = false },
            title = { Text("Chọn ảnh đại diện") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            // TODO: Open camera
                            showImagePicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Chụp ảnh")
                        }
                    }
                    
                    TextButton(
                        onClick = {
                            // TODO: Open gallery
                            showImagePicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Chọn từ thư viện")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImagePicker = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}


@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    number: String,
    label: String,
    color: Color,
    icon: ImageVector
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = number,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
fun PriorityTaskCard(
    title: String,
    deadline: String,
    tags: List<String>,
    priority: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = priority,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE53935),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color(0xFF666666),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = deadline,
                    fontSize = 13.sp,
                    color = Color(0xFF666666)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE3F2FD)
                    ) {
                        Text(
                            text = tag,
                            fontSize = 12.sp,
                            color = Color(0xFF1976D2),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}
