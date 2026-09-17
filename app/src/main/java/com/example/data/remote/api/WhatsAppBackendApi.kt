package com.example.data.remote.api

import com.example.data.remote.models.ConnectAccountRequest
import com.example.data.remote.models.ConnectAccountResponse
import com.example.data.remote.models.ConversationDto
import com.example.data.remote.models.DashboardAnalyticsDto
import com.example.data.remote.models.LoginRequest
import com.example.data.remote.models.LoginResponse
import com.example.data.remote.models.MediaUploadResponse
import com.example.data.remote.models.MessageDto
import com.example.data.remote.models.ResetPasswordRequest
import com.example.data.remote.models.ResetPasswordResponse
import com.example.data.remote.models.SendMediaMessageRequest
import com.example.data.remote.models.SendTemplateMessageRequest
import com.example.data.remote.models.SendTextMessageRequest
import com.example.data.remote.models.TemplateDto
import com.example.data.remote.models.WhatsAppAccountStatusResponse
import com.example.data.remote.models.WhatsAppSendMessageResponse
import com.example.data.remote.models.WhatsAppWebhookPayload
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface WhatsAppBackendApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ResetPasswordResponse>

    @GET("whatsapp/account/status")
    suspend fun getAccountStatus(): Response<WhatsAppAccountStatusResponse>

    @POST("whatsapp/account/connect")
    suspend fun connectAccount(@Body request: ConnectAccountRequest): Response<ConnectAccountResponse>

    @GET("conversations")
    suspend fun getConversations(
        @Query("limit") limit: Int = 50,
        @Query("cursor") cursor: String? = null
    ): Response<List<ConversationDto>>

    @GET("conversations/{id}/messages")
    suspend fun getMessages(
        @Path("id") conversationId: String,
        @Query("limit") limit: Int = 50,
        @Query("before") before: Long? = null
    ): Response<List<MessageDto>>

    @POST("messages/send")
    suspend fun sendTextMessage(@Body request: SendTextMessageRequest): Response<WhatsAppSendMessageResponse>

    @POST("messages/send-media")
    suspend fun sendMediaMessage(@Body request: SendMediaMessageRequest): Response<WhatsAppSendMessageResponse>

    @POST("messages/send-template")
    suspend fun sendTemplateMessage(@Body request: SendTemplateMessageRequest): Response<WhatsAppSendMessageResponse>

    @Multipart
    @POST("media/upload")
    suspend fun uploadMedia(
        @Part file: MultipartBody.Part,
        @Part("type") type: String
    ): Response<MediaUploadResponse>

    @GET("templates")
    suspend fun getTemplates(): Response<List<TemplateDto>>

    @POST("templates/sync")
    suspend fun syncTemplates(): Response<List<TemplateDto>>

    @POST("webhooks/whatsapp")
    suspend fun simulateWebhook(@Body payload: WhatsAppWebhookPayload): Response<Map<String, String>>

    @GET("analytics/dashboard")
    suspend fun getDashboardAnalytics(): Response<DashboardAnalyticsDto>
}
