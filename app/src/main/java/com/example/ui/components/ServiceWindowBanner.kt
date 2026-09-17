package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WindowActiveGreen
import com.example.ui.theme.WindowWarningAmber

@Composable
fun ServiceWindowBanner(
    remainingMillis: Long,
    onOpenTemplatePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpired = remainingMillis <= 0L
    val hours = (remainingMillis / (1000 * 60 * 60)).coerceAtLeast(0)
    val minutes = ((remainingMillis / (1000 * 60)) % 60).coerceAtLeast(0)

    val isWarning = !isExpired && hours < 3

    val (bgColor, contentColor, icon) = when {
        isExpired -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Default.Warning
        )
        isWarning -> Triple(
            WindowWarningAmber.copy(alpha = 0.15f),
            WindowWarningAmber,
            Icons.Default.HourglassBottom
        )
        else -> Triple(
            WindowActiveGreen.copy(alpha = 0.12f),
            WindowActiveGreen,
            Icons.Default.CheckCircle
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("service_window_banner"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isExpired) {
                            "24h Service Window Expired"
                        } else {
                            "24h Window Active: ${hours}h ${minutes}m left"
                        },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = contentColor
                    )
                }

                if (isExpired) {
                    Button(
                        onClick = onOpenTemplatePicker,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("btn_send_template_banner")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Send Template", fontSize = 12.sp)
                    }
                }
            }

            AnimatedVisibility(visible = isExpired) {
                Text(
                    text = "Meta WhatsApp Business policy requires sending an approved Message Template to restart communication once 24 hours have elapsed without a customer response.",
                    fontSize = 11.sp,
                    color = contentColor.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}
