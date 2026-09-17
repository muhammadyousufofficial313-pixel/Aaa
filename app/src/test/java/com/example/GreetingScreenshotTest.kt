package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.local.entities.ConversationEntity
import com.example.data.local.entities.ConversationWithCustomer
import com.example.data.local.entities.CustomerEntity
import com.example.ui.inbox.ConversationItem
import com.example.ui.theme.WhatsAppBusinessInboxTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun conversation_item_screenshot() {
    val sampleItem = ConversationWithCustomer(
        conversation = ConversationEntity(
            id = "conv_sample",
            customerId = "15550192801",
            lastMessageText = "Hello! Could you confirm if my bulk order is ready?",
            lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 15,
            unreadCount = 1,
            windowExpiresAt = System.currentTimeMillis() + 86_400_000L,
            isImportant = true
        ),
        customer = CustomerEntity(
            id = "15550192801",
            name = "Sarah Jenkins",
            phoneNumber = "+1 555-019-2801",
            tags = listOf("VIP", "Wholesale")
        )
    )

    composeTestRule.setContent {
      WhatsAppBusinessInboxTheme {
        ConversationItem(
            item = sampleItem,
            onClick = {},
            onToggleImportant = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/conversation_item.png")
  }
}
