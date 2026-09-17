package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entities.AuditLogEntity
import com.example.data.local.entities.BusinessEntity
import com.example.data.local.entities.CallLogEntity
import com.example.data.local.entities.ConversationEntity
import com.example.data.local.entities.ConversationWithCustomer
import com.example.data.local.entities.CustomerEntity
import com.example.data.local.entities.MessageEntity
import com.example.data.local.entities.StaffEntity
import com.example.data.local.entities.TemplateEntity
import com.example.domain.model.MessageStatus
import com.example.domain.model.SenderType
import kotlinx.coroutines.flow.Flow

@Dao
interface WhatsAppDao {

    // --- CONVERSATIONS ---
    @Transaction
    @Query("SELECT * FROM conversations WHERE isArchived = 0 ORDER BY lastMessageTimestamp DESC")
    fun getAllActiveConversations(): Flow<List<ConversationWithCustomer>>

    @Transaction
    @Query("SELECT * FROM conversations WHERE isArchived = 1 ORDER BY lastMessageTimestamp DESC")
    fun getArchivedConversations(): Flow<List<ConversationWithCustomer>>

    @Transaction
    @Query("SELECT * FROM conversations WHERE isImportant = 1 AND isArchived = 0 ORDER BY lastMessageTimestamp DESC")
    fun getImportantConversations(): Flow<List<ConversationWithCustomer>>

    @Transaction
    @Query("SELECT * FROM conversations WHERE unreadCount > 0 AND isArchived = 0 ORDER BY lastMessageTimestamp DESC")
    fun getUnreadConversations(): Flow<List<ConversationWithCustomer>>

    @Transaction
    @Query("SELECT * FROM conversations WHERE assignedStaffId = :staffId AND isArchived = 0 ORDER BY lastMessageTimestamp DESC")
    fun getConversationsAssignedTo(staffId: String): Flow<List<ConversationWithCustomer>>

    @Transaction
    @Query("""
        SELECT c.* FROM conversations c
        LEFT JOIN customers cust ON c.customerId = cust.id
        WHERE c.isArchived = 0 AND (
            cust.name LIKE '%' || :query || '%' OR
            cust.phoneNumber LIKE '%' || :query || '%' OR
            c.lastMessageText LIKE '%' || :query || '%'
        )
        ORDER BY c.lastMessageTimestamp DESC
    """)
    fun searchConversations(query: String): Flow<List<ConversationWithCustomer>>

    @Transaction
    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: String): ConversationWithCustomer?

    @Transaction
    @Query("SELECT * FROM conversations WHERE customerId = :customerId LIMIT 1")
    suspend fun getConversationByCustomerId(customerId: String): ConversationWithCustomer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConversation(conversation: ConversationEntity)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :conversationId")
    suspend fun markConversationAsRead(conversationId: String)

    @Query("UPDATE conversations SET isImportant = :isImportant WHERE id = :conversationId")
    suspend fun setImportant(conversationId: String, isImportant: Boolean)

    @Query("UPDATE conversations SET isArchived = :isArchived WHERE id = :conversationId")
    suspend fun setArchived(conversationId: String, isArchived: Boolean)

    @Query("UPDATE conversations SET assignedStaffId = :staffId WHERE id = :conversationId")
    suspend fun assignConversationStaff(conversationId: String, staffId: String?)

    @Query("SELECT SUM(unreadCount) FROM conversations WHERE isArchived = 0")
    fun getTotalUnreadCountFlow(): Flow<Int?>

    // --- MESSAGES ---
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getPagedMessages(conversationId: String, limit: Int, offset: Int): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE waMessageId = :waId LIMIT 1")
    suspend fun getMessageByWaId(waId: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET status = :status, failureReason = :failureReason WHERE id = :id")
    suspend fun updateMessageStatusById(id: String, status: MessageStatus, failureReason: String? = null)

    @Query("UPDATE messages SET status = :status, failureReason = :failureReason WHERE waMessageId = :waId")
    suspend fun updateMessageStatusByWaId(waId: String, status: MessageStatus, failureReason: String? = null)

    @Query("SELECT * FROM messages WHERE status = 'PENDING' ORDER BY timestamp ASC")
    suspend fun getPendingMessages(): List<MessageEntity>

    // --- CUSTOMERS ---
    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET notes = :notes WHERE id = :customerId")
    suspend fun updateCustomerNotes(customerId: String, notes: String)

    @Query("UPDATE customers SET tags = :tags WHERE id = :customerId")
    suspend fun updateCustomerTags(customerId: String, tags: String)

    @Query("UPDATE customers SET isBlocked = :blocked WHERE id = :customerId")
    suspend fun setCustomerBlocked(customerId: String, blocked: Boolean)

    // --- TEMPLATES ---
    @Query("SELECT * FROM templates ORDER BY name ASC")
    fun getAllTemplates(): Flow<List<TemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<TemplateEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TemplateEntity)

    // --- STAFF ---
    @Query("SELECT * FROM staff_members WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveStaff(): Flow<List<StaffEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffEntity)

    @Query("UPDATE staff_members SET isActive = 0 WHERE id = :staffId")
    suspend fun deactivateStaff(staffId: String)

    // --- BUSINESS SETTINGS ---
    @Query("SELECT * FROM businesses LIMIT 1")
    fun getBusinessFlow(): Flow<BusinessEntity?>

    @Query("SELECT * FROM businesses LIMIT 1")
    suspend fun getBusiness(): BusinessEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBusiness(business: BusinessEntity)

    // --- AUDIT LOGS ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // --- DASHBOARD AGGREGATES ---
    @Query("SELECT COUNT(*) FROM conversations WHERE isArchived = 0")
    fun getTotalActiveConversationsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM conversations WHERE isArchived = 0 AND windowExpiresAt > :now")
    fun getActiveWindowConversationsCount(now: Long = System.currentTimeMillis()): Flow<Int>

    @Query("SELECT COUNT(*) FROM messages WHERE senderType = 'BUSINESS'")
    fun getMessagesSentCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM messages WHERE senderType = 'CUSTOMER'")
    fun getMessagesReceivedCount(): Flow<Int>

    // --- CALL LOGS ---
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getAllCallLogs(): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE conversationId = :conversationId ORDER BY timestamp DESC")
    fun getCallLogsForConversation(conversationId: String): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(callLog: CallLogEntity)

    @Query("DELETE FROM call_logs WHERE id = :id")
    suspend fun deleteCallLog(id: String)

    @Query("DELETE FROM call_logs")
    suspend fun clearAllCallLogs()
}
