package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "assignment_members",
    primaryKeys = ["assignmentId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = Assignment::class,
            parentColumns = ["id"],
            childColumns = ["assignmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("assignmentId"), Index("userId")]
)
data class AssignmentMember(
    val assignmentId: Long,
    val userId: Long,
    val userName: String = "",
    val status: SubmissionStatus = SubmissionStatus.ASSIGNED,
    val submissionFile: String? = null,
    val submissionDate: Long? = null,
    val score: Int? = null,
    val feedback: String? = null,
    val gradedBy: Long? = null,
    val gradedAt: Long? = null,
    val assignedAt: Long = System.currentTimeMillis()
)

enum class SubmissionStatus {
    ASSIGNED,      // Đã giao
    IN_PROGRESS,   // Đang làm
    SUBMITTED,     // Đã nộp
    GRADED         // Đã chấm
}
