package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WebhookSimulatorDialog(
    onDismiss: () -> Unit,
    onSimulate: (phoneNumber: String, name: String, text: String) -> Unit
) {
    var phone by remember { mutableStateOf("+1 555-839-1029") }
    var name by remember { mutableStateOf("Bilal Tariq") }
    var text by remember { mutableStateOf("Hi, I want to inquire about wholesale pricing and delivery schedules.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simulate Incoming WhatsApp Message", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Triggers an authentic Meta Cloud API webhook event (entry[].changes[].value.messages[]). Tests database storage, reactive UI update, and push notification delivery.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Customer WhatsApp Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("sim_phone_input")
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Profile Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("sim_name_input")
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Message Body") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("sim_text_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (phone.isNotBlank() && text.isNotBlank()) {
                        onSimulate(phone, name, text)
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_confirm_simulation")
            ) {
                Text("Simulate Webhook Event")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
