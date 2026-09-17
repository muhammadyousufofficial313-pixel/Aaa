package com.example.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ConversationWithCustomer
import com.example.data.local.entities.StaffEntity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomerDetailsSheet(
    sheetState: SheetState,
    item: ConversationWithCustomer,
    staffList: List<StaffEntity>,
    onDismiss: () -> Unit,
    onSaveNotes: (String) -> Unit,
    onUpdateTags: (List<String>) -> Unit,
    onAssignStaff: (String?) -> Unit,
    onBlockCustomer: (Boolean) -> Unit,
    onStartAudioCall: (() -> Unit)? = null,
    onStartVideoCall: (() -> Unit)? = null
) {
    val cust = item.customer
    val conv = item.conversation

    var notesText by remember(cust?.notes) { mutableStateOf(cust?.notes ?: "") }
    var currentTags by remember(cust?.tags) { mutableStateOf(cust?.tags ?: emptyList()) }
    var newTagInput by remember { mutableStateOf("") }
    var staffDropdownExpanded by remember { mutableStateOf(false) }

    val assignedStaff = staffList.find { it.id == conv.assignedStaffId }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Profile
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (cust?.name ?: conv.customerId).take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = cust?.name ?: conv.customerId,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = cust?.phoneNumber ?: conv.customerId,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Call Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onDismiss()
                        onStartAudioCall?.invoke()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_details_audio_call"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Audio Call")
                }

                Button(
                    onClick = {
                        onDismiss()
                        onStartVideoCall?.invoke()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_details_video_call"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Video Call")
                }
            }

            HorizontalDivider()

            // Staff Assignment
            Column {
                Text(
                    text = "Assigned Team Member",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = staffDropdownExpanded,
                    onExpandedChange = { staffDropdownExpanded = !staffDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = assignedStaff?.name ?: "Unassigned",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = staffDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("staff_assign_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = staffDropdownExpanded,
                        onDismissRequest = { staffDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Unassigned") },
                            onClick = {
                                onAssignStaff(null)
                                staffDropdownExpanded = false
                            }
                        )
                        staffList.forEach { staff ->
                            DropdownMenuItem(
                                text = { Text("${staff.name} (${staff.role})") },
                                onClick = {
                                    onAssignStaff(staff.id)
                                    staffDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Customer Tags
            Column {
                Text(
                    text = "Customer Tags",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    currentTags.forEach { tag ->
                        InputChip(
                            selected = false,
                            onClick = {
                                val updated = currentTags.filter { it != tag }
                                currentTags = updated
                                onUpdateTags(updated)
                            },
                            label = { Text(tag) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove tag",
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTagInput,
                        onValueChange = { newTagInput = it },
                        placeholder = { Text("New tag (e.g. Wholesale, VIP)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_tag_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newTagInput.isNotBlank()) {
                                val updated = currentTags + newTagInput.trim()
                                currentTags = updated
                                onUpdateTags(updated)
                                newTagInput = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add tag")
                    }
                }
            }

            // Customer Notes
            Column {
                Text(
                    text = "Internal Business Notes",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text("Add preferences, order history or context for your team...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_notes_input"),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onSaveNotes(notesText) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.End)
                        .testTag("btn_save_notes")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Notes")
                }
            }

            HorizontalDivider()

            // Block Customer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (cust?.isBlocked == true) "Customer is Blocked" else "Block WhatsApp Contact",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (cust?.isBlocked == true) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Prevents incoming messages from triggering alerts",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = { onBlockCustomer(cust?.isBlocked != true) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_block_customer")
                ) {
                    Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (cust?.isBlocked == true) "Unblock" else "Block")
                }
            }
        }
    }
}
