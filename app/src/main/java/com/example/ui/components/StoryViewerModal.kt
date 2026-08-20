package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.data.model.Story
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StoryViewerModal(
    stories: List<Story>,
    initialStoryIndex: Int,
    onDismiss: () -> Unit,
    onSendReaction: (String, String) -> Unit
) {
    if (stories.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = initialStoryIndex.coerceIn(0, stories.size - 1),
        pageCount = { stories.size }
    )
    val coroutineScope = rememberCoroutineScope()

    var progress by remember { mutableFloatStateOf(0f) }
    var isPaused by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    var floatingReaction by remember { mutableStateOf<String?>(null) }

    // Automatic story progression timer per page
    LaunchedEffect(pagerState.currentPage, isPaused) {
        progress = 0f
        while (progress < 1f && !isPaused) {
            delay(50)
            progress += 0.012f
        }
        if (progress >= 1f && !isPaused) {
            if (pagerState.currentPage < stories.size - 1) {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            } else {
                onDismiss()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .systemBarsPadding()
                .testTag("story_viewer_modal")
        ) {
            // Horizontal Pager for smooth slideable stories
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("slideable_stories_pager")
            ) { pageIndex ->
                val story = stories[pageIndex]
                val storyColor = parseColorHex(story.colorHex, getPaletteColor(story.avatarColorIndex))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(storyColor)
                ) {
                    FrameGridOverlay(lineColor = Color.White.copy(alpha = 0.15f))

                    // Left / Right tap zones for quick tap navigation while maintaining full slide capability
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(0.35f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (pagerState.currentPage > 0) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                        }
                                    }
                                }
                        )
                        Box(
                            modifier = Modifier
                                .weight(0.3f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    isPaused = !isPaused
                                }
                        )
                        Box(
                            modifier = Modifier
                                .weight(0.35f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (pagerState.currentPage < stories.size - 1) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                        }
                                    } else {
                                        onDismiss()
                                    }
                                }
                        )
                    }

                    // Center Story Visual & Schoolbell Title
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "prakash gupsup · SLIDE TO FLIP",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "FRAME // 0${pageIndex + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = story.headline,
                            style = MaterialTheme.typography.displayLarge,
                            color = Color.White,
                            fontSize = 34.sp,
                            lineHeight = 38.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = story.subhead,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Slide cue pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.3f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "⟵ Swipe or slide to explore ⟶",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            val currentStory = stories[pagerState.currentPage]

            // Top Segmented Progress Bar and User Header (Overlaid)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                // Segmented Progress Bars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    stories.forEachIndexed { i, _ ->
                        val segProgress = when {
                            i < pagerState.currentPage -> 1f
                            i == pagerState.currentPage -> progress
                            else -> 0f
                        }
                        LinearProgressIndicator(
                            progress = { segProgress },
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(CircleShape),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // User Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GupSupAvatar(
                            name = currentStory.username,
                            colorIndex = currentStory.avatarColorIndex,
                            size = 36.dp
                        )
                        Column {
                            Text(
                                text = "@${currentStory.username.lowercase()}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Slideable Story · ${pagerState.currentPage + 1}/${stories.size}",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .testTag("close_story_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Story", tint = Color.White)
                    }
                }
            }

            // Bottom Reply Bar & Quick Emojis
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                // Quick Reactions Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val reactions = listOf("🔥", "❤️", "⚡️", "👏", "✨", "📰")
                    reactions.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .clickable {
                                    floatingReaction = emoji
                                    onSendReaction(currentStory.id, emoji)
                                }
                                .padding(4.dp)
                        )
                    }
                }

                // Reply Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = {
                            replyText = it
                            isPaused = it.isNotBlank()
                        },
                        placeholder = {
                            Text("Reply to ${currentStory.username}...", color = Color.White.copy(alpha = 0.6f))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("story_reply_input"),
                        shape = RoundedCornerShape(26.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Black.copy(alpha = 0.35f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.35f)
                        ),
                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                onSendReaction(currentStory.id, replyText)
                                replyText = ""
                                isPaused = false
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(GupSupRed, CircleShape)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}
