package com.example.myapplication.ui.screen.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDataScreen(
    onBackClick: () -> Unit
) {
    var selectedFormat by remember { mutableStateOf("PDF") }
    var selectedData by remember { mutableStateOf(setOf("groups", "tasks")) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Xuất dữ liệu",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Format Selection
            item {
                Text(
                    text = "Chọn định dạng",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FormatCard(
                        format = "PDF",
                        icon = Icons.Default.PictureAsPdf,
                        isSelected = selectedFormat == "PDF",
                        onClick = { selectedFormat = "PDF" },
                        modifier = Modifier.weight(1f)
                    )
                    FormatCard(
                        format = "Excel",
                        icon = Icons.Default.TableChart,
                        isSelected = selectedFormat == "Excel",
                        onClick = { selectedFormat = "Excel" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Data Selection
            item {
                Text(
                    text = "Chọn dữ liệu cần xuất",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                DataCheckboxItem(
                    title = "Nhóm",
                    description = "Thông tin các nhóm",
                    isChecked = selectedData.contains("groups"),
                    onCheckedChange = {
                        selectedData = if (it) selectedData + "groups" else selectedData - "groups"
                    }
                )
            }
            
            item {
                DataCheckboxItem(
                    title = "Nhiệm vụ",
                    description = "Danh sách nhiệm vụ và tiến độ",
                    isChecked = selectedData.contains("tasks"),
                    onCheckedChange = {
                        selectedData = if (it) selectedData + "tasks" else selectedData - "tasks"
                    }
                )
            }
            
            item {
                DataCheckboxItem(
                    title = "Thành viên",
                    description = "Thông tin thành viên",
                    isChecked = selectedData.contains("members"),
                    onCheckedChange = {
                        selectedData = if (it) selectedData + "members" else selectedData - "members"
                    }
                )
            }
            
            item {
                DataCheckboxItem(
                    title = "Tin nhắn",
                    description = "Lịch sử chat nhóm",
                    isChecked = selectedData.contains("messages"),
                    onCheckedChange = {
                        selectedData = if (it) selectedData + "messages" else selectedData - "messages"
                    }
                )
            }
            
            // Export Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "File sẽ được lưu vào thư mục Downloads",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            
            // Export Button
            item {
                Button(
                    onClick = { showSuccessDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedData.isNotEmpty()
                ) {
                    Icon(Icons.Default.Download, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Xuất dữ liệu")
                }
            }
        }
    }
    
    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Xuất dữ liệu thành công!") },
            text = { Text("File đã được lưu vào thư mục Downloads") },
            confirmButton = {
                Button(onClick = { showSuccessDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun FormatCard(
    format: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
        else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = format,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun DataCheckboxItem(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
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
