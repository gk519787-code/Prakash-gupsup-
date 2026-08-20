package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.outlined.Palette
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
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CreateScreen(
    onOpenComposer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GupSupBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OverlineTag(text = "MAKE SOMETHING", color = GupSupBlue)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Create",
            style = MaterialTheme.typography.displayMedium,
            color = GupSupText
        )

        // Visual Placeholder Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(vertical = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, GupSupLine, RoundedCornerShape(24.dp))
                .background(GupSupSurface)
                .clickable { onOpenComposer() },
            contentAlignment = Alignment.Center
        ) {
            FrameGridOverlay()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(GupSupRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Frame",
                        tint = GupSupBg,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "YOUR NEXT FRAME\nSTARTS HERE.",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GupSupText,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Button(
            onClick = onOpenComposer,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("open_composer_btn"),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GupSupRed,
                contentColor = GupSupBg
            )
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Open Composer", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "PHOTO · VIDEO · REEL · STORY",
            style = MaterialTheme.typography.labelSmall,
            color = GupSupDim
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposerModal(
    visible: Boolean,
    onClose: () -> Unit,
    onSubmit: (String, String, String, String, String, String) -> Unit,
    onSparkGenerateCaption: suspend (String, String) -> String
) {
    if (!visible) return

    var caption by remember { mutableStateOf("") }
    var frameTitle by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("#gupsup #creative #newcut") }
    var selectedType by remember { mutableStateOf("photo") }
    var selectedColorHex by remember { mutableStateOf("#1557C0") }
    var selectedFilter by remember { mutableStateOf("Original") }
    var isSparkGenerating by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val types = listOf("photo", "reel", "story", "text")
    val colorHexes = listOf(
        "#1557C0" to "Cobalt",
        "#E85D2A" to "Rust",
        "#F0B323" to "Amber",
        "#5E3FA3" to "Ultra",
        "#2A8C82" to "Teal"
    )
    val filters = listOf("Original", "Editorial Noir", "Sunset Rust", "Slate Crisp", "Neon Night")

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .imePadding()
                .systemBarsPadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .testTag("composer_sheet"),
                shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
                colors = CardDefaults.cardColors(containerColor = GupSupSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "New Frame",
                            style = MaterialTheme.typography.headlineMedium,
                            color = GupSupText,
                            fontWeight = FontWeight.Black
                        )
                        IconButton(onClick = onClose, modifier = Modifier.testTag("close_composer_btn")) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = GupSupText)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Visual Preview Frame
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(parseColorHex(selectedColorHex, GupSupBlue)),
                        contentAlignment = Alignment.Center
                    ) {
                        FrameGridOverlay()

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = GupSupText,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = frameTitle.ifBlank { "ADD YOUR VISUAL" },
                                style = MaterialTheme.typography.titleMedium,
                                color = GupSupText,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${selectedType.uppercase()} / $selectedFilter",
                                style = MaterialTheme.typography.labelSmall,
                                color = GupSupText.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Type Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        types.forEach { type ->
                            val isSelected = selectedType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(
                                        1.dp,
                                        if (isSelected) GupSupRed else GupSupLine,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .background(if (isSelected) GupSupRed else GupSupSurface)
                                    .clickable { selectedType = type }
                                    .padding(vertical = 10.dp)
                                    .testTag("type-$type"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) GupSupBg else GupSupMuted,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Color Tone Selector
                    Text(
                        text = "Colorway Preset",
                        style = MaterialTheme.typography.labelSmall,
                        color = GupSupDim
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        colorHexes.forEach { (hex, name) ->
                            val isSelected = selectedColorHex == hex
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(parseColorHex(hex, GupSupBlue))
                                    .border(
                                        if (isSelected) 2.5.dp else 1.dp,
                                        if (isSelected) GupSupText else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { selectedColorHex = hex }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Frame Title Input
                    OutlinedTextField(
                        value = frameTitle,
                        onValueChange = { frameTitle = it },
                        placeholder = { Text("Frame Title (e.g. THE AFTERGLOW)", color = GupSupDim) },
                        modifier = Modifier.fillMaxWidth().testTag("frame_title_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GupSupBlue,
                            unfocusedBorderColor = GupSupLine,
                            focusedTextColor = GupSupText,
                            unfocusedTextColor = GupSupText
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Caption Input with Spark AI Magic Button
                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        placeholder = { Text("Write a caption...", color = GupSupDim) },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        isSparkGenerating = true
                                        val prompt = if (caption.isNotBlank()) caption else "Late night Tokyo frame with neon reflection"
                                        val generated = onSparkGenerateCaption(prompt, "Cinematic")
                                        caption = generated
                                        isSparkGenerating = false
                                    }
                                },
                                enabled = !isSparkGenerating,
                                modifier = Modifier.testTag("spark_magic_caption_btn")
                            ) {
                                if (isSparkGenerating) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = GupSupRed)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Gemini Magic Caption",
                                        tint = GupSupRed
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("caption-input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GupSupBlue,
                            unfocusedBorderColor = GupSupLine,
                            focusedTextColor = GupSupText,
                            unfocusedTextColor = GupSupText
                        ),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tags Input
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        placeholder = { Text("#tags (e.g. #35mm #cinematic)", color = GupSupDim) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GupSupBlue,
                            unfocusedBorderColor = GupSupLine,
                            focusedTextColor = GupSupText,
                            unfocusedTextColor = GupSupText
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Publish Button
                    Button(
                        onClick = {
                            onSubmit(
                                caption.ifBlank { "A new frame, cut fresh." },
                                selectedType,
                                frameTitle.ifBlank { "FRAME // CUT" },
                                selectedColorHex,
                                selectedFilter,
                                tags
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("publish-button"),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GupSupRed,
                            contentColor = GupSupBg
                        )
                    ) {
                        Text(text = "Publish Frame", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}
