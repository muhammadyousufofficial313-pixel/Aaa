package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entities.CallLogEntity
import com.example.data.local.entities.ConversationEntity
import com.example.data.local.entities.CustomerEntity
import com.example.data.local.entities.MessageEntity
import com.example.data.remote.engine.MockBackendEngine
import com.example.domain.model.CallDirection
import com.example.domain.model.CallStatus
import com.example.domain.model.CallType
import com.example.domain.model.MessageStatus
import com.example.domain.model.MessageType
import com.example.domain.model.SenderType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WhatsAppInboxRobolectricTest {

    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testCustomerAndConversationInsertion() = runBlocking {
        val dao = database.whatsAppDao()

        val customer = CustomerEntity(
            id = "15551234567",
            name = "John Doe",
            phoneNumber = "+1 555-123-4567",
            tags = listOf("Lead", "VIP")
        )
        dao.insertOrUpdateCustomer(customer)

        val conv = ConversationEntity(
            id = "conv_15551234567",
            customerId = "15551234567",
            lastMessageText = "Hello from WhatsApp",
            lastMessageTimestamp = System.currentTimeMillis(),
            unreadCount = 1,
            windowExpiresAt = System.currentTimeMillis() + 86_400_000L
        )
        dao.insertOrUpdateConversation(conv)

        val activeList = dao.getAllActiveConversations().first()
        assertEquals(1, activeList.size)
        assertEquals("John Doe", activeList[0].customer?.name)
        assertEquals("Hello from WhatsApp", activeList[0].conversation.lastMessageText)
        assertTrue(activeList[0].conversation.isWindowActive)
    }

    @Test
    fun testMessageDeliveryAndReadTransitions() = runBlocking {
        val dao = database.whatsAppDao()

        val msg = MessageEntity(
            id = "msg_test_1",
            conversationId = "conv_123",
            waMessageId = "wamid.HBgL1234567890",
            senderType = SenderType.BUSINESS,
            content = "Thank you for contacting us!",
            status = MessageStatus.SENT,
            timestamp = System.currentTimeMillis()
        )
        dao.insertMessage(msg)

        val retrieved = dao.getMessageById("msg_test_1")
        assertNotNull(retrieved)
        assertEquals(MessageStatus.SENT, retrieved?.status)

        dao.updateMessageStatusById("msg_test_1", MessageStatus.DELIVERED)
        assertEquals(MessageStatus.DELIVERED, dao.getMessageById("msg_test_1")?.status)

        dao.updateMessageStatusById("msg_test_1", MessageStatus.READ)
        assertEquals(MessageStatus.READ, dao.getMessageById("msg_test_1")?.status)
    }

    @Test
    fun testWebhookPayloadGeneration() {
        val payload = MockBackendEngine.createSimulatedIncomingMessagePayload(
            fromPhone = "+15559876543",
            customerName = "Ali Raza",
            messageText = "Inquiring about business catalog"
        )

        assertEquals("whatsapp_business_account", payload.objectType)
        val entry = payload.entry.firstOrNull()
        assertNotNull(entry)
        val change = entry?.changes?.firstOrNull()
        assertNotNull(change)
        assertEquals("messages", change?.field)

        val incoming = change?.value?.messages?.firstOrNull()
        assertNotNull(incoming)
        assertEquals("Inquiring about business catalog", incoming?.text?.body)
        assertTrue(incoming?.id?.startsWith("wamid.") == true)
    }

    @Test
    fun testCallLogInsertionAndRetrieval() = runBlocking {
        val dao = database.whatsAppDao()

        val callLog = CallLogEntity(
            id = "call_test_1",
            conversationId = "conv_123",
            customerId = "15551234567",
            customerName = "John Doe",
            customerPhone = "+1 555-123-4567",
            callType = CallType.VIDEO,
            direction = CallDirection.INCOMING,
            status = CallStatus.CONNECTED,
            durationSeconds = 145L,
            timestamp = System.currentTimeMillis()
        )
        dao.insertCallLog(callLog)

        val logs = dao.getAllCallLogs().first()
        assertEquals(1, logs.size)
        assertEquals("call_test_1", logs[0].id)
        assertEquals(CallType.VIDEO, logs[0].callType)
        assertEquals(CallDirection.INCOMING, logs[0].direction)
        assertEquals(145L, logs[0].durationSeconds)

        val convLogs = dao.getCallLogsForConversation("conv_123").first()
        assertEquals(1, convLogs.size)
        assertEquals("John Doe", convLogs[0].customerName)
    }
}
