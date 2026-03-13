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
        GroupMessage::class,
        ChatRoom::class,
        ChatMember::class,
        MessageReaction::class,
        MessageThread::class,
        MessageAttachment::class,
        AssignmentChat::class,
        ChatNotification::class,
        Document::class,
        DocumentFolder::class,
        Poll::class,
        MessageTemplate::class,
        Sticker::class,
        StickerPack::class,
        Mention::class,
        LinkPreview::class,
        ScheduledMessage::class
    ],
    version = 5, // Tăng version lên 5
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    // Existing DAOs
    abstract fun groupDao(): GroupDao
    abstract fun memberDao(): MemberDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun assignmentMemberDao(): AssignmentMemberDao
    abstract fun groupMessageDao(): GroupMessageDao
    
    // Chat DAOs
    abstract fun chatRoomDao(): ChatRoomDao
    abstract fun chatMemberDao(): ChatMemberDao
    abstract fun messageReactionDao(): MessageReactionDao
    abstract fun messageAttachmentDao(): MessageAttachmentDao
    abstract fun chatNotificationDao(): ChatNotificationDao
    
    // Document DAOs
    abstract fun documentDao(): DocumentDao
    
    // Poll DAO
    abstract fun pollDao(): PollDao
    
    // Template DAO
    abstract fun messageTemplateDao(): MessageTemplateDao
    
    // Sticker DAOs
    abstract fun stickerDao(): StickerDao
    
    // Mention DAOs
    abstract fun mentionDao(): MentionDao
    
    // Scheduled Message DAO
    abstract fun scheduledMessageDao(): ScheduledMessageDao
    
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
