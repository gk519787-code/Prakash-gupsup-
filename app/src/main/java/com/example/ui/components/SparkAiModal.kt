package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SparkAiModal(
    onDismiss: () -> Unit,
    onApplyGeneratedText: (String) -> Unit,
    onGenerateCaption: suspend (String, String) -> String,
    onGenerateReelAudio: suspend (String, String) -> String
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var selectedMode by remember { mutableStateOf("caption") } // "caption", "reel", "critique"
    var promptInput by remember { mutableStateOf("Late night neon reflections in rain") }
    var selectedTone by remember { mutableStateOf("Cinematic") }
    var resultText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val tones = listOf("Cinematic", "Poetic", "Viral", "Minimalist")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = GupSupSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(GupSupLine)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(GupSupRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = GupSupBg,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        OverlineTag(text = "CREATIVE DIRECTOR", color = GupSupRed)
                        Text(
                            text = "Spark Gemini AI",
                            style = MaterialTheme.typography.titleLarge,
                            color = GupSupText
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = GupSupText)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mode Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GupSupLine, RoundedCornerShape(14.dp))
                    .background(GupSupRaised.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val modes = listOf("caption" to "Captions", "reel" to "Reel Sound/Hook", "critique" to "Critique")
                modes.forEach { (modeKey, modeLabel) ->
                    val isSelected = selectedMode == modeKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) GupSupBlue else Color.Transparent)
                            .clickable { selectedMode = modeKey }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = modeLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) GupSupBg else GupSupMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Concept Input
            Text(
                text = "What is the concept or frame vibe?",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = GupSupText
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("spark_prompt_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GupSupBlue,
                    unfocusedBorderColor = GupSupLine,
                    focusedContainerColor = GupSupSurface,
                    unfocusedContainerColor = GupSupSurface,
                    focusedTextColor = GupSupText,
                    unfocusedTextColor = GupSupText
                ),
                maxLines = 3
            )

            if (selectedMode == "caption") {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Select Tone",
                    style = MaterialTheme.typography.labelSmall,
                    color = GupSupDim
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tones.forEach { tone ->
                        FilterChip(
                            selected = selectedTone == tone,
                            onClick = { selectedTone = tone },
                            label = { Text(tone, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GupSupRed,
                                selectedLabelColor = GupSupBg
                            ),
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        resultText = if (selectedMode == "caption") {
                            onGenerateCaption(promptInput, selectedTone)
                        } else {
                            onGenerateReelAudio("Frame Cut", promptInput)
                        }
                        isLoading = false
                    }
                },
                enabled = !isLoading && promptInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("generate_spark_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GupSupRed,
                    contentColor = GupSupBg
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = GupSupBg,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(text = "Spark Creativity", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Results Card
            AnimatedVisibility(visible = resultText.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 12.dp)
                        .fillMaxWidth()
                        .border(1.dp, GupSupLine, RoundedCornerShape(14.dp))
                        .background(GupSupRaised.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OverlineTag(text = "SPARK OUTPUT", color = GupSupBlue)
                        Row {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(resultText))
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy text",
                                    tint = GupSupMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = resultText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = GupSupText,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            onApplyGeneratedText(resultText)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GupSupBlue,
                            contentColor = GupSupBg
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Text("Apply to Composer / Frame", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
