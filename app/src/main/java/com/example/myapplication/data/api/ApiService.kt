package com.example.myapplication.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Auth APIs
    @POST("auth/login.php")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("auth/register.php")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("auth/forgot_password.php")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<BaseResponse>
    
    @POST("auth/reset_password.php")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<BaseResponse>
    
    // Group APIs
    @GET("groups/groups.php")
    suspend fun getGroups(@Query("userId") userId: Long): Response<GroupsResponse>
    
    @POST("groups/groups.php")
    suspend fun createGroup(@Body request: CreateGroupRequest): Response<GroupResponse>
    
    @PUT("groups/groups.php")
    suspend fun updateGroup(@Body request: UpdateGroupRequest): Response<BaseResponse>
    
    @DELETE("groups/groups.php")
    suspend fun deleteGroup(@Query("id") groupId: Long): Response<BaseResponse>
    
    // Assignment APIs
    @GET("assignments/assignments.php")
    suspend fun getAssignments(@Query("groupId") groupId: Long): Response<AssignmentsResponse>
    
    @POST("assignments/assignments.php")
    suspend fun createAssignment(@Body request: CreateAssignmentRequest): Response<AssignmentResponse>
    
    @PUT("assignments/assignments.php")
    suspend fun updateAssignment(@Body request: UpdateAssignmentRequest): Response<BaseResponse>
    
    @DELETE("assignments/assignments.php")
    suspend fun deleteAssignment(@Query("id") assignmentId: Long): Response<BaseResponse>
    
    // Member APIs
    @GET("members/members.php")
    suspend fun getGroupMembers(@Query("groupId") groupId: Long): Response<MembersResponse>
    
    @POST("members/members.php")
    suspend fun addGroupMember(@Body request: AddMemberRequest): Response<MemberResponse>
    
    @DELETE("members/members.php")
    suspend fun removeGroupMember(@Query("groupId") groupId: Long, @Query("userId") userId: Long): Response<BaseResponse>
    
    // Message APIs
    @GET("messages/messages.php")
    suspend fun getMessages(
        @Query("groupId") groupId: Long,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<MessagesResponse>
    
    @POST("messages/messages.php")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<MessageResponse>
    
    @DELETE("messages/messages.php")
    suspend fun deleteMessage(@Query("id") messageId: Long, @Query("userId") userId: Long): Response<BaseResponse>
    
    // Message Reactions APIs
    @GET("messages/message_reactions.php")
    suspend fun getMessageReactions(@Query("messageId") messageId: Long): Response<ReactionsResponse>
    
    @POST("messages/message_reactions.php")
    suspend fun addMessageReaction(@Body request: AddReactionRequest): Response<BaseResponse>
    
    @DELETE("messages/message_reactions.php")
    suspend fun removeMessageReaction(
        @Query("messageId") messageId: Long,
        @Query("userId") userId: Long,
        @Query("reaction") reaction: String? = null
    ): Response<BaseResponse>
    
    // Message Attachments APIs
    @GET("messages/message_attachments.php")
    suspend fun getMessageAttachments(@Query("messageId") messageId: Long): Response<AttachmentsResponse>
    
    @POST("messages/message_attachments.php")
    suspend fun addMessageAttachment(@Body request: AddAttachmentRequest): Response<AttachmentResponse>
    
    @DELETE("messages/message_attachments.php")
    suspend fun deleteMessageAttachment(@Query("id") attachmentId: Long): Response<BaseResponse>
    
    // Document APIs
    @GET("documents/documents.php")
    suspend fun getDocuments(
        @Query("groupId") groupId: Long,
        @Query("fileType") fileType: String? = null
    ): Response<DocumentsResponse>
    
    @POST("documents/documents.php")
    suspend fun uploadDocument(@Body request: UploadDocumentRequest): Response<DocumentResponse>
    
    @PUT("documents/documents.php")
    suspend fun updateDocument(@Body request: UpdateDocumentRequest): Response<BaseResponse>
    
    @DELETE("documents/documents.php")
    suspend fun deleteDocument(
        @Query("id") documentId: Long,
        @Query("userId") userId: Long
    ): Response<BaseResponse>
    
    // Upload API
    @Multipart
    @POST("upload/upload.php")
    suspend fun uploadFile(
        @Part file: okhttp3.MultipartBody.Part,
        @Part("type") type: okhttp3.RequestBody
    ): Response<UploadResponse>
}

// Auth Data Classes
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: UserData? = null
)

data class UserData(
    val id: Long,
    val fullName: String,
    val email: String?,
    val phone: String?
)

// Group Data Classes
data class CreateGroupRequest(
    val name: String,
    val description: String,
    val createdBy: Long
)

data class UpdateGroupRequest(
    val id: Long,
    val name: String,
    val description: String
)

data class GroupsResponse(
    val success: Boolean,
    val message: String,
    val data: List<GroupData>? = null
)

data class GroupResponse(
    val success: Boolean,
    val message: String,
    val data: GroupData? = null
)

data class GroupData(
    val id: Long,
    val name: String,
    val description: String,
    val createdBy: Long,
    val creatorName: String,
    val memberCount: Int,
    val createdAt: String
)

// Assignment Data Classes
data class CreateAssignmentRequest(
    val groupId: Long,
    val title: String,
    val description: String,
    val dueDate: Long? = null,
    val priority: String = "medium",
    val createdBy: Long,
    val assignedMembers: List<Long> = emptyList()
)

data class UpdateAssignmentRequest(
    val id: Long,
    val title: String? = null,
    val description: String? = null,
    val dueDate: Long? = null,
    val status: String? = null,
    val priority: String? = null
)

data class AssignmentsResponse(
    val success: Boolean,
    val message: String,
    val data: List<AssignmentData>? = null
)

data class AssignmentResponse(
    val success: Boolean,
    val message: String,
    val data: AssignmentData? = null
)

data class AssignmentData(
    val id: Long,
    val groupId: Long,
    val title: String,
    val description: String,
    val dueDate: String?,
    val status: String,
    val priority: String,
    val createdBy: Long,
    val creatorName: String,
    val assignedMembers: List<MemberData> = emptyList(),
    val createdAt: String
)

// Member Data Classes
data class AddMemberRequest(
    val groupId: Long,
    val email: String,
    val role: String = "member"
)

data class MembersResponse(
    val success: Boolean,
    val message: String,
    val data: List<MemberData>? = null
)

data class MemberResponse(
    val success: Boolean,
    val message: String,
    val data: MemberData? = null
)

data class MemberData(
    val id: Long,
    val fullName: String,
    val email: String,
    val role: String,
    val joinedAt: String? = null
)

// Base Response
data class BaseResponse(
    val success: Boolean,
    val message: String
)

// Message Data Classes
data class SendMessageRequest(
    val groupId: Long,
    val userId: Long,
    val message: String
)

data class MessagesResponse(
    val success: Boolean,
    val message: String,
    val data: List<MessageData>? = null
)

data class MessageResponse(
    val success: Boolean,
    val message: String,
    val data: MessageData? = null
)

data class MessageData(
    val id: Long,
    val groupId: Long,
    val userId: Long,
    val message: String,
    val senderName: String,
    val senderEmail: String,
    val createdAt: String
)

// Message Reactions Data Classes
data class AddReactionRequest(
    val messageId: Long,
    val userId: Long,
    val reaction: String // like, love, haha, wow, sad, angry
)

data class ReactionsResponse(
    val success: Boolean,
    val message: String,
    val data: List<ReactionGroup>? = null
)

data class ReactionGroup(
    val reaction: String,
    val count: Int,
    val users: List<ReactionUser>
)

data class ReactionUser(
    val userId: Long,
    val userName: String
)

// Message Attachments Data Classes
data class AddAttachmentRequest(
    val messageId: Long,
    val fileName: String,
    val filePath: String,
    val fileType: String,
    val fileSize: Long
)

data class AttachmentsResponse(
    val success: Boolean,
    val message: String,
    val data: List<AttachmentData>? = null
)

data class AttachmentResponse(
    val success: Boolean,
    val message: String,
    val data: AttachmentData? = null
)

data class AttachmentData(
    val id: Long,
    val messageId: Long,
    val fileName: String,
    val filePath: String,
    val fileType: String,
    val fileSize: Long,
    val createdAt: String
)

// Document Data Classes
data class UploadDocumentRequest(
    val groupId: Long,
    val title: String,
    val description: String = "",
    val fileName: String,
    val filePath: String,
    val fileType: String,
    val fileSize: Long,
    val uploadedBy: Long
)

data class UpdateDocumentRequest(
    val id: Long,
    val title: String? = null,
    val description: String? = null,
    val incrementDownload: Boolean? = null
)

data class DocumentsResponse(
    val success: Boolean,
    val message: String,
    val data: List<DocumentData>? = null
)

data class DocumentResponse(
    val success: Boolean,
    val message: String,
    val data: DocumentData? = null
)

data class DocumentData(
    val id: Long,
    val groupId: Long,
    val title: String,
    val description: String,
    val fileName: String,
    val filePath: String,
    val fileType: String,
    val fileSize: Long,
    val downloadCount: Int,
    val createdAt: String,
    val updatedAt: String,
    val uploaderId: Long? = null,
    val uploaderName: String,
    val uploaderEmail: String
)

// Upload Response
data class UploadResponse(
    val success: Boolean,
    val message: String,
    val data: UploadData? = null
)

data class UploadData(
    val fileName: String,
    val uniqueFileName: String,
    val filePath: String,
    val fileType: String,
    val fileSize: Long,
    val fileExtension: String
)
