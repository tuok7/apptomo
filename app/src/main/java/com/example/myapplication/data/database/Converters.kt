package com.example.myapplication.data.database

import androidx.room.TypeConverter
import com.example.myapplication.data.model.AssignmentStatus
import com.example.myapplication.data.model.Priority

class Converters {
    @TypeConverter
    fun fromAssignmentStatus(value: AssignmentStatus): String {
        return value.name
    }
    
    @TypeConverter
    fun toAssignmentStatus(value: String): AssignmentStatus {
        return AssignmentStatus.valueOf(value)
    }
    
    @TypeConverter
    fun fromPriority(value: Priority): String {
        return value.name
    }
    
    @TypeConverter
    fun toPriority(value: String): Priority {
        return Priority.valueOf(value)
    }
}
