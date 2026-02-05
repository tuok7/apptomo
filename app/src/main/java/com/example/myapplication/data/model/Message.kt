package com.example.myapplication.data.model

data class Message(
    val id: Long,
    val groupId: Long,
    val userId: Long,
    val message: String,
    val senderName: String,
    val senderEmail: String,
    val createdAt: String
)
