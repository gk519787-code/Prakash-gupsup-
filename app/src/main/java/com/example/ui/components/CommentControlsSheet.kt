package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import com.example.data.model.CommentEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentControlsSheet(
    postId: String,
    postTitle: String,
    comments: List<CommentEntity>,
    onDismiss: () -> Unit,
    onAddComment: (String, Boolean) -> Unit,
    onLikeComment: (String, Boolean, Int) -> Unit,
    onSparkPolishText: suspend (String) -> String
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var inputText by remember { mutableStateOf("") }
    var isPolishing by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf("top") } // "top" or "new"
    val coroutineScope = rememberCoroutineScope()

    val quickSparkChips = listOf(
        "🔥 Masterful tonal balance!",
        "✨ The light leak in this frame is pure magic.",
        "⚡️ That sound sync is next level.",
        "🎞️ Cinematic poetry."
    )

    val sortedComments = remember(comments, sortOrder) {
        if (sortOrder == "top") {
            comments.sortedByDescending { it.likes }
        } else {
            comments
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalBottomSheetState,
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
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    OverlineTag(text = "CONVERSATION / $postTitle", color = GupSupBlue)
                    Text(
                        text = "Comments (${comments.size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = GupSupText
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = sortOrder == "top",
                        onClick = { sortOrder = "top" },
                        label = { Text("Top", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GupSupBlue,
                            selectedLabelColor = GupSupBg
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                    FilterChip(
                        selected = sortOrder == "new",
                        onClick = { sortOrder = "new" },
                        label = { Text("New", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GupSupBlue,
                            selectedLabelColor = GupSupBg
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }
            }

            HorizontalDivider(color = GupSupLine, thickness = 1.dp)

            // Spark Quick Suggestions
            Column(modifier = Modifier.padding(vertical = 10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = GupSupRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "SPARK SMART REPLIES",
                        style = MaterialTheme.typography.labelSmall,
                        color = GupSupRed
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(quickSparkChips) { chip ->
                        Box(
                            modifier = Modifier
                                .border(1.dp, GupSupLine, RoundedCornerShape(14.dp))
                                .background(GupSupRaised.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .clickable {
                                    inputText = chip
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = chip,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 12.sp,
                                color = GupSupText
                            )
                        }
                    }
                }
            }

            // Comments List
            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .heightIn(max = 340.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                if (sortedComments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No comments yet.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = GupSupMuted
                                )
                                Text(
                                    text = "Spark the conversation above!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GupSupDim
                                )
                            }
                        }
                    }
                } else {
                    items(sortedComments, key = { it.id }) { comment ->
                        CommentRow(
                            comment = comment,
                            onLike = { onLikeComment(comment.id, comment.isLiked, comment.likes) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sticky Bottom Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GupSupAvatar(name = "You", colorIndex = 0, size = 36.dp)

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Add to the discussion...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GupSupDim
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    isPolishing = true
                                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).let {
                                        // launch spark polish
                                    }
                                }
                            },
                            enabled = !isPolishing
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Spark AI Polish",
                                tint = if (inputText.isNotBlank()) GupSupRed else GupSupDim,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("comment_input_field"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GupSupBlue,
                        unfocusedBorderColor = GupSupLine,
                        focusedContainerColor = GupSupSurface,
                        unfocusedContainerColor = GupSupSurface,
                        focusedTextColor = GupSupText,
                        unfocusedTextColor = GupSupText
                    ),
                    singleLine = false,
                    maxLines = 3
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onAddComment(inputText.trim(), false)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            if (inputText.isNotBlank()) GupSupRed else GupSupLine,
                            CircleShape
                        )
                        .testTag("send_comment_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Post comment",
                        tint = if (inputText.isNotBlank()) GupSupBg else GupSupDim,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentRow(
    comment: CommentEntity,
    onLike: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (comment.isSparkAi) {
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GupSupBlue.copy(alpha = 0.06f))
                        .padding(8.dp)
                } else Modifier
            ),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        GupSupAvatar(
            name = comment.username,
            colorIndex = comment.avatarColorIndex,
            size = 32.dp
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "@${comment.username}",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 13.sp,
                    color = GupSupText
                )
                if (comment.isSparkAi) {
                    Box(
                        modifier = Modifier
                            .background(GupSupRed, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "AI DIRECTOR",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = comment.timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = GupSupDim
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = GupSupMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = onLike,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (comment.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like comment",
                    tint = if (comment.isLiked) GupSupRed else GupSupDim,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (comment.likes > 0) {
                Text(
                    text = comment.likes.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = GupSupDim
                )
            }
        }
    }
}
