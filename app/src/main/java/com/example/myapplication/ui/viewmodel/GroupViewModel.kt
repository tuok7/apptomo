package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.*
import com.example.myapplication.data.model.Priority
import com.example.myapplication.data.preferences.UserPreferences
import com.example.myapplication.data.repository.GroupRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GroupViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GroupRepository()
    private val userPreferences = UserPreferences(application)
    
    private val _uiState = MutableStateFlow(GroupUiState())
    val uiState: StateFlow<GroupUiState> = _uiState.asStateFlow()
    
    private val _groups = MutableStateFlow<List<GroupData>>(emptyList())
    val groups: StateFlow<List<GroupData>> = _groups.asStateFlow()
    
    private val _selectedGroup = MutableStateFlow<GroupData?>(null)
    val selectedGroup: StateFlow<GroupData?> = _selectedGroup.asStateFlow()
    
    private val _assignments = MutableStateFlow<List<AssignmentData>>(emptyList())
    val assignments: StateFlow<List<AssignmentData>> = _assignments.asStateFlow()
    
    private val _members = MutableStateFlow<List<MemberData>>(emptyList())
    val members: StateFlow<List<MemberData>> = _members.asStateFlow()
    
    private val _messages = MutableStateFlow<List<MessageData>>(emptyList())
    val messages: StateFlow<List<MessageData>> = _messages.asStateFlow()
    
    init {
        // Chỉ load groups nếu user đã đăng nhập
        val userId = userPreferences.getUserId()
        if (userId != -1L) {
            loadGroups()
        }
    }
    
    fun loadGroups() {
        val userId = userPreferences.getUserId()
        if (userId == -1L) {
            android.util.Log.e("GroupViewModel", "User ID is -1, cannot load groups")
            _groups.value = emptyList() // Clear groups
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            android.util.Log.d("GroupViewModel", "Loading groups for userId=$userId")
            
            val result = repository.getGroups(userId)
            result.fold(
                onSuccess = { response ->
                    android.util.Log.d("GroupViewModel", "Load groups response: success=${response.success}, data size=${response.data?.size}")
                    if (response.success && response.data != null) {
                        _groups.value = response.data
                        _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                    } else {
                        _groups.value = emptyList()
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = response.message
                        )
                    }
                },
                onFailure = { exception ->
                    android.util.Log.e("GroupViewModel", "Load groups failed: ${exception.message}", exception)
                    _groups.value = emptyList()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error"
                    )
                }
            )
        }
    }
    
    fun clearData() {
        _groups.value = emptyList()
        _selectedGroup.value = null
        _assignments.value = emptyList()
        _members.value = emptyList()
        _messages.value = emptyList()
        _uiState.value = GroupUiState()
    }
    
    fun createGroup(name: String, description: String) {
        val userId = userPreferences.getUserId()
        if (userId == -1L) {
            android.util.Log.e("GroupViewModel", "User ID is -1, cannot create group")
            _uiState.value = _uiState.value.copy(error = "Vui lòng đăng nhập lại")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, groupCreated = false)
            
            android.util.Log.d("GroupViewModel", "Creating group: name=$name, userId=$userId")
            
            val result = repository.createGroup(name, description, userId)
            result.fold(
                onSuccess = { response ->
                    android.util.Log.d("GroupViewModel", "Create group response: success=${response.success}, message=${response.message}")
                    if (response.success) {
                        loadGroups() // Refresh groups list
                        _uiState.value = _uiState.value.copy(
                            isLoading = false, 
                            error = null,
                            groupCreated = true
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = response.message,
                            groupCreated = false
                        )
                    }
                },
                onFailure = { exception ->
                    android.util.Log.e("GroupViewModel", "Create group failed: ${exception.message}", exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Không thể tạo nhóm",
                        groupCreated = false
                    )
                }
            )
        }
    }
    
    fun selectGroup(group: GroupData) {
        _selectedGroup.value = group
        // Reset lists trước khi load
        _assignments.value = emptyList()
        _members.value = emptyList()
        // Load data
        loadAssignments(group.id)
        loadMembers(group.id)
    }
    
    fun loadAssignments(groupId: Long) {
        viewModelScope.launch {
            try {
                val result = repository.getAssignments(groupId)
                result.fold(
                    onSuccess = { response ->
                        if (response.success && response.data != null) {
                            _assignments.value = response.data
                        } else {
                            _assignments.value = emptyList()
                            android.util.Log.e("GroupViewModel", "Load assignments failed: ${response.message}")
                        }
                    },
                    onFailure = { exception ->
                        _assignments.value = emptyList()
                        android.util.Log.e("GroupViewModel", "Load assignments error: ${exception.message}", exception)
                    }
                )
            } catch (e: Exception) {
                _assignments.value = emptyList()
                android.util.Log.e("GroupViewModel", "Load assignments exception: ${e.message}", e)
            }
        }
    }
    
    fun loadMembers(groupId: Long) {
        viewModelScope.launch {
            try {
                val result = repository.getGroupMembers(groupId)
                result.fold(
                    onSuccess = { response ->
                        if (response.success && response.data != null) {
                            _members.value = response.data
                        } else {
                            _members.value = emptyList()
                            android.util.Log.e("GroupViewModel", "Load members failed: ${response.message}")
                        }
                    },
                    onFailure = { exception ->
                        _members.value = emptyList()
                        android.util.Log.e("GroupViewModel", "Load members error: ${exception.message}", exception)
                    }
                )
            } catch (e: Exception) {
                _members.value = emptyList()
                android.util.Log.e("GroupViewModel", "Load members exception: ${e.message}", e)
            }
        }
    }
    
    fun addAssignment(
        groupId: Long,
        title: String,
        description: String,
        dueDate: Long?,
        priority: Priority
    ) {
        val userId = userPreferences.getUserId()
        if (userId == -1L) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val priorityString = when (priority) {
                Priority.LOW -> "low"
                Priority.MEDIUM -> "medium"
                Priority.HIGH -> "high"
            }
            
            val result = repository.createAssignment(
                groupId = groupId,
                title = title,
                description = description,
                dueDate = dueDate,
                priority = priorityString,
                createdBy = userId
            )
            
            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        loadAssignments(groupId) // Refresh assignments
                        _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = response.message
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to create assignment"
                    )
                }
            )
        }
    }
    
    fun addMember(groupId: Long, email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = repository.addGroupMember(groupId, email)
            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        loadMembers(groupId) // Refresh members
                        _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = response.message
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to add member"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun loadMessages(groupId: Long) {
        viewModelScope.launch {
            val result = repository.getMessages(groupId)
            result.fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        _messages.value = response.data
                    }
                },
                onFailure = { /* Handle error */ }
            )
        }
    }
    
    fun sendMessage(groupId: Long, message: String) {
        val userId = userPreferences.getUserId()
        if (userId == -1L) return
        
        viewModelScope.launch {
            val result = repository.sendMessage(groupId, userId, message)
            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        loadMessages(groupId) // Refresh messages
                    }
                },
                onFailure = { /* Handle error */ }
            )
        }
    }
    
    fun deleteMessage(messageId: Long, groupId: Long) {
        val userId = userPreferences.getUserId()
        if (userId == -1L) return
        
        viewModelScope.launch {
            val result = repository.deleteMessage(messageId, userId)
            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        loadMessages(groupId) // Refresh messages
                    }
                },
                onFailure = { /* Handle error */ }
            )
        }
    }
}

data class GroupUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val groupCreated: Boolean = false
)
