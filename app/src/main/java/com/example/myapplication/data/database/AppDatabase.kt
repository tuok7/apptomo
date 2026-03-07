package com.example.myapplication.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.data.dao.*
import com.example.myapplication.data.model.*

@Database(
    entities = [
        Group::class,
        Member::class,
        Assignment::class,
        AssignmentMember::class,
        GroupMessage::class
    ],
    version = 3, // Giữ nguyên version 3
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun memberDao(): MemberDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun assignmentMemberDao(): AssignmentMemberDao
    abstract fun groupMessageDao(): GroupMessageDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "group_assignment_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
