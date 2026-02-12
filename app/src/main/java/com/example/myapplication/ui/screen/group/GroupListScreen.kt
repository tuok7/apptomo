package com.example.myapplication.ui.screen.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.api.GroupData
import com.example.myapplication.ui.viewmodel.GroupViewModel
import com.example.myapplication.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupListScreen(
    viewModel: GroupViewModel,
    onGroupClick: (GroupData) -> Unit,
    onAddGroupClick: () -> Unit,
    onJoinGroupClick: () -> Unit = {}
) {
    val groups by viewModel.groups.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Search and filter states
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(SortOption.NAME) }
    var showFilterMenu by remember { mutableStateOf(false) }
    
    // Load groups khi vào màn hình
    LaunchedEffect(Unit) {
        viewModel.loadGroups()
    }
    
    val onRefresh: () -> Unit = {
        scope.launch {
            isRefreshing = true
            viewModel.loadGroups()
            delay(1000)
            isRefreshing = false
        }
    }
    
    // Filter and sort groups
    val filteredGroups = remember(groups, searchQuery, sortOption) {
        var result = groups
        
        // Search filter
        if (searchQuery.isNotEmpty()) {
            result = result.filter { 
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }
        }
        
        // Sort
        result = when (sortOption) {
            SortOption.NAME -> result.sortedBy { it.name }
            SortOption.MEMBER_COUNT -> result.sortedByDescending { it.memberCount }
            SortOption.RECENT -> result.sortedByDescending { it.id }
        }
        
        result
    }
    
    // Show error message
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        if (showSearchBar) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Tìm kiếm nhóm...") },
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
                            Text(
                                "Quản lý bài tập nhóm",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    actions = {
                        if (showSearchBar) {
                            IconButton(onClick = { 
                                showSearchBar = false
                                searchQuery = ""
                            }) {
                                Icon(Icons.Default.Close, "Đóng tìm kiếm")
                            }
                        } else {
                            IconButton(onClick = { showSearchBar = true }) {
                                Icon(Icons.Default.Search, "Tìm kiếm")
                            }
                        }
                        
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, "Sắp xếp")
                        }
                        
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Theo tên") },
                                onClick = {
                                    sortOption = SortOption.NAME
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (sortOption == SortOption.NAME) {
                                        Icon(Icons.Default.Check, null)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Theo số thành viên") },
                                onClick = {
                                    sortOption = SortOption.MEMBER_COUNT
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (sortOption == SortOption.MEMBER_COUNT) {
                                        Icon(Icons.Default.Check, null)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Gần đây") },
                                onClick = {
                                    sortOption = SortOption.RECENT
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (sortOption == SortOption.RECENT) {
                                        Icon(Icons.Default.Check, null)
                                    }
                                }
                            )
                        }
                        
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(Icons.Default.FilterList, "Lọc")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFE3F2FD),
                        titleContentColor = Color(0xFF1976D2)
                    )
                )
                
                // Statistics bar
                if (!showSearchBar && groups.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF5F5F5)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                icon = Icons.Default.Group,
                                label = "Tổng nhóm",
                                value = groups.size.toString(),
                                color = Color(0xFF2196F3)
                            )
                            StatItem(
                                icon = Icons.Default.Person,
                                label = "Thành viên",
                                value = groups.sumOf { it.memberCount }.toString(),
                                color = Color(0xFF4CAF50)
                            )
                            StatItem(
                                icon = Icons.Default.Assignment,
                                label = "Bài tập",
                                value = "0",
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Join Group FAB
                SmallFloatingActionButton(
                    onClick = onJoinGroupClick,
                    containerColor = Color(0xFF4CAF50)
                ) {
                    Icon(Icons.Default.Login, contentDescription = "Tham gia nhóm", tint = Color.White)
                }
                
                // Create Group FAB
                FloatingActionButton(
                    onClick = onAddGroupClick,
                    containerColor = Color(0xFF2196F3)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tạo nhóm", tint = Color.White)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(5) {
                        SkeletonGroupItem()
                    }
                }
            }
            filteredGroups.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    if (searchQuery.isNotEmpty()) {
                        EmptySearchState(query = searchQuery)
                    } else {
                        EmptyGroupsState(onCreateGroup = onAddGroupClick)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredGroups, key = { it.id }) { group ->
                        SwipeToDeleteItem(
                            onDelete = { 
                                scope.launch {
                                    snackbarHostState.showSnackbar("Đã xóa nhóm ${group.name}")
                                }
                            }
                        ) {
                            EnhancedGroupCard(
                                group = group,
                                onClick = { onGroupClick(group) },
                                onDelete = { 
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Đã xóa nhóm ${group.name}")
                                    }
                                }
                            )
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun EnhancedGroupCard(
    group: GroupData,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Group Avatar
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE3F2FD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = group.name.take(2).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        if (group.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = group.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.Gray
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Chỉnh sửa") },
                            onClick = { 
                                showMenu = false
                                // TODO: Navigate to edit
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Chia sẻ") },
                            onClick = { 
                                showMenu = false
                                // TODO: Share group
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Share, null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Ghim") },
                            onClick = { 
                                showMenu = false
                                // TODO: Pin group
                            },
                            leadingIcon = {
                                Icon(Icons.Default.PushPin, null)
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Xóa", color = Color.Red) },
                            onClick = { 
                                showMenu = false
                                showDeleteDialog = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, null, tint = Color.Red)
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Group stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GroupStatChip(
                    icon = Icons.Default.Person,
                    text = "${group.memberCount} thành viên",
                    color = Color(0xFF4CAF50)
                )
                
                GroupStatChip(
                    icon = Icons.Default.Assignment,
                    text = "0 bài tập",
                    color = Color(0xFFFF9800)
                )
                
                GroupStatChip(
                    icon = Icons.Default.Message,
                    text = "0 tin nhắn",
                    color = Color(0xFF2196F3)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF2196F3)
                    )
                ) {
                    Icon(
                        Icons.Default.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nhắn tin", style = MaterialTheme.typography.labelLarge)
                }
                
                OutlinedButton(
                    onClick = { /* TODO: View members */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Thành viên", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { 
                Text(
                    "Xác nhận xóa",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { 
                Text(
                    "Bạn có chắc muốn xóa nhóm \"${group.name}\"? Tất cả thành viên, bài tập và tin nhắn sẽ bị xóa vĩnh viễn.",
                    style = MaterialTheme.typography.bodyMedium
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun GroupStatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EmptySearchState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.Gray.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Không tìm thấy kết quả",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Không có nhóm nào phù hợp với \"$query\"",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

enum class SortOption {
    NAME,
    MEMBER_COUNT,
    RECENT
}
