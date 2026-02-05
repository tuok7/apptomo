package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "assignment_members",
    primaryKeys = ["assignmentId", "memberId"],
    foreignKeys = [
        ForeignKey(
            entity = Assignment::class,
            parentColumns = ["id"],
            childColumns = ["assignmentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("assignmentId"), Index("memberId")]
)
data class AssignmentMember(
    val assignmentId: Long,
    val memberId: Long
)
