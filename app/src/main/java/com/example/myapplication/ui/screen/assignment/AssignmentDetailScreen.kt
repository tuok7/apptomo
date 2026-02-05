package com.example.myapplication.ui.screen.assignment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.api.AssignmentData
import com.example.myapplication.data.api.MemberData
import com.example.myapplication.ui.viewmodel.GroupViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentDetailScreen(
    viewModel: GroupViewModel,
    assignmentId: Long,
    onBackClick: () -> Unit
) {
    val assignments by viewModel.assignments.collectAsState()
    val members by viewModel.members.collectAsState()
    val assignment = assignments.find { it.id == assignmentId }
    
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(assignment?.title ?: "Chi tiết bài tập") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        assignment?.let { assign ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Mô tả", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = assign.description.ifEmpty { "Không có mô tả" },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Hạn chót", style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        assign.dueDate ?: "Không có",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Column {
                                    Text("Trạng thái", style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        getStatusText(assign.status),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Column {
                                    Text("Độ ưu tiên", style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        getPriorityText(assign.priority),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
                
                item {
                    Text(
                        "Thành viên được phân công (${assign.assignedMembers.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                if (assign.assignedMembers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Chưa phân công thành viên nào")
                        }
                    }
                } else {
                    items(assign.assignedMembers, key = { it.id }) { member ->
                        AssignedMemberCard(member = member)
                    }
                }
            }
        }
    }
}

@Composable
fun AssignedMemberCard(member: MemberData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = member.fullName, style = MaterialTheme.typography.titleMedium)
                Text(text = member.email, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun getStatusText(status: String): String {
    return when (status.lowercase()) {
        "todo" -> "Chưa làm"
        "in_progress" -> "Đang làm"
        "completed" -> "Hoàn thành"
        else -> status
    }
}

private fun getPriorityText(priority: String): String {
    return when (priority.lowercase()) {
        "low" -> "Thấp"
        "medium" -> "Trung bình"
        "high" -> "Cao"
        else -> priority
    }
}
