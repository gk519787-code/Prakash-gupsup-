package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MessageEntity
import com.example.ui.components.*
import com.example.ui.theme.*

data class ChatPartner(
    val id: String,
    val name: String,
    val subtitle: String,
    val avatarColorIndex: Int,
    val isSparkAi: Boolean = false
)

@Composable
fun MessagesModal(
    visible: Boolean,
    messages: List<MessageEntity>,
    onClose: () -> Unit,
    onSendMessage: (String, String, String) -> Unit
) {
    if (!visible) return

    val partners = listOf(
        ChatPartner("spark", "✨ Spark AI Director", "Gemini creative assistant & critique bot", 0, isSparkAi = true),
        ChatPartner("mara", "Mara Frame", "The light in frame 03 is perfect.", 1),
        ChatPartner("noah", "Noah Afterhours", "Sending the rough cut now.", 2),
        ChatPartner("iris", "Studio Iris", "Let's make the next one quieter.", 3),
        ChatPartner("june", "June Park", "You around for a shoot?", 4)
    )

    var activePartner by remember { mutableStateOf<ChatPartner?>(null) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GupSupBg)
                .systemBarsPadding()
                .imePadding()
                .testTag("messages_modal")
        ) {
            if (activePartner == null) {
                // Conversations List
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            OverlineTag(text = "DIRECT SYNC", color = GupSupBlue)
                            Text(
                                text = "Messages",
                                style = MaterialTheme.typography.displayMedium,
                                color = GupSupText
                            )
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(40.dp)
                                .border(1.dp, GupSupLine, CircleShape)
                                .background(GupSupSurface, CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = GupSupText)
                        }
                    }

                    HorizontalDivider(color = GupSupLine, thickness = 1.dp)

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(partners) { partner ->
                            val lastMsg = messages.filter { it.chatPartnerId == partner.id }.lastOrNull()?.text ?: partner.subtitle
                            val isSpark = partner.isSparkAi

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(
                                        1.dp,
                                        if (isSpark) GupSupRed else GupSupLine,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .background(
                                        if (isSpark) GupSupRed.copy(alpha = 0.06f) else GupSupSurface
                                    )
                                    .clickable { activePartner = partner }
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GupSupAvatar(
                                    name = partner.name,
                                    colorIndex = partner.avatarColorIndex,
                                    size = 44.dp
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = partner.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = GupSupText
                                        )
                                        if (isSpark) {
                                            Box(
                                                modifier = Modifier
                                                    .background(GupSupRed, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "GEMINI",
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = lastMsg,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GupSupMuted,
                                        maxLines = 1,
                                        fontSize = 13.sp
                                    )
                                }

                                Text(
                                    text = "LIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSpark) GupSupRed else GupSupDim,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = "MESSAGING IS READY · LIVE SYNC CONNECTS WHEN AVAILABLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = GupSupDim,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                // Active Chat Room View
                val currentPartner = activePartner!!
                val partnerMessages = messages.filter { it.chatPartnerId == currentPartner.id }
                var chatInput by remember { mutableStateOf("") }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Chat Top Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = { activePartner = null }) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GupSupText)
                            }
                            GupSupAvatar(name = currentPartner.name, colorIndex = currentPartner.avatarColorIndex, size = 36.dp)
                            Column {
                                Text(
                                    text = currentPartner.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GupSupText
                                )
                                Text(
                                    text = if (currentPartner.isSparkAi) "Gemini Director · Online" else "Active now",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GupSupTeal,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        IconButton(onClick = onClose) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = GupSupText)
                        }
                    }

                    HorizontalDivider(color = GupSupLine, thickness = 1.dp)

                    // Message Bubbles List
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(partnerMessages) { msg ->
                            val isUser = msg.isFromUser
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .widthIn(max = 280.dp)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (isUser) 16.dp else 4.dp,
                                                bottomEnd = if (isUser) 4.dp else 16.dp
                                            )
                                        )
                                        .background(
                                            when {
                                                isUser -> GupSupRed
                                                msg.isSparkAi -> GupSupBlue.copy(alpha = 0.12f)
                                                else -> GupSupSurface
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            if (isUser) GupSupRed else GupSupLine,
                                            RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (isUser) 16.dp else 4.dp,
                                                bottomEnd = if (isUser) 4.dp else 16.dp
                                            )
                                        )
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isUser) GupSupBg else GupSupText,
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                        }
                    }

                    // Chat Input Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text("Message ${currentPartner.name}...", color = GupSupDim) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_message_input"),
                            shape = RoundedCornerShape(22.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GupSupBlue,
                                unfocusedBorderColor = GupSupLine,
                                focusedContainerColor = GupSupSurface,
                                unfocusedContainerColor = GupSupSurface,
                                focusedTextColor = GupSupText,
                                unfocusedTextColor = GupSupText
                            ),
                            singleLine = true
                        )

                        IconButton(
                            onClick = {
                                if (chatInput.isNotBlank()) {
                                    onSendMessage(currentPartner.id, currentPartner.name, chatInput.trim())
                                    chatInput = ""
                                }
                            },
                            enabled = chatInput.isNotBlank(),
                            modifier = Modifier
                                .size(44.dp)
                                .background(if (chatInput.isNotBlank()) GupSupRed else GupSupLine, CircleShape)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = GupSupBg)
                        }
                    }
                }
            }
        }
    }
}
